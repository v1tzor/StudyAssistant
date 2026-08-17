/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.settings.impl.domain.interactors

import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.settings.CalendarSettings
import ru.aleshin.studyassistant.core.domain.entities.settings.NotificationSettings
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UserDataResetRepository
import ru.aleshin.studyassistant.settings.impl.domain.common.SettingsEitherWrapper
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
internal interface UserDataInteractor {

    suspend fun deleteCurrentSchedule(): UnitDomainResult<SettingsFailures>
    suspend fun deleteAllUserData(): UnitDomainResult<SettingsFailures>

    class Base(
        private val userDataResetRepository: UserDataResetRepository,
        private val generalSettingsRepository: GeneralSettingsRepository,
        private val notificationSettingsRepository: NotificationSettingsRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val startClassesReminderManager: StartClassesReminderManager,
        private val endClassesReminderManager: EndClassesReminderManager,
        private val homeworksReminderManager: HomeworksReminderManager,
        private val workloadWarningManager: WorkloadWarningManager,
        private val dateManager: DateManager,
        private val eitherWrapper: SettingsEitherWrapper,
    ) : UserDataInteractor {

        override suspend fun deleteCurrentSchedule() = eitherWrapper.wrapUnit {
            userDataResetRepository.deleteAllSchedules()
            startClassesReminderManager.stopReminderService(emptyList())
            endClassesReminderManager.stopReminderService(emptyList())
        }

        override suspend fun deleteAllUserData() = eitherWrapper.wrapUnit {
            userDataResetRepository.deleteAllUserData()
            val settings = generalSettingsRepository.fetchSettings().first()
            generalSettingsRepository.updateSettings(
                settings.copy(
                    isFirstStart = false,
                    isUnfinishedSetup = null,
                    isSetup = false,
                ),
            )
            notificationSettingsRepository.updateSettings(NotificationSettings())
            calendarSettingsRepository.updateSettings(
                CalendarSettings(
                    holidays = emptyList(),
                    updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
                ),
            )
            startClassesReminderManager.stopReminderService(emptyList())
            endClassesReminderManager.stopReminderService(emptyList())
            homeworksReminderManager.stopReminderService()
            workloadWarningManager.stopWarningService()
        }
    }
}
