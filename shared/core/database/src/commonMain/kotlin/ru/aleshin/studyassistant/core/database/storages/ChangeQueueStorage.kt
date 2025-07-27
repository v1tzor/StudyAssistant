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

package ru.aleshin.studyassistant.core.database.storages

import app.cash.sqldelight.async.coroutines.awaitAsList
import ru.aleshin.studyassistant.core.database.mappers.sync.convertToBase
import ru.aleshin.studyassistant.core.database.mappers.sync.convertToLocal
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChange
import ru.aleshin.studyassistant.core.domain.entities.sync.SourceSyncKey
import ru.aleshin.studyassistant.sqldelight.sync.OfflineChangeQueries

/**
 * Interface for managing a queue of offline changes made by the user while disconnected from the network.
 *
 * Offline changes are stored persistently and are later uploaded to the backend once network connectivity
 * is restored and synchronization resumes.
 *
 * All operations are suspendable and safe to call from background threads.
 *
 * @author Stanislav Aleshin on 22.07.2025
 */
interface ChangeQueueStorage {

    /**
     * Adds a list of offline changes to the queue.
     * Duplicate entries (by ID) may be overwritten, depending on the underlying implementation.
     *
     * @param changes A list of [OfflineChange]s to add to persistent queue storage.
     */
    suspend fun addChangesToQueue(changes: List<OfflineChange>)

    /**
     * Fetches all queued changes associated with a specific remote source.
     *
     * @param sourceKey Unique identifier for the source.
     * @return A list of [OfflineChange] objects relevant to the provided [sourceKey].
     */
    suspend fun fetchAllSourceChanges(sourceKey: SourceSyncKey): List<OfflineChange>

    /**
     * Removes a single change from the queue by its unique ID.
     *
     * @param id The identifier of the change to remove.
     */
    suspend fun deleteChangeById(id: String)

    /**
     * Removes a batch of changes from the queue using their IDs.
     *
     * @param ids A list of change IDs to remove.
     */
    suspend fun deleteChangesByIds(ids: List<String>)

    /**
     * Removes all queued changes that belong to the specified source.
     * Useful when a full sync completes or the local cache is wiped.
     *
     * @param sourceKey Unique key identifying which source's changes should be removed.
     */
    suspend fun deleteAllSourceChanges(sourceKey: SourceSyncKey)

    /**
     * Default SQL implementation of [ChangeQueueStorage] using SQLDelight-generated queries.
     */
    class Base(private val offlineChangeQuery: OfflineChangeQueries) : ChangeQueueStorage {

        override suspend fun addChangesToQueue(changes: List<OfflineChange>) {
            changes.forEach { change ->
                offlineChangeQuery.addChangeToQueue(change.convertToLocal()).await()
            }
        }

        override suspend fun fetchAllSourceChanges(sourceKey: SourceSyncKey): List<OfflineChange> {
            return offlineChangeQuery
                .fetchAllSourceChanges(sourceKey)
                .awaitAsList()
                .map { it.convertToBase() }
        }

        override suspend fun deleteChangeById(id: String) {
            offlineChangeQuery.deleteChangeById(id).await()
        }

        override suspend fun deleteChangesByIds(ids: List<String>) {
            offlineChangeQuery.deleteChangesByIds(ids).await()
        }

        override suspend fun deleteAllSourceChanges(sourceKey: SourceSyncKey) {
            offlineChangeQuery.deleteAllSourceChanges(sourceKey).await()
        }
    }
}