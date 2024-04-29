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

package database.organizations

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import functional.UID
import kotlinx.coroutines.flow.Flow
import managers.CoroutineManager
import randomUUID
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationEntity
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
interface OrganizationsLocalDataSource {

    suspend fun fetchAllOrganization(targetUser: UID): Flow<List<OrganizationEntity>>

    suspend fun fetchOrganizationById(uid: UID): Flow<OrganizationEntity>

    suspend fun addOrUpdateOrganization(organization: OrganizationEntity): UID

    class Base(
        private val organizationQueries: OrganizationQueries,
        private val coroutineManager: CoroutineManager,
    ) : OrganizationsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun fetchAllOrganization(targetUser: UID): Flow<List<OrganizationEntity>> {
            return organizationQueries.fetchAllOrganizations().asFlow().mapToList(coroutineContext)
        }

        override suspend fun fetchOrganizationById(uid: UID): Flow<OrganizationEntity> {
            return organizationQueries.fetchById(uid).asFlow().mapToOne(coroutineContext)
        }

        override suspend fun addOrUpdateOrganization(organization: OrganizationEntity): UID {
            val uid = randomUUID()
            organizationQueries.addOrUpdateOrganization(organization)
            return uid
        }
    }
}