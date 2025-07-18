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
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.managers.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
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
    suspend fun deleteClassBySchedule(uid: UID, schedule: CustomSchedule): UnitDomainResult<EditorFailures>

    class Base(
        private val customScheduleRepository: CustomScheduleRepository,
        private val notificationSettingsRepository: NotificationSettingsRepository,
        private val usersRepository: UsersRepository,
        private val startClassesReminderManager: StartClassesReminderManager,
        private val endClassesReminderManager: EndClassesReminderManager,
        private val eitherWrapper: EditorEitherWrapper,
    ) : CustomClassInteractor {

        override suspend fun addClassBySchedule(
            classModel: Class,
            schedule: CustomSchedule,
        ) = eitherWrapper.wrap {
            val createClassModel = classModel.copy(uid = randomUUID())
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            val updatedClasses = schedule.classes.toMutableList().apply { add(createClassModel) }
            val updatedSchedule = schedule.copy(classes = updatedClasses)

            customScheduleRepository.addOrUpdateSchedule(updatedSchedule, targetUser).apply {
                updateReminderServices()
            }
            return@wrap createClassModel.uid
        }

        override suspend fun fetchClass(classId: UID, scheduleId: UID) = eitherWrapper.wrapFlow {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            customScheduleRepository.fetchClassById(classId, scheduleId, targetUser)
        }

        override suspend fun updateClassBySchedule(
            classModel: Class,
            schedule: CustomSchedule,
        ) = eitherWrapper.wrap {
            val updatedClassId: UID
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
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

            customScheduleRepository.addOrUpdateSchedule(updatedSchedule, targetUser).apply {
                updateReminderServices()
            }
            return@wrap updatedClassId
        }

        override suspend fun deleteClassBySchedule(
            uid: UID,
            schedule: CustomSchedule,
        ) = eitherWrapper.wrapUnit {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            val updatedClasses = schedule.classes.toMutableList().apply { removeAll { it.uid == uid } }
            val updatedSchedule = schedule.copy(classes = updatedClasses)

            customScheduleRepository.addOrUpdateSchedule(updatedSchedule, targetUser).apply {
                updateReminderServices()
            }
        }

        private suspend fun updateReminderServices() {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            val notificationSettings = notificationSettingsRepository.fetchSettings(targetUser).first()
            if (notificationSettings.beginningOfClasses != null) {
                startClassesReminderManager.startOrRetryReminderService()
            }
            if (notificationSettings.endOfClasses) {
                endClassesReminderManager.startOrRetryReminderService()
            }
        }
    }
}