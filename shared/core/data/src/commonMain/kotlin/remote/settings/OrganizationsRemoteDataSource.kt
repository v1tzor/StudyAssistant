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

package remote.settings

import dev.gitlive.firebase.firestore.FirebaseFirestore
import functional.UID
import kotlinx.coroutines.flow.Flow
import models.organizations.OrganizationPojo

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface OrganizationsRemoteDataSource {

    suspend fun fetchAllOrganization(targetUser: UID): Flow<List<OrganizationPojo>>

    suspend fun fetchOrganizationById(uid: UID): Flow<OrganizationPojo>

    suspend fun addOrUpdateOrganization(organization: OrganizationPojo): UID

    class Base(
        private val database: FirebaseFirestore,
    ) : OrganizationsRemoteDataSource {

        override suspend fun fetchAllOrganization(targetUser: UID): Flow<List<OrganizationPojo>> {
            TODO()
        }

        override suspend fun fetchOrganizationById(uid: UID): Flow<OrganizationPojo> {
            TODO()
        }

        override suspend fun addOrUpdateOrganization(organization: OrganizationPojo): UID {
            TODO()
        }
    }
}