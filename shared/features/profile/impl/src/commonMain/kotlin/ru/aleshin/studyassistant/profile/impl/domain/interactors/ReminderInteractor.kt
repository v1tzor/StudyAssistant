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
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.managers.EndClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.HomeworksReminderManager
import ru.aleshin.studyassistant.core.domain.managers.StartClassesReminderManager
import ru.aleshin.studyassistant.core.domain.managers.WorkloadWarningManager
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
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
        private val usersRepository: UsersRepository,
        private val eitherWrapper: ProfileEitherWrapper,
    ) : ReminderInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun stopReminders() = eitherWrapper.wrapUnit {
            val allOrganizations = organizationsRepository.fetchAllShortOrganization(targetUser).first()
            val organizationIds = allOrganizations.map { it.uid }

            startClassesReminderManager.stopReminderService(organizationIds)
            endClassesReminderManager.stopReminderService(organizationIds)

            homeworksReminderManager.stopReminderService()
            workloadWarningManager.stopWarningService()
        }
    }
}