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

package ru.aleshin.studyassistant.core.data.managers.sync

import co.touchlab.kermit.Logger
import dev.tmapps.konnection.Konnection
import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.data.mappers.requsts.FriendRequestsSyncMapper
import ru.aleshin.studyassistant.core.data.utils.sync.BaseSourceSyncManager
import ru.aleshin.studyassistant.core.database.datasource.requests.FriendRequestsLocalDataSource
import ru.aleshin.studyassistant.core.database.models.requests.FriendRequestsDetailsEntity
import ru.aleshin.studyassistant.core.database.storages.ChangeQueueStorage
import ru.aleshin.studyassistant.core.domain.managers.sync.FriendRequestsSourceSyncManager
import ru.aleshin.studyassistant.core.remote.datasources.requests.FriendRequestsRemoteDataSource
import ru.aleshin.studyassistant.core.remote.models.requests.FriendRequestsDetailsPojo
import ru.aleshin.studyassistant.core.remote.utils.DatabaseEvent

/**
 * @author Stanislav Aleshin on 22.07.2025.
 */
class FriendRequestsSourceSyncManagerImpl(
    localDataSource: FriendRequestsLocalDataSource,
    remoteDataSource: FriendRequestsRemoteDataSource,
    mapper: FriendRequestsSyncMapper,
    changeQueueStorage: ChangeQueueStorage,
    coroutineManager: CoroutineManager,
    connectionManger: Konnection,
) : BaseSourceSyncManager.SingleDocument<FriendRequestsDetailsEntity, FriendRequestsDetailsPojo>(
    localDataSource = localDataSource,
    remoteDataSource = remoteDataSource,
    mappers = mapper,
    changeQueueStorage = changeQueueStorage,
    connectionManger = connectionManger,
    coroutineManager = coroutineManager,
), FriendRequestsSourceSyncManager {

    override suspend fun syncLocalDatabase() {
        val upsertRemoteModel = remoteDataSource.fetchItem().first() ?: return
        localDataSource.addOrUpdateItem(mappers.remoteToLocal(upsertRemoteModel))
        Logger.i("test2") { "$sourceSyncKey: updateLocalDatabase: upsertRemoteModel" }
    }

    override suspend fun collectOnlineChanges() {
        remoteDataSource.observeEvents().collect { event ->
            val localModel = localDataSource.fetchMetadata()
            when (event) {
                is DatabaseEvent.Create<FriendRequestsDetailsPojo> -> {
                    if (localModel?.updatedAt == null || localModel.updatedAt <= event.data.updatedAt) {
                        localDataSource.addOrUpdateItem(mappers.remoteToLocal(event.data))
                        Logger.i("test2") { "$sourceSyncKey: collectServerUpdates: event -> $event" }
                    }
                }
                is DatabaseEvent.Delete<FriendRequestsDetailsPojo> -> {
                    if (localModel != null) {
                        localDataSource.deleteItem()
                        Logger.i("test2") { "$sourceSyncKey: collectServerUpdates: event -> $event" }
                    }
                }
                is DatabaseEvent.Update<FriendRequestsDetailsPojo> -> {
                    if (localModel?.updatedAt == null || localModel.updatedAt <= event.data.updatedAt) {
                        localDataSource.addOrUpdateItem(mappers.remoteToLocal(event.data))
                        Logger.i("test2") { "$sourceSyncKey: collectServerUpdates: event -> $event" }
                    }
                }
                is DatabaseEvent.BatchUpdate<*> -> syncLocalDatabase()
            }
        }
    }
}