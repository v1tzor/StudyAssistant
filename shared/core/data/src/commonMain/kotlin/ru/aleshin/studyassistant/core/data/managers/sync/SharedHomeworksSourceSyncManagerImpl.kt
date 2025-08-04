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

import dev.tmapps.konnection.Konnection
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.data.mappers.share.ShareHomeworksSyncMapper
import ru.aleshin.studyassistant.core.data.utils.sync.BaseSourceSyncManager
import ru.aleshin.studyassistant.core.database.datasource.shared.SharedHomeworksLocalDataSource
import ru.aleshin.studyassistant.core.database.models.shared.homeworks.SharedHomeworksDetailsEntity
import ru.aleshin.studyassistant.core.database.storages.ChangeQueueStorage
import ru.aleshin.studyassistant.core.domain.managers.sync.SharedHomeworksSourceSyncManager
import ru.aleshin.studyassistant.core.remote.datasources.share.SharedHomeworksRemoteDataSource
import ru.aleshin.studyassistant.core.remote.models.shared.homeworks.SharedHomeworksDetailsPojo
import ru.aleshin.studyassistant.core.remote.utils.DatabaseEvent

/**
 * @author Stanislav Aleshin on 22.07.2025.
 */
class SharedHomeworksSourceSyncManagerImpl(
    localDataSource: SharedHomeworksLocalDataSource,
    remoteDataSource: SharedHomeworksRemoteDataSource,
    mapper: ShareHomeworksSyncMapper,
    changeQueueStorage: ChangeQueueStorage,
    coroutineManager: CoroutineManager,
    connectionManger: Konnection,
) : BaseSourceSyncManager.SingleDocument<SharedHomeworksDetailsEntity, SharedHomeworksDetailsPojo>(
    localDataSource = localDataSource,
    remoteDataSource = remoteDataSource,
    mappers = mapper,
    changeQueueStorage = changeQueueStorage,
    connectionManger = connectionManger,
    coroutineManager = coroutineManager,
), SharedHomeworksSourceSyncManager {

    override suspend fun syncLocalDatabase() {
        val upsertRemoteModel = remoteDataSource.fetchOnceItem() ?: return
        localDataSource.addOrUpdateItem(mappers.remoteToLocal(upsertRemoteModel))
    }

    override suspend fun collectOnlineChanges() {
        remoteDataSource.observeEvents().collect { event ->
            val localModel = localDataSource.fetchMetadata()
            when (event) {
                is DatabaseEvent.Create<SharedHomeworksDetailsPojo> -> {
                    if (localModel?.updatedAt == null || localModel.updatedAt <= event.data.updatedAt) {
                        localDataSource.addOrUpdateItem(mappers.remoteToLocal(event.data))
                    }
                }
                is DatabaseEvent.Delete<SharedHomeworksDetailsPojo> -> {
                    if (localModel != null) {
                        localDataSource.deleteItem()
                    }
                }
                is DatabaseEvent.Update<SharedHomeworksDetailsPojo> -> {
                    if (localModel?.updatedAt == null || localModel.updatedAt <= event.data.updatedAt) {
                        localDataSource.addOrUpdateItem(mappers.remoteToLocal(event.data))
                    }
                }
                is DatabaseEvent.BatchUpdate<*> -> syncLocalDatabase()
            }
        }
    }
}