/*
 * Copyright 2024 Stanislav Aleshin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.aleshin.studyassistant.editor.impl.domain.interactors

import kotlinx.coroutines.flow.first
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateOfWeekDay
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.DayOfNumberedWeek
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.DateVersion
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.managers.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 30.05.2024.
 */
internal interface BaseClassInteractor {

    suspend fun addClassBySchedule(
        classModel: Class,
        schedule: BaseSchedule?,
        targetDay: DayOfNumberedWeek
    ): DomainResult<EditorFailures, UID>

    suspend fun fetchClass(classId: UID, scheduleId: UID): FlowDomainResult<EditorFailures, Class?>

    suspend fun updateClassBySchedule(classModel: Class, schedule: BaseSchedule): DomainResult<EditorFailures, UID>

    suspend fun deleteClassBySchedule(uid: UID, schedule: BaseSchedule): UnitDomainResult<EditorFailures>

    class Base(
        private val scheduleRepository: BaseScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val notificationSettingsRepository: NotificationSettingsRepository,
        private val usersRepository: UsersRepository,
        private val startClassesReminderManager: StartClassesReminderManager,
        private val endClassesReminderManager: EndClassesReminderManager,
        private val dateManager: DateManager,
        private val eitherWrapper: EditorEitherWrapper,
    ) : BaseClassInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addClassBySchedule(
            classModel: Class,
            schedule: BaseSchedule?,
            targetDay: DayOfNumberedWeek
        ) = eitherWrapper.wrap {
            val createClassModel = classModel.copy(uid = randomUUID())
            val currentDate = dateManager.fetchBeginningCurrentInstant()
            val mondayDate = currentDate.dateOfWeekDay(DayOfWeek.MONDAY)

            return@wrap if (schedule != null) {
                val actualClasses = schedule.classes.toMutableList().apply {
                    add(createClassModel)
                }
                if (mondayDate.equalsDay(schedule.dateVersion.from)) {
                    val updatedSchedule = schedule.copy(classes = actualClasses)
                    scheduleRepository.addOrUpdateSchedule(updatedSchedule, targetUser)
                } else {
                    val deprecatedVersion = schedule.dateVersion.makeDeprecated(currentDate)
                    val deprecatedSchedule = schedule.copy(dateVersion = deprecatedVersion)
                    scheduleRepository.addOrUpdateSchedule(deprecatedSchedule, targetUser)

                    val actualVersion = DateVersion.createNewVersion(currentDate)
                    val actualSchedule = schedule.copy(
                        uid = randomUUID(),
                        dateVersion = actualVersion,
                        classes = actualClasses,
                    )
                    scheduleRepository.addOrUpdateSchedule(actualSchedule, targetUser).let {
                        return@let createClassModel.uid
                    }
                }
            } else {
                val createSchedule = BaseSchedule.createActual(
                    currentDate = currentDate,
                    dayOfNumberedWeek = targetDay,
                    classes = listOf(createClassModel),
                )
                scheduleRepository.addOrUpdateSchedule(createSchedule, targetUser).let {
                    createClassModel.uid
                }
            }.apply {
                updateReminderServices(currentDate, targetDay)
            }
        }

        override suspend fun fetchClass(classId: UID, scheduleId: UID) = eitherWrapper.wrapFlow {
            scheduleRepository.fetchClassById(classId, scheduleId, targetUser)
        }

        override suspend fun updateClassBySchedule(classModel: Class, schedule: BaseSchedule) = eitherWrapper.wrap {
            val classId = classModel.uid
            val currentDate = dateManager.fetchBeginningCurrentInstant()
            val mondayDate = currentDate.dateOfWeekDay(DayOfWeek.MONDAY)

            val updatedClassId: UID
            val oldModel = schedule.classes.find { it.uid == classId }
            val actualClasses = schedule.classes.toMutableList().apply {
                if (classModel.subject?.uid != oldModel?.subject?.uid ||
                    classModel.organization.uid != oldModel?.organization?.uid
                ) {
                    updatedClassId = randomUUID()
                    remove(oldModel)
                    add(classModel.copy(uid = updatedClassId))
                } else {
                    updatedClassId = classId
                    set(indexOf(oldModel), classModel)
                }
            }

            return@wrap if (mondayDate.equalsDay(schedule.dateVersion.from)) {
                val updatedSchedule = schedule.copy(classes = actualClasses)
                scheduleRepository.addOrUpdateSchedule(updatedSchedule, targetUser)
            } else {
                val deprecatedVersion = schedule.dateVersion.makeDeprecated(currentDate)
                val deprecatedSchedule = schedule.copy(dateVersion = deprecatedVersion)
                scheduleRepository.addOrUpdateSchedule(deprecatedSchedule, targetUser)

                val actualVersion = DateVersion.createNewVersion(currentDate)
                val actualSchedule = schedule.copy(
                    uid = randomUUID(),
                    dateVersion = actualVersion,
                    classes = actualClasses,
                )
                scheduleRepository.addOrUpdateSchedule(actualSchedule, targetUser).let {
                    return@let updatedClassId
                }
            }.apply {
                val targetDay = DayOfNumberedWeek(schedule.dayOfWeek, schedule.week)
                updateReminderServices(currentDate, targetDay)
            }
        }

        override suspend fun deleteClassBySchedule(uid: UID, schedule: BaseSchedule) = eitherWrapper.wrapUnit {
            val currentDate = dateManager.fetchBeginningCurrentInstant()
            val mondayDate = currentDate.dateOfWeekDay(DayOfWeek.MONDAY)

            val actualClasses = schedule.classes.toMutableList().apply {
                removeAll { it.uid == uid }
            }

            if (mondayDate.equalsDay(schedule.dateVersion.from)) {
                val updatedSchedule = schedule.copy(classes = actualClasses)
                scheduleRepository.addOrUpdateSchedule(updatedSchedule, targetUser)
            } else {
                val deprecatedVersion = schedule.dateVersion.makeDeprecated(currentDate)
                val deprecatedSchedule = schedule.copy(dateVersion = deprecatedVersion)
                scheduleRepository.addOrUpdateSchedule(deprecatedSchedule, targetUser)

                val actualVersion = DateVersion.createNewVersion(currentDate)
                val actualSchedule = schedule.copy(
                    uid = randomUUID(),
                    dateVersion = actualVersion,
                    classes = actualClasses,
                )
                scheduleRepository.addOrUpdateSchedule(actualSchedule, targetUser)
            }.apply {
                val targetDay = DayOfNumberedWeek(schedule.dayOfWeek, schedule.week)
                updateReminderServices(currentDate, targetDay)
            }
        }

        private suspend fun updateReminderServices(currentDate: Instant, targetDay: DayOfNumberedWeek) {
            val calendarSettings = calendarSettingsRepository.fetchSettings(targetUser).first()
            val notificationSettings = notificationSettingsRepository.fetchSettings(targetUser).first()

            val currentWeek = currentDate.dateTime().date.numberOfRepeatWeek(calendarSettings.numberOfWeek)
            val currentWeekDay = currentDate.dateTime().dayOfWeek

            if (currentWeek == targetDay.week && currentWeekDay == targetDay.dayOfWeek) {
                if (notificationSettings.beginningOfClasses != null) {
                    startClassesReminderManager.startOrRetryReminderService()
                }
                if (notificationSettings.endOfClasses) {
                    endClassesReminderManager.startOrRetryReminderService()
                }
            }
        }
    }
}