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

package ru.aleshin.studyassistant.core.data.utils.sync

import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.database.storages.ChangeQueueStorage
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChange
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType
import ru.aleshin.studyassistant.core.domain.entities.sync.SourceSyncKey
import ru.aleshin.studyassistant.core.remote.utils.BaseRemotePojo

/**
 * @author Stanislav Aleshin on 22.07.2025.
 */
interface RemoteResultSyncHandler {

    /**
     * Adds a change to the local offline change queue.
     * These changes will be synchronized to the remote database once a connection is available.
     *
     * @param change The offline change to queue.
     */
    suspend fun addChangesToQueue(changes: List<OfflineChange>)

    /**
     * Remove all items from local offline change queue
     *
     * @param sourceKey Unique identifier for the source.
     */
    suspend fun clearAllChanges(sourceKey: SourceSyncKey)

    /**
     * Executes the given suspend [block] that performs a remote operation on the provided [data].
     * If an [InternetConnectionException] occurs during execution (i.e. no internet connection is available),
     * the operation is not lost and is instead recorded as an [OfflineChange] and added to the local queue
     * to be synchronized later when the connection is restored.
     *
     * This method enables fault-tolerant sync behavior where changes made offline can be persisted and replayed
     * when network connectivity becomes available.
     *
     * @param data The remote models on which the operation is to be performed.
     * @param type The type of change (e.g., [OfflineChangeType.UPSERT]) being executed.
     * @param sourceKey Unique identifier for the source.
     * @param block The suspend function that performs the actual remote operation.
     */
    suspend fun <P : BaseRemotePojo> executeOrAddToQueue(
        data: List<P>,
        type: OfflineChangeType,
        sourceKey: SourceSyncKey,
        block: suspend (List<P>) -> Unit,
    )

    /**
     * Executes the given suspend [block] that performs a remote operation on the provided [data].
     * If an [InternetConnectionException] occurs during execution (i.e. no internet connection is available),
     * the operation is not lost and is instead recorded as an [OfflineChange] and added to the local queue
     * to be synchronized later when the connection is restored.
     *
     * This method enables fault-tolerant sync behavior where changes made offline can be persisted and replayed
     * when network connectivity becomes available.
     *
     * @param data The remote model on which the operation is to be performed.
     * @param type The type of change (e.g., [OfflineChangeType.UPSERT]) being executed.
     * @param sourceKey Unique identifier for the source.
     * @param block The suspend function that performs the actual remote operation.
     */
    suspend fun <P : BaseRemotePojo> executeOrAddToQueue(
        data: P,
        type: OfflineChangeType,
        sourceKey: SourceSyncKey,
        block: suspend (P) -> Unit,
    )

    /**
     * Executes the given suspend [block] that performs a remote operation.
     * If an [InternetConnectionException] occurs during execution (i.e. no internet connection is available),
     * the operation is not lost and is instead recorded as an [OfflineChange] and added to the local queue
     * to be synchronized later when the connection is restored.
     *
     * This method enables fault-tolerant sync behavior where changes made offline can be persisted and replayed
     * when network connectivity becomes available.
     *
     * @param documentIds The remote model documentId list.
     * @param type The type of change (e.g., [OfflineChangeType.DELETE]) being executed.
     * @param sourceKey Unique identifier for the source.
     * @param block The suspend function that performs the actual remote operation.
     */
    suspend fun executeOrAddToQueue(
        documentIds: List<UID>,
        type: OfflineChangeType,
        sourceKey: SourceSyncKey,
        block: suspend () -> Unit,
    )

    /**
     * Executes the given suspend [block] that performs a remote operation.
     * If an [InternetConnectionException] occurs during execution (i.e. no internet connection is available),
     * the operation is not lost and is instead recorded as an [OfflineChange] and added to the local queue
     * to be synchronized later when the connection is restored.
     *
     * This method enables fault-tolerant sync behavior where changes made offline can be persisted and replayed
     * when network connectivity becomes available.
     *
     * @param documentId The remote model documentId.
     * @param type The type of change (e.g., [OfflineChangeType.DELETE]) being executed.
     * @param sourceKey Unique identifier for the source.
     * @param block The suspend function that performs the actual remote operation.
     */
    suspend fun executeOrAddToQueue(
        documentId: UID,
        type: OfflineChangeType,
        sourceKey: SourceSyncKey,
        block: suspend () -> Unit
    )

    class Base(
        private val changeQueueStorage: ChangeQueueStorage,
        private val dateManager: DateManager,
    ) : RemoteResultSyncHandler {

        override suspend fun <P : BaseRemotePojo> executeOrAddToQueue(
            data: List<P>,
            type: OfflineChangeType,
            sourceKey: SourceSyncKey,
            block: suspend (List<P>) -> Unit
        ) {
            try {
                block.invoke(data)
            } catch (_: InternetConnectionException) {
                val changes = data.map { OfflineChange.create(it.id, it.updatedAt, type, sourceKey) }
                addChangesToQueue(changes)
            }
        }

        override suspend fun <P : BaseRemotePojo> executeOrAddToQueue(
            data: P,
            type: OfflineChangeType,
            sourceKey: SourceSyncKey,
            block: suspend (P) -> Unit
        ) {
            executeOrAddToQueue(listOf(data), type, sourceKey) { block(it[0]) }
        }

        override suspend fun executeOrAddToQueue(
            documentIds: List<UID>,
            type: OfflineChangeType,
            sourceKey: SourceSyncKey,
            block: suspend () -> Unit
        ) {
            try {
                block.invoke()
            } catch (_: InternetConnectionException) {
                val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
                val changes = documentIds.map { OfflineChange.create(it, updatedAt, type, sourceKey) }
                addChangesToQueue(changes)
            }
        }

        override suspend fun executeOrAddToQueue(
            documentId: UID,
            type: OfflineChangeType,
            sourceKey: SourceSyncKey,
            block: suspend () -> Unit
        ) {
            executeOrAddToQueue(listOf(documentId), type, sourceKey, block)
        }

        override suspend fun addChangesToQueue(changes: List<OfflineChange>) {
            changeQueueStorage.addChangesToQueue(changes)
        }

        override suspend fun clearAllChanges(sourceKey: SourceSyncKey) {
            changeQueueStorage.deleteAllSourceChanges(sourceKey)
        }
    }
}