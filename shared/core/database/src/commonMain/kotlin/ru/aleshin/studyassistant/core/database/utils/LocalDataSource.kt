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

package ru.aleshin.studyassistant.core.database.utils

import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel

/**
 * Represents a local data source that manages entities stored in the local database (e.g. SQLiteDelight).
 *
 * This interface is a core part of the repository pattern used in synchronization with a remote database.
 * It abstracts access to local data storage and defines how entities are handled locally.
 *
 * There are two types of local data sources:
 *
 * - [OnlyOffline]: Local-only storage used when the user doesn't have access to remote features
 *   (e.g. due to not having a premium subscription).
 *
 * - [FullSynced]: Local storage that is synchronized with a remote database using a SourceSyncManager.
 *   It supports syncing offline changes and updating local state based on remote updates.
 *
 * @author Stanislav Aleshin on 22.07.2025.
 */
sealed interface LocalDataSource {

    /**
     * Represents a local-only data source.
     *
     * Used when remote synchronization is disabled or unavailable (e.g. free users).
     * Entities are stored only locally and are not synced with any backend.
     */
    interface OnlyOffline : LocalDataSource

    /**
     * Represents a local data source that participates in bidirectional synchronization with a remote backend.
     *
     * Used in combination with `BaseSourceSyncManager` to:
     * - Pull updates from the server.
     * - Push local offline changes.
     * - Receive real-time updates via event subscriptions.
     * Typical implementations use SQLiteDelight to persist data locally and expose them as Flows.
     *
     * @param T The local entity type, must inherit from [BaseLocalEntity].
     */
    sealed interface FullSynced<T : BaseLocalEntity> : LocalDataSource {

        /**
         * SyncedWithRemote implementation used when a user owns exactly one document.
         *
         * - User settings
         * - Profile configuration
         */
        interface SingleDocument<T : BaseLocalEntity> : FullSynced<T>, LocalSingleDocumentCommands<T>

        /**
         * SyncedWithRemote implementation used when a user owns a list of documents.
         *
         * Examples:
         * - Todos
         * - Notes
         * - Schedules
         */
        interface MultipleDocuments<T : BaseLocalEntity> : FullSynced<T>, LocalMultipleDocumentsCommands<T>
    }
}

/**
 * Set of local operations for a collection of documents.
 *
 * Usually used in [LocalDataSource.FullSynced.MultipleDocuments] implementations.
 */
interface LocalMultipleDocumentsCommands<T : BaseLocalEntity> {

    /** Upload or overwrite a single document */
    suspend fun addOrUpdateItem(item: T)

    /** Upload or overwrite multiple documents */
    suspend fun addOrUpdateItems(items: List<T>)

    /** Observe changes of a document by its ID */
    suspend fun fetchItemById(id: String): Flow<T?>

    /** Observe changes of multiple documents by their IDs */
    suspend fun fetchItemsById(ids: List<String>): Flow<List<T>>

    /** Fetch metadata for conflict resolution */
    suspend fun fetchAllMetadata(): List<MetadataModel>

    /** Remove multiple documents by their IDs */
    suspend fun deleteItemsById(ids: List<String>)

    /** Remove all documents */
    suspend fun deleteAllItems()
}

/**
 * Local operations for a single user-owned document.
 *
 * Used in [LocalDataSource.FullSynced.SingleDocument] implementations.
 */
interface LocalSingleDocumentCommands<T : BaseLocalEntity> {

    /** Upload or overwrite the document */
    suspend fun addOrUpdateItem(item: T)

    /** Observe changes to the document */
    suspend fun fetchItem(): Flow<T?>

    /** Fetch minimal metadata for conflict resolution */
    suspend fun fetchMetadata(): MetadataModel?

    /** Remove the document */
    suspend fun deleteItem()
}