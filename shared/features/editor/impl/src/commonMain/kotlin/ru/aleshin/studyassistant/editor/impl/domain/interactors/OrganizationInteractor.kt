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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.entities.organizations.convertToBase
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
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
    suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile): DomainResult<EditorFailures, String>
    suspend fun deleteAvatar(avatarUrl: String): UnitDomainResult<EditorFailures>

    class Base(
        private val organizationsRepository: OrganizationsRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: EditorEitherWrapper,
    ) : OrganizationInteractor {

        override suspend fun addOrUpdateOrganization(organization: Organization) = eitherWrapper.wrap {
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val updatedOrganization = organization.copy(updatedAt = updatedAt)
            organizationsRepository.addOrUpdateOrganization(updatedOrganization)
        }

        override suspend fun fetchOrganizationById(uid: UID) = eitherWrapper.wrapFlow {
            organizationsRepository.fetchOrganizationById(uid).map { organization ->
                checkNotNull(organization)
            }
        }

        override suspend fun fetchShortOrganizationById(uid: UID) = eitherWrapper.wrapFlow {
            organizationsRepository.fetchShortOrganizationById(uid).map { organization ->
                checkNotNull(organization)
            }
        }

        override suspend fun fetchAllShortOrganizations() = eitherWrapper.wrapFlow {
            organizationsRepository.fetchAllShortOrganization().map { organizations ->
                organizations.sortedByDescending { it.isMain }
            }
        }

        override suspend fun updateShortOrganization(organization: OrganizationShort) = eitherWrapper.wrapUnit {
            val baseUid = organization.uid
            val baseModel = organizationsRepository.fetchOrganizationById(baseUid).first()
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val updatedModel = organization.convertToBase(checkNotNull(baseModel)).copy(updatedAt = updatedAt)

            organizationsRepository.addOrUpdateOrganization(updatedModel)
        }

        override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile) = eitherWrapper.wrap {
            organizationsRepository.uploadAvatar(oldAvatarUrl, file)
        }
        override suspend fun deleteAvatar(avatarUrl: String) = eitherWrapper.wrap {
            organizationsRepository.deleteAvatar(avatarUrl)
        }
    }
}