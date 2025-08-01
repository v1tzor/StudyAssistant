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

package ru.aleshin.studyassistant.settings.impl.domain.interactors

import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkResult
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.settings.NotificationSettings
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.settings.impl.domain.common.SettingsEitherWrapper
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
internal interface NotificationSettingsInteractor {

    suspend fun fetchSettings(): FlowWorkResult<SettingsFailures, NotificationSettings>
    suspend fun updateSettings(settings: NotificationSettings): UnitDomainResult<SettingsFailures>

    class Base(
        private val settingsRepository: NotificationSettingsRepository,
        private val startClassesReminderManager: StartClassesReminderManager,
        private val endClassesReminderManager: EndClassesReminderManager,
        private val homeworksReminderManager: HomeworksReminderManager,
        private val workloadWarningManager: WorkloadWarningManager,
        private val organizationsRepository: OrganizationsRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: SettingsEitherWrapper,
    ) : NotificationSettingsInteractor {

        override suspend fun fetchSettings() = eitherWrapper.wrapFlow {
            settingsRepository.fetchSettings()
        }

        override suspend fun updateSettings(settings: NotificationSettings) = eitherWrapper.wrapUnit {
            val allOrganizations = organizationsRepository.fetchAllShortOrganization().first()
            val organizationIds = allOrganizations.map { it.uid }

            settingsRepository.updateSettings(settings)

            if (settings.beginningOfClasses != null) {
                startClassesReminderManager.startOrRetryReminderService()
            } else {
                startClassesReminderManager.stopReminderService(organizationIds)
            }
            if (settings.endOfClasses) {
                endClassesReminderManager.startOrRetryReminderService()
            } else {
                endClassesReminderManager.stopReminderService(organizationIds)
            }
            if (settings.unfinishedHomeworks != null) {
                val currentTime = dateManager.fetchCurrentInstant()
                val targetDateTime = LocalDateTime(
                    date = currentTime.dateTime().date,
                    time = LocalTime.fromMillisecondOfDay(settings.unfinishedHomeworks?.toInt() ?: 0)
                )
                val targetInstant = targetDateTime.toInstant(TimeZone.currentSystemDefault())
                homeworksReminderManager.startOrRetryReminderService(targetInstant)
            } else {
                homeworksReminderManager.stopReminderService()
            }
            if (settings.highWorkload != null) {
                workloadWarningManager.startOrRetryWarningService()
            } else {
                workloadWarningManager.stopWarningService()
            }
        }
    }
}