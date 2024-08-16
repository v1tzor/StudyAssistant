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

package ru.aleshin.studyassistant.core.domain.repositories

import dev.gitlive.firebase.storage.File
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface OrganizationsRepository {
    suspend fun addOrUpdateOrganization(organization: Organization, targetUser: UID): UID
    suspend fun addOrUpdateOrganizationsGroup(organizations: List<Organization>, targetUser: UID)
    suspend fun uploadAvatar(uid: UID, file: File, targetUser: UID): String
    suspend fun fetchOrganizationById(uid: UID, targetUser: UID): Flow<Organization?>
    suspend fun fetchOrganizationsById(uid: List<UID>, targetUser: UID): Flow<List<Organization>>
    suspend fun fetchShortOrganizationById(uid: UID, targetUser: UID): Flow<OrganizationShort?>
    suspend fun fetchAllOrganization(targetUser: UID): Flow<List<Organization>>
    suspend fun fetchAllShortOrganization(targetUser: UID): Flow<List<OrganizationShort>>
    suspend fun deleteAvatar(uid: UID, targetUser: UID)
}