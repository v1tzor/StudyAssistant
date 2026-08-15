/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.organizations.mapToLocalData
import ru.aleshin.studyassistant.core.database.datasource.avatar.AvatarLocalDataSource
import ru.aleshin.studyassistant.core.database.datasource.avatar.AvatarType
import ru.aleshin.studyassistant.core.database.datasource.organizations.OrganizationsLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
class OrganizationsRepositoryImpl(
    private val localDataSource: OrganizationsLocalDataSource,
    private val avatarLocalDataSource: AvatarLocalDataSource,
) : OrganizationsRepository {

    override suspend fun addOrUpdateOrganization(organization: Organization): UID {
        val updatedOrganization = organization.copy(uid = organization.uid.ifBlank { randomUUID() })
        localDataSource.addOrUpdateOrganization(updatedOrganization.mapToLocalData())
        return updatedOrganization.uid
    }

    override suspend fun addOrUpdateOrganizationsGroup(organizations: List<Organization>) {
        val updatedOrganizations = organizations.map { organization ->
            organization.copy(uid = organization.uid.ifBlank { randomUUID() })
        }
        localDataSource.addOrUpdateOrganizations(updatedOrganizations.map { it.mapToLocalData() })
    }

    override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile): String {
        val avatar = avatarLocalDataSource.saveAvatar(AvatarType.ORGANIZATION, file)
        if (oldAvatarUrl != null && oldAvatarUrl != avatar) {
            avatarLocalDataSource.deleteAvatar(oldAvatarUrl)
        }
        return avatar
    }

    override suspend fun fetchOrganizationById(uid: UID): Flow<Organization?> {
        return localDataSource.fetchOrganizationDetailsById(uid).map { organization ->
            organization?.mapToDomain()
        }
    }

    override suspend fun fetchOrganizationsById(uid: List<UID>): Flow<List<Organization>> {
        return localDataSource.fetchOrganizationsDetailsById(uid).map { organizations ->
            organizations.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchShortOrganizationById(uid: UID): Flow<OrganizationShort?> {
        return localDataSource.fetchShortOrganizationById(uid).map { organization ->
            organization?.mapToDomain()
        }
    }

    override suspend fun fetchAllOrganization(): Flow<List<Organization>> {
        return localDataSource.fetchAllOrganizationDetails().map { organizations ->
            organizations.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchAllShortOrganization(): Flow<List<OrganizationShort>> {
        return localDataSource.fetchAllShortOrganization().map { organizations ->
            organizations.map { it.mapToDomain() }
        }
    }

    override suspend fun deleteAvatar(avatarUrl: String) {
        avatarLocalDataSource.deleteAvatar(avatarUrl)
    }
}
