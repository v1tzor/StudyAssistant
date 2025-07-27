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

package ru.aleshin.studyassistant.core.database.datasource.requests

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.requests.mapToDetails
import ru.aleshin.studyassistant.core.database.mappers.requests.mapToEntity
import ru.aleshin.studyassistant.core.database.models.requests.FriendRequestsDetailsEntity
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.sqldelight.user.CurrentFriendRequestsQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 20.07.2025.
 */
interface FriendRequestsLocalDataSource : LocalDataSource.FullSynced.SingleDocument<FriendRequestsDetailsEntity> {

    class Base(
        private val friendRequestsQueries: CurrentFriendRequestsQueries,
        private val coroutineManager: CoroutineManager,
    ) : FriendRequestsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateItem(item: FriendRequestsDetailsEntity) {
            friendRequestsQueries.addOrUpdateFriendRequsts(item.mapToEntity()).await()
        }

        override suspend fun fetchItem(): Flow<FriendRequestsDetailsEntity?> {
            val query = friendRequestsQueries.fetchFriendRequsts()
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToDetails() }
        }

        override suspend fun fetchMetadata(): MetadataModel? {
            val query = friendRequestsQueries.fetchFriendRequsts()
            val emptyItem = query.awaitAsOneOrNull()
            return if (emptyItem != null) {
                MetadataModel(emptyItem.document_id, emptyItem.updated_at)
            } else {
                null
            }
        }

        override suspend fun deleteItem() {
            friendRequestsQueries.deleteFriendRequsts().await()
        }
    }
}