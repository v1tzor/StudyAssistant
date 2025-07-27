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

package ru.aleshin.studyassistant.core.database.datasource.user

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.user.convertToDetails
import ru.aleshin.studyassistant.core.database.mappers.user.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.user.mapToEntity
import ru.aleshin.studyassistant.core.database.models.users.AppUserDetailsEntity
import ru.aleshin.studyassistant.core.database.models.users.BaseAppUserEntity
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.sqldelight.user.CurrentUserQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 20.07.2025.
 */
interface UserLocalDataSource : LocalDataSource.FullSynced.SingleDocument<BaseAppUserEntity> {

    suspend fun fetchCurrentUserDetails(): Flow<AppUserDetailsEntity?>

    class Base(
        private val userQuery: CurrentUserQueries,
        private val coroutineManager: CoroutineManager,
    ) : UserLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateItem(item: BaseAppUserEntity) {
            userQuery.addOrUpdateUser(item.mapToEntity()).await()
        }

        override suspend fun fetchItem(): Flow<BaseAppUserEntity?> {
            return userQuery.fetchUser().mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun fetchCurrentUserDetails(): Flow<AppUserDetailsEntity?> {
            return userQuery.fetchUser().mapToOneOrNullFlow(coroutineContext) { user ->
                user.mapToBase().convertToDetails()
            }
        }

        override suspend fun fetchMetadata(): MetadataModel? {
            val metadata = userQuery.fetchMetadata().awaitAsOneOrNull()
            return metadata?.let { MetadataModel(it.document_id, it.updated_at) }
        }

        override suspend fun deleteItem() {
            userQuery.deleteUser().await()
        }
    }
}