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

package ru.aleshin.studyassistant.info.impl.domain.interactors

import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.info.impl.domain.common.InfoEitherWrapper
import ru.aleshin.studyassistant.info.impl.domain.entities.InfoFailures

/**
 * @author Stanislav Aleshin on 30.05.2024.
 */
internal interface OrganizationsInteractor {

    suspend fun fetchOrganizationById(organizationId: UID): FlowDomainResult<InfoFailures, Organization?>
    suspend fun fetchAllShortOrganizations(): FlowDomainResult<InfoFailures, List<OrganizationShort>>
    suspend fun fetchMainOrFirstOrganization(): FlowDomainResult<InfoFailures, OrganizationShort?>

    class Base(
        private val organizationsRepository: OrganizationsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: InfoEitherWrapper,
    ) : OrganizationsInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchOrganizationById(organizationId: UID) = eitherWrapper.wrapFlow {
            organizationsRepository.fetchOrganizationById(organizationId, targetUser)
        }

        override suspend fun fetchAllShortOrganizations() = eitherWrapper.wrapFlow {
            organizationsRepository.fetchAllShortOrganization(targetUser)
        }

        override suspend fun fetchMainOrFirstOrganization() = eitherWrapper.wrapFlow {
            organizationsRepository.fetchAllShortOrganization(targetUser).map { organizations ->
                organizations.find { it.isMain } ?: organizations.getOrNull(0)
            }
        }
    }
}