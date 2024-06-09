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

import entities.organizations.Organization
import entities.organizations.OrganizationShort
import entities.organizations.convertToBase
import entities.organizations.convertToShort
import functional.FlowDomainResult
import functional.UID
import functional.UnitDomainResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import repositories.OrganizationsRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 30.05.2024.
 */
internal interface OrganizationInteractor {

    suspend fun fetchOrganizationById(uid: UID): FlowDomainResult<EditorFailures, Organization>
    suspend fun fetchAllOrganizations(): FlowDomainResult<EditorFailures, List<Organization>>

    suspend fun fetchAllShortOrganizations(): FlowDomainResult<EditorFailures, List<OrganizationShort>>

    suspend fun updateShortOrganization(organization: OrganizationShort): UnitDomainResult<EditorFailures>

    class Base(
        private val organizationsRepository: OrganizationsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : OrganizationInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchOrganizationById(uid: UID) = eitherWrapper.wrapFlow {
            organizationsRepository.fetchOrganizationById(uid, targetUser)
        }

        override suspend fun fetchAllOrganizations() = eitherWrapper.wrapFlow {
            organizationsRepository.fetchAllOrganization(targetUser)
        }

        override suspend fun fetchAllShortOrganizations() = eitherWrapper.wrapFlow {
            organizationsRepository.fetchAllOrganization(targetUser).map { organizations ->
                organizations.map { it.convertToShort() }
            }
        }

        override suspend fun updateShortOrganization(organization: OrganizationShort) = eitherWrapper.wrapUnit {
            val baseUid = organization.uid
            val baseModel = organizationsRepository.fetchOrganizationById(baseUid, targetUser).first()
            val updatedModel = organization.convertToBase(baseModel)

            organizationsRepository.addOrUpdateOrganization(updatedModel, targetUser)
        }
    }
}
