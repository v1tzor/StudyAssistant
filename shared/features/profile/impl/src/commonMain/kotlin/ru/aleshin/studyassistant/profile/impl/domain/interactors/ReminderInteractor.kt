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

package ru.aleshin.studyassistant.profile.impl.domain.interactors

import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.managers.reminders.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.reminders.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.profile.impl.domain.common.ProfileEitherWrapper
import ru.aleshin.studyassistant.profile.impl.domain.entities.ProfileFailures

/**
 * @author Stanislav Aleshin on 24.08.2024.
 */
internal interface ReminderInteractor {

    suspend fun stopReminders(): UnitDomainResult<ProfileFailures>

    class Base(
        private val startClassesReminderManager: StartClassesReminderManager,
        private val endClassesReminderManager: EndClassesReminderManager,
        private val homeworksReminderManager: HomeworksReminderManager,
        private val workloadWarningManager: WorkloadWarningManager,
        private val organizationsRepository: OrganizationsRepository,
        private val eitherWrapper: ProfileEitherWrapper,
    ) : ReminderInteractor {

        override suspend fun stopReminders() = eitherWrapper.wrapUnit {
            val allOrganizations = organizationsRepository.fetchAllShortOrganization().first()
            val organizationIds = allOrganizations.map { it.uid }

            startClassesReminderManager.stopReminderService(organizationIds)
            endClassesReminderManager.stopReminderService(organizationIds)

            homeworksReminderManager.stopReminderService()
            workloadWarningManager.stopWarningService()
        }
    }
}