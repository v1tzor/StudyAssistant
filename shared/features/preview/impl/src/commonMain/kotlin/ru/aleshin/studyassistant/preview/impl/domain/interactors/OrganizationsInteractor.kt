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

import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.preview.impl.domain.common.PreviewEitherWrapper
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
internal interface OrganizationsInteractor {

    suspend fun addOrUpdateOrganization(organization: Organization): DomainResult<PreviewFailures, UID>
    suspend fun fetchAllOrganization(): FlowDomainResult<PreviewFailures, List<Organization>>
    suspend fun uploadAvatar(uid: UID, file: File): DomainResult<PreviewFailures, String>
    suspend fun deleteAvatar(uid: UID): UnitDomainResult<PreviewFailures>

    class Base(
        private val organizationsRepository: OrganizationsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: PreviewEitherWrapper,
    ) : OrganizationsInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addOrUpdateOrganization(organization: Organization) = eitherWrapper.wrap {
            organizationsRepository.addOrUpdateOrganization(organization, targetUser)
        }

        override suspend fun fetchAllOrganization() = eitherWrapper.wrapFlow {
            organizationsRepository.fetchAllOrganization(targetUser).map { organizations ->
                organizations.sortedByDescending { it.isMain }
            }
        }

        override suspend fun uploadAvatar(uid: UID, file: File) = eitherWrapper.wrap {
            organizationsRepository.uploadAvatar(uid, file, targetUser)
        }

        override suspend fun deleteAvatar(uid: UID) = eitherWrapper.wrap {
            organizationsRepository.deleteAvatar(uid, targetUser)
        }
    }
}