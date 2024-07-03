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

import entities.classes.Class
import entities.common.DayOfNumberedWeek
import entities.schedules.DateVersion
import entities.schedules.base.BaseSchedule
import extensions.dateOfWeekDay
import extensions.equalsDay
import extensions.randomUUID
import functional.DomainResult
import functional.FlowDomainResult
import functional.UID
import functional.UnitDomainResult
import kotlinx.datetime.DayOfWeek
import managers.DateManager
import repositories.BaseScheduleRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 30.05.2024.
 */
internal interface BaseClassInteractor {

    suspend fun addClassBySchedule(
        classModel: Class,
        schedule: BaseSchedule?,
        weekDay: DayOfNumberedWeek
    ): DomainResult<EditorFailures, UID>

    suspend fun fetchClass(classId: UID, scheduleId: UID): FlowDomainResult<EditorFailures, Class?>

    suspend fun updateClassBySchedule(classModel: Class, schedule: BaseSchedule): DomainResult<EditorFailures, UID>

    suspend fun deleteClassBySchedule(uid: UID, schedule: BaseSchedule): UnitDomainResult<EditorFailures>

    class Base(
        private val scheduleRepository: BaseScheduleRepository,
        private val usersRepository: UsersRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: EditorEitherWrapper,
    ) : BaseClassInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addClassBySchedule(
            classModel: Class,
            schedule: BaseSchedule?,
            weekDay: DayOfNumberedWeek
        ) = eitherWrapper.wrap {
            val createClassModel = classModel.copy(uid = randomUUID())
            val currentDate = dateManager.fetchBeginningCurrentInstant()
            val mondayDate = currentDate.dateOfWeekDay(DayOfWeek.MONDAY)

            if (schedule != null) {
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
                    dayOfNumberedWeek = weekDay,
                    classes = listOf(createClassModel),
                )
                scheduleRepository.addOrUpdateSchedule(createSchedule, targetUser).let {
                    createClassModel.uid
                }
            }
        }

        override suspend fun fetchClass(classId: UID, scheduleId: UID) = eitherWrapper.wrapFlow {
            scheduleRepository.fetchClassById(classId, scheduleId, targetUser)
        }

        override suspend fun updateClassBySchedule(classModel: Class, schedule: BaseSchedule) = eitherWrapper.wrap {
            val classId = classModel.uid
            val currentDate = dateManager.fetchBeginningCurrentInstant()
            val mondayDate = currentDate.dateOfWeekDay(DayOfWeek.MONDAY)

            val createClassId: UID
            val oldModel = schedule.classes.find { it.uid == classId }
            val actualClasses = schedule.classes.toMutableList().apply {
                if (classModel.subject?.uid != oldModel?.subject?.uid ||
                    classModel.organization.uid != oldModel?.organization?.uid
                ) {
                    createClassId = randomUUID()
                    remove(oldModel)
                    add(classModel.copy(uid = createClassId))
                } else {
                    createClassId = classId
                    set(indexOf(oldModel), classModel)
                }
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
                    return@let createClassId
                }
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
            }
        }
    }
}