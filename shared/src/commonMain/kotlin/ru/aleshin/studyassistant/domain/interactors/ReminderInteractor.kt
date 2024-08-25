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

package ru.aleshin.studyassistant.domain.interactors

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.managers.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.domain.common.MainEitherWrapper
import ru.aleshin.studyassistant.domain.entities.MainFailures

/**
 * @author Stanislav Aleshin on 24.08.2024.
 */
interface ReminderInteractor {

    suspend fun startOrRetryAvailableReminders(): UnitDomainResult<MainFailures>
    suspend fun stopReminders(): UnitDomainResult<MainFailures>

    class Base(
        private val notificationSettingsRepository: NotificationSettingsRepository,
        private val startClassesReminderManager: StartClassesReminderManager,
        private val endClassesReminderManager: EndClassesReminderManager,
        private val homeworksReminderManager: HomeworksReminderManager,
        private val workloadWarningManager: WorkloadWarningManager,
        private val usersRepository: UsersRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: MainEitherWrapper,
    ) : ReminderInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun startOrRetryAvailableReminders() = eitherWrapper.wrapUnit {
            notificationSettingsRepository.fetchSettings(targetUser).first().apply {
                if (beginningOfClasses != null) {
                    startClassesReminderManager.startOrRetryReminderService()
                }
                if (endOfClasses) {
                    endClassesReminderManager.startOrRetryReminderService()
                }
                if (unfinishedHomeworks != null) {
                    val currentTime = dateManager.fetchCurrentInstant()
                    val targetDateTime = LocalDateTime(
                        date = currentTime.dateTime().date,
                        time = LocalTime.fromMillisecondOfDay(unfinishedHomeworks?.toInt() ?: 0)
                    )
                    val targetInstant = targetDateTime.toInstant(TimeZone.currentSystemDefault())
                    homeworksReminderManager.startOrRetryReminderService(targetInstant)
                }
                if (highWorkload != null) {
                    workloadWarningManager.startOrRetryWarningService()
                }
            }
        }

        override suspend fun stopReminders() = eitherWrapper.wrapUnit {
            startClassesReminderManager.stopReminderService()
            endClassesReminderManager.stopReminderService()
            homeworksReminderManager.stopReminderService()
            workloadWarningManager.stopWarningService()
        }
    }
}