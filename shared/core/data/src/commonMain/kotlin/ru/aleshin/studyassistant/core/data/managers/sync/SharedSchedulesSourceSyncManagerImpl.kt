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
import ru.aleshin.studyassistant.core.data.mappers.share.SharedSchedulesSyncMapper
import ru.aleshin.studyassistant.core.data.utils.sync.BaseSourceSyncManager
import ru.aleshin.studyassistant.core.database.datasource.shared.SharedSchedulesLocalDataSource
import ru.aleshin.studyassistant.core.database.models.shared.schedules.SharedSchedulesShortDetailsEntity
import ru.aleshin.studyassistant.core.database.storages.ChangeQueueStorage
import ru.aleshin.studyassistant.core.domain.managers.sync.SharedSchedulesSourceSyncManager
import ru.aleshin.studyassistant.core.remote.datasources.share.SharedSchedulesRemoteDataSource
import ru.aleshin.studyassistant.core.remote.models.shared.schedules.SharedSchedulesDetailsPojo
import ru.aleshin.studyassistant.core.remote.utils.DatabaseEvent

/**
 * @author Stanislav Aleshin on 22.07.2025.
 */
class SharedSchedulesSourceSyncManagerImpl(
    localDataSource: SharedSchedulesLocalDataSource,
    remoteDataSource: SharedSchedulesRemoteDataSource,
    mapper: SharedSchedulesSyncMapper,
    changeQueueStorage: ChangeQueueStorage,
    coroutineManager: CoroutineManager,
    connectionManger: Konnection,
) : BaseSourceSyncManager.SingleDocument<SharedSchedulesShortDetailsEntity, SharedSchedulesDetailsPojo>(
    localDataSource = localDataSource,
    remoteDataSource = remoteDataSource,
    mappers = mapper,
    changeQueueStorage = changeQueueStorage,
    connectionManger = connectionManger,
    coroutineManager = coroutineManager,
), SharedSchedulesSourceSyncManager {

    override suspend fun updateLocalDatabase() {
        val upsertRemoteModel = remoteDataSource.fetchItem().first() ?: return
        localDataSource.addOrUpdateItem(mappers.remoteToLocal(upsertRemoteModel))
        Logger.i("test2") { "$sourceSyncKey: updateLocalDatabase: upsertRemoteModel" }
    }

    override suspend fun collectServerUpdates() {
        remoteDataSource.observeEvents().collect { event ->
            val localModel = localDataSource.fetchMetadata()
            when (event) {
                is DatabaseEvent.Create<SharedSchedulesDetailsPojo> -> {
                    if (localModel?.updatedAt == null || localModel.updatedAt <= event.data.updatedAt) {
                        Logger.i("test2") { "$sourceSyncKey: collectServerUpdates: event -> $event" }
                    }
                }
                is DatabaseEvent.Delete<SharedSchedulesDetailsPojo> -> {
                    if (localModel != null) {
                        localDataSource.deleteItem()
                        Logger.i("test2") { "$sourceSyncKey: collectServerUpdates: event -> $event" }
                    }
                }
                is DatabaseEvent.Update<SharedSchedulesDetailsPojo> -> {
                    if (localModel?.updatedAt == null || localModel.updatedAt <= event.data.updatedAt) {
                        localDataSource.addOrUpdateItem(mappers.remoteToLocal(event.data))
                        Logger.i("test2") { "$sourceSyncKey: collectServerUpdates: event -> $event" }
                    }
                }
            }
        }
    }
}