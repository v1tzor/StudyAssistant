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
import entities.schedules.BaseSchedule
import entities.schedules.DateVersion
import extensions.dateOfWeekDay
import extensions.isCurrentDay
import functional.DomainResult
import functional.FlowDomainResult
import functional.UID
import functional.UnitDomainResult
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.datetime.DayOfWeek
import managers.DateManager
import randomUUID
import repositories.BaseScheduleRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 30.05.2024.
 */
internal interface BaseClassInteractor {

    suspend fun addClass(classModel: Class, weekDay: DayOfNumberedWeek): DomainResult<EditorFailures, UID>
    suspend fun fetchClass(classId: UID, scheduleId: UID): FlowDomainResult<EditorFailures, Class?>
    suspend fun updateClass(classModel: Class): DomainResult<EditorFailures, UID>
    suspend fun deleteClass(targetClass: Class): UnitDomainResult<EditorFailures>

    class Base(
        private val scheduleRepository: BaseScheduleRepository,
        private val usersRepository: UsersRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: EditorEitherWrapper,
    ) : BaseClassInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addClass(classModel: Class, weekDay: DayOfNumberedWeek) = eitherWrapper.wrap {
            val scheduleId = classModel.scheduleId
            val createClassModel = classModel.copy(uid = randomUUID())
            val classSchedule = scheduleRepository.fetchScheduleById(scheduleId, targetUser).firstOrNull()
            val currentDate = dateManager.fetchBeginningCurrentDay()
            val mondayDate = currentDate.dateOfWeekDay(DayOfWeek.MONDAY)

            if (scheduleId.isNotEmpty() && classSchedule != null) {
                val actualClasses = classSchedule.classes.toMutableList().apply {
                    add(createClassModel)
                }
                if (mondayDate.isCurrentDay(classSchedule.dateVersion.from)) {
                    val updatedSchedule = classSchedule.copy(classes = actualClasses)
                    scheduleRepository.addOrUpdateSchedule(updatedSchedule, targetUser)
                } else {
                    val deprecatedVersion = classSchedule.dateVersion.makeDeprecated(currentDate)
                    val deprecatedSchedule = classSchedule.copy(dateVersion = deprecatedVersion)
                    scheduleRepository.addOrUpdateSchedule(deprecatedSchedule, targetUser)

                    val actualVersion = DateVersion.createNewVersion(currentDate)
                    val actualSchedule = classSchedule.copy(
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

        override suspend fun updateClass(classModel: Class) = eitherWrapper.wrap {
            val scheduleId = classModel.scheduleId
            val classSchedule = scheduleRepository.fetchScheduleById(scheduleId, targetUser).firstOrNull()
            val currentDate = dateManager.fetchBeginningCurrentDay()
            val mondayDate = currentDate.dateOfWeekDay(DayOfWeek.MONDAY)

            if (scheduleId.isNotEmpty() && classSchedule != null) {
                val createClassId: UID
                val classId = classModel.uid
                val oldModel = scheduleRepository.fetchClassById(classId, scheduleId, targetUser).firstOrNull()
                val actualClasses = classSchedule.classes.toMutableList().apply {
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
                if (mondayDate.isCurrentDay(classSchedule.dateVersion.from)) {
                    val updatedSchedule = classSchedule.copy(classes = actualClasses)
                    scheduleRepository.addOrUpdateSchedule(updatedSchedule, targetUser)
                } else {
                    val deprecatedVersion = classSchedule.dateVersion.makeDeprecated(currentDate)
                    val deprecatedSchedule = classSchedule.copy(dateVersion = deprecatedVersion)
                    scheduleRepository.addOrUpdateSchedule(deprecatedSchedule, targetUser)

                    val actualVersion = DateVersion.createNewVersion(currentDate)
                    val actualSchedule = classSchedule.copy(
                        uid = randomUUID(),
                        dateVersion = actualVersion,
                        classes = actualClasses,
                    )
                    scheduleRepository.addOrUpdateSchedule(actualSchedule, targetUser).let {
                        return@let createClassId
                    }
                }
            } else {
                throw NullPointerException("Base schedule is null")
            }
        }

        override suspend fun deleteClass(targetClass: Class) = eitherWrapper.wrapUnit {
            val scheduleId = targetClass.scheduleId
            val classSchedule = scheduleRepository.fetchScheduleById(scheduleId, targetUser).firstOrNull()
            val currentDate = dateManager.fetchBeginningCurrentDay()
            val mondayDate = currentDate.dateOfWeekDay(DayOfWeek.MONDAY)

            if (scheduleId.isNotEmpty() && classSchedule != null) {
                val actualClasses = classSchedule.classes.toMutableList().apply {
                    remove(targetClass)
                }
                if (mondayDate.isCurrentDay(classSchedule.dateVersion.from)) {
                    val updatedSchedule = classSchedule.copy(classes = actualClasses)
                    scheduleRepository.addOrUpdateSchedule(updatedSchedule, targetUser)
                } else {
                    val deprecatedVersion = classSchedule.dateVersion.makeDeprecated(currentDate)
                    val deprecatedSchedule = classSchedule.copy(dateVersion = deprecatedVersion)
                    scheduleRepository.addOrUpdateSchedule(deprecatedSchedule, targetUser)

                    val actualVersion = DateVersion.createNewVersion(currentDate)
                    val actualSchedule = classSchedule.copy(
                        uid = randomUUID(),
                        dateVersion = actualVersion,
                        classes = actualClasses,
                    )
                    scheduleRepository.addOrUpdateSchedule(actualSchedule, targetUser)
                }
            } else {
                throw NullPointerException("Base schedule is null")
            }
        }
    }
}
