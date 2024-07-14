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

import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
internal interface CustomClassInteractor {

    suspend fun addClassBySchedule(classModel: Class, schedule: CustomSchedule): DomainResult<EditorFailures, UID>
    suspend fun fetchClass(classId: UID, scheduleId: UID): FlowDomainResult<EditorFailures, Class?>
    suspend fun updateClassBySchedule(classModel: Class, schedule: CustomSchedule): DomainResult<EditorFailures, UID>
    suspend fun deleteClassBySchedule(targetClass: Class, schedule: CustomSchedule): UnitDomainResult<EditorFailures>

    class Base(
        private val customScheduleRepository: CustomScheduleRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : CustomClassInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addClassBySchedule(
            classModel: Class,
            schedule: CustomSchedule,
        ) = eitherWrapper.wrap {
            val createClassModel = classModel.copy(uid = randomUUID())
            val updatedClasses = schedule.classes.toMutableList().apply { add(createClassModel) }
            val updatedSchedule = schedule.copy(classes = updatedClasses)

            customScheduleRepository.addOrUpdateSchedule(updatedSchedule, targetUser).let {
                createClassModel.uid
            }
        }

        override suspend fun fetchClass(classId: UID, scheduleId: UID) = eitherWrapper.wrapFlow {
            customScheduleRepository.fetchClassById(classId, scheduleId, targetUser)
        }

        override suspend fun updateClassBySchedule(
            classModel: Class,
            schedule: CustomSchedule,
        ) = eitherWrapper.wrap {
            val updatedClassId: UID
            val oldModel = schedule.classes.find { it.uid == classModel.uid }
            val updatedClasses = schedule.classes.toMutableList().apply {
                if (classModel.subject?.uid != oldModel?.subject?.uid ||
                    classModel.organization.uid != oldModel?.organization?.uid
                ) {
                    updatedClassId = randomUUID()
                    remove(oldModel)
                    add(classModel.copy(uid = updatedClassId))
                } else {
                    updatedClassId = classModel.uid
                    set(indexOf(oldModel), classModel)
                }
            }
            val updatedSchedule = schedule.copy(classes = updatedClasses)

            customScheduleRepository.addOrUpdateSchedule(updatedSchedule, targetUser).let {
                updatedClassId
            }
        }

        override suspend fun deleteClassBySchedule(
            targetClass: Class,
            schedule: CustomSchedule,
        ) = eitherWrapper.wrapUnit {
            val updatedClasses = schedule.classes.toMutableList().apply { remove(targetClass) }
            val updatedSchedule = schedule.copy(classes = updatedClasses)

            customScheduleRepository.addOrUpdateSchedule(updatedSchedule, targetUser)
        }
    }
}