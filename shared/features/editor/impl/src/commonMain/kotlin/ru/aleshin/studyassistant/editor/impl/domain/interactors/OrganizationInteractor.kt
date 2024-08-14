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

import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.entities.organizations.convertToBase
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 30.05.2024.
 */
internal interface OrganizationInteractor {

    suspend fun addOrUpdateOrganization(organization: Organization): DomainResult<EditorFailures, UID>
    suspend fun fetchOrganizationById(uid: UID): FlowDomainResult<EditorFailures, Organization>
    suspend fun fetchShortOrganizationById(uid: UID): FlowDomainResult<EditorFailures, OrganizationShort>
    suspend fun fetchAllShortOrganizations(): FlowDomainResult<EditorFailures, List<OrganizationShort>>
    suspend fun updateShortOrganization(organization: OrganizationShort): UnitDomainResult<EditorFailures>
    suspend fun uploadAvatar(uid: UID, file: File): DomainResult<EditorFailures, String>
    suspend fun deleteAvatar(uid: UID): UnitDomainResult<EditorFailures>

    class Base(
        private val organizationsRepository: OrganizationsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : OrganizationInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addOrUpdateOrganization(organization: Organization) = eitherWrapper.wrap {
            organizationsRepository.addOrUpdateOrganization(organization, targetUser)
        }

        override suspend fun fetchOrganizationById(uid: UID) = eitherWrapper.wrapFlow {
            organizationsRepository.fetchOrganizationById(uid, targetUser).map { organization ->
                checkNotNull(organization)
            }
        }

        override suspend fun fetchShortOrganizationById(uid: UID) = eitherWrapper.wrapFlow {
            organizationsRepository.fetchShortOrganizationById(uid, targetUser).map { organization ->
                checkNotNull(organization)
            }
        }

        override suspend fun fetchAllShortOrganizations() = eitherWrapper.wrapFlow {
            organizationsRepository.fetchAllShortOrganization(targetUser).map { organizations ->
                organizations.sortedByDescending { it.isMain }
            }
        }

        override suspend fun updateShortOrganization(organization: OrganizationShort) = eitherWrapper.wrapUnit {
            val baseUid = organization.uid
            val baseModel = organizationsRepository.fetchOrganizationById(baseUid, targetUser).first()
            val updatedModel = organization.convertToBase(checkNotNull(baseModel))

            organizationsRepository.addOrUpdateOrganization(updatedModel, targetUser)
        }

        override suspend fun uploadAvatar(uid: UID, file: File) = eitherWrapper.wrap {
            organizationsRepository.uploadAvatar(uid, file, targetUser)
        }
        override suspend fun deleteAvatar(uid: UID) = eitherWrapper.wrap {
            organizationsRepository.deleteAvatar(uid, targetUser)
        }
    }
}