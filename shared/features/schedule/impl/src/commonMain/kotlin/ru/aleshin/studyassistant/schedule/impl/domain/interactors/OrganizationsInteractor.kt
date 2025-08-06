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

package ru.aleshin.studyassistant.schedule.impl.domain.interactors

import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures

/**
 * @author Stanislav Aleshin on 16.08.2024.
 */
internal interface OrganizationsInteractor {

    suspend fun addOrUpdateOrganizationsData(organizations: List<Organization>): UnitDomainResult<ScheduleFailures>
    suspend fun fetchAllShortOrganizations(): FlowDomainResult<ScheduleFailures, List<OrganizationShort>>
    suspend fun fetchOrganizationById(uid: UID): FlowDomainResult<ScheduleFailures, Organization>

    class Base(
        private val organizationsRepository: OrganizationsRepository,
        private val subjectsRepository: SubjectsRepository,
        private val employeeRepository: EmployeeRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: ScheduleEitherWrapper,
    ) : OrganizationsInteractor {

        override suspend fun addOrUpdateOrganizationsData(organizations: List<Organization>) = eitherWrapper.wrapUnit {
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()

            val organizations = organizations.map { it.copy(updatedAt = updatedAt) }
            val subjects = organizations.map { it.subjects }.extractAllItem().map { it.copy(updatedAt = updatedAt) }
            val employees = organizations.map { it.employee }.extractAllItem().map { it.copy(updatedAt = updatedAt) }

            organizationsRepository.addOrUpdateOrganizationsGroup(organizations)
            employeeRepository.addOrUpdateEmployeeGroup(employees)
            subjectsRepository.addOrUpdateSubjectsGroup(subjects)
        }

        override suspend fun fetchAllShortOrganizations() = eitherWrapper.wrapFlow {
            organizationsRepository.fetchAllShortOrganization().map { organizations ->
                organizations.sortedByDescending { it.isMain }
            }
        }

        override suspend fun fetchOrganizationById(uid: UID) = eitherWrapper.wrapFlow {
            organizationsRepository.fetchOrganizationById(uid).map { organization ->
                checkNotNull(organization)
            }
        }
    }
}