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

package ru.aleshin.studyassistant.preview.impl.domain.interactors

import entities.organizations.Organization
import functional.DomainResult
import functional.FlowDomainResult
import functional.UID
import functional.UnitDomainResult
import repositories.OrganizationsRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.preview.impl.domain.common.PreviewEitherWrapper
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
internal interface OrganizationsInteractor {

    suspend fun fetchAllOrganization(): FlowDomainResult<PreviewFailures, List<Organization>>

    suspend fun createOrUpdateOrganization(organization: Organization): DomainResult<PreviewFailures, UID>

    class Base(
        private val organizationsRepository: OrganizationsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: PreviewEitherWrapper,
    ) : OrganizationsInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchAllOrganization() = eitherWrapper.wrapFlow {
            organizationsRepository.fetchAllOrganization(targetUser)
        }

        override suspend fun createOrUpdateOrganization(organization: Organization) = eitherWrapper.wrap {
            organizationsRepository.addOrUpdateOrganization(organization, targetUser)
        }
    }
}