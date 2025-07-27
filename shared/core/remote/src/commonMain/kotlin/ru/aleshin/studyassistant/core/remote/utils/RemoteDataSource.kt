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

package ru.aleshin.studyassistant.core.remote.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.datetime.format.DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import ru.aleshin.studyassistant.core.api.AppwriteApi.Common
import ru.aleshin.studyassistant.core.api.AppwriteApi.Common.UPDATED_AT
import ru.aleshin.studyassistant.core.api.AppwriteApi.Common.USER_ID
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.api.databases.DatabaseService
import ru.aleshin.studyassistant.core.api.realtime.RealtimeService
import ru.aleshin.studyassistant.core.api.utils.Channels
import ru.aleshin.studyassistant.core.api.utils.Query
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.getStringOrNull
import ru.aleshin.studyassistant.core.common.functional.UID

/**
 * Root abstraction for all Appwrite-based remote data sources in the application.
 *
 * This interface separates two categories of sources:
 * - [OnlyOnline] — Stateless sources without local caching or offline support
 * - [FullSynced] — Full-featured sources supporting offline-first, sync queues, and real-time updates
 *
 * All syncable sources are expected to use:
 * - A `LocalDataSource.SyncedWithRemote` for offline caching
 * - A `ChangeQueueStorage` for storing offline changes
 * - A `SourceSyncManager` for syncing to/from remote
 *
 * @author Stanislav Aleshin on 21.07.2025.
 */
sealed interface RemoteDataSource {

    /**
     * Represents an online-only data source.
     *
     * This kind of source:
     * - Is used strictly while the device is online
     * - Has no local mirror or offline queue
     * - Cannot listen to real-time events
     *
     * Suitable for accessing public or shared data, or resources not owned by the current user.
     */
    interface OnlyOnline : RemoteDataSource

    /**
     * Represents a remote data source that supports full offline-first synchronization.
     *
     * Responsibilities:
     * - Act as a bridge between local database (SQLDelight) and Appwrite
     * - Fetch/persist items to the cloud
     * - Participate in conflict resolution based on `updatedAt`
     * - Emit [DatabaseEvent]s in real-time
     *
     * Each Sync implementation should be associated with:
     * - A corresponding `LocalDataSource.SyncedWithRemote`
     * - A `SourceSyncManager` to automate syncing
     * - A unique `SourceSyncKey`
     *
     * Sync supports both:
     * - [SingleDocument] — one document per user
     * - [MultipleDocuments] — list of documents per user
     */
    sealed interface FullSynced<T : BaseRemotePojo> : RemoteDataSource, RemoteDatabaseEventListener<T> {

        /**
         * Sync implementation used when a user owns exactly one document.
         *
         * Examples:
         * - User settings
         * - User profile
         */
        interface SingleDocument<T : BaseRemotePojo> : FullSynced<T>, RemoteSingleDocumentCommands<T> {

            /**
             * Base implementation of a single-document Appwrite data source.
             *
             * Handles:
             * - Document creation, update, deletion
             * - Real-time subscription to one document
             * - Timestamp-based conflict resolution (via `updatedAt`)
             */
            abstract class BaseAppwrite<T : BaseRemotePojo>(
                protected val database: DatabaseService,
                protected val realtime: RealtimeService,
                protected val userSessionProvider: UserSessionProvider,
            ) : SingleDocument<T> {

                abstract val databaseId: String
                abstract val collectionId: String
                abstract val nestedType: KSerializer<T>

                abstract fun permissions(currentUser: UID): List<String>

                override suspend fun addOrUpdateItem(item: T) {
                    val currentUser = userSessionProvider.getCurrentUserId()

                    database.upsertDocument(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        documentId = currentUser,
                        data = item,
                        nestedType = nestedType,
                        permissions = permissions(currentUser),
                    )
                }

                override suspend fun fetchItem(): Flow<T?> {
                    val currentUser = userSessionProvider.getCurrentUserId()

                    return database.getDocumentFlow(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        documentId = currentUser,
                        nestedType = nestedType,
                    ).map { item ->
                        item?.data
                    }
                }

                override suspend fun fetchMetadata(): MetadataModel? {
                    val currentUser = userSessionProvider.getCurrentUserId()

                    val document = database.getDocumentOrNull(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        documentId = currentUser,
                        nestedType = JsonElement.serializer(),
                    )

                    return if (document != null) {
                        val documentUpdatedAt = ISO_DATE_TIME_OFFSET.parse(document.updatedAt)
                        val updatedAt = document.data.getStringOrNull(UPDATED_AT).let {
                            it?.toLongOrNull() ?: documentUpdatedAt.toInstantUsingOffset().toEpochMilliseconds()
                        }
                        MetadataModel(id = currentUser, updatedAt = updatedAt)
                    } else {
                        null
                    }
                }

                override suspend fun deleteItem() {
                    val currentUser = userSessionProvider.getCurrentUserId()

                    database.deleteDocument(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        documentId = currentUser,
                    )
                }

                override suspend fun observeEvents(): Flow<DatabaseEvent<T>> {
                    val currentUser = userSessionProvider.getCurrentUserId()

                    return realtime.subscribe(
                        channels = Channels.document(databaseId, collectionId, currentUser),
                        payloadType = nestedType,
                    ).map { response ->
                        response.copy(
                            events = response.events.filter {
                                it.contains(databaseId) &&
                                    it.contains(collectionId) &&
                                    it.contains(currentUser)
                            }
                        )
                    }.filter { response ->
                        response.events.isNotEmpty()
                    }.map { response ->
                        if (response.events.any { it.contains("create") }) {
                            DatabaseEvent.Create(data = response.payload, documentId = currentUser)
                        } else if (response.events.any { it.contains("delete") }) {
                            DatabaseEvent.Delete(documentId = currentUser)
                        } else {
                            DatabaseEvent.Update(data = response.payload, documentId = currentUser)
                        }
                    }
                }
            }
        }

        /**
         * Sync implementation used when a user owns a list of documents.
         *
         * Examples:
         * - Todos
         * - Notes
         * - Schedules
         */
        interface MultipleDocuments<T : BaseMultipleRemotePojo> : FullSynced<T>, RemoteMultipleDocumentsCommands<T> {

            /**
             * Base implementation of a multi-document Appwrite data source.
             *
             * Features:
             * - Add/update one or multiple documents
             * - Fetch all documents owned by a user
             * - Fetch minimal metadata (id + updatedAt) for conflict resolution
             * - Real-time document-level updates
             * - UID-based filtering for selective operations
             */
            abstract class BaseAppwrite<T : BaseMultipleRemotePojo>(
                protected val database: DatabaseService,
                protected val realtime: RealtimeService,
                protected val userSessionProvider: UserSessionProvider,
            ) : MultipleDocuments<T> {

                abstract val databaseId: String
                abstract val collectionId: String
                abstract val nestedType: KSerializer<T>

                abstract fun permissions(currentUser: UID): List<String>

                override suspend fun addOrUpdateItem(item: T) {
                    val currentUser = userSessionProvider.getCurrentUserId()

                    database.upsertDocument(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        documentId = item.id,
                        data = item,
                        nestedType = nestedType,
                        permissions = permissions(currentUser),
                    )
                }

                override suspend fun addOrUpdateItems(items: List<T>) {
                    val currentUser = userSessionProvider.getCurrentUserId()

                    database.upsertDocuments(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        documents = items,
                        nestedType = nestedType,
                        permissions = permissions(currentUser),
                    )
                }

                override suspend fun fetchItemById(id: String): Flow<T?> {
                    return database.getDocumentFlow(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        documentId = id,
                        nestedType = nestedType,
                    ).map { items ->
                        items?.data
                    }
                }

                override suspend fun fetchItemsByIds(ids: List<String>): Flow<List<T>> {
                    return database.listDocumentsFlow(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        queries = listOf(Query.equal(Common.UID, ids)),
                        nestedType = nestedType,
                    ).map { items ->
                        items.map { it.data }
                    }
                }

                override suspend fun fetchAllItems(targetUser: String): Flow<List<T>> {
                    return database.listDocumentsFlow(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        queries = listOf(Query.equal(USER_ID, targetUser)),
                        nestedType = nestedType,
                    ).map { documents ->
                        documents.map { it.data }
                    }
                }

                override suspend fun fetchAllMetadata(targetUser: String): List<MetadataModel> {
                    return database.listDocuments(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        queries = listOf(Query.equal(USER_ID, targetUser)),
                        nestedType = JsonElement.serializer(),
                    ).documents.map { document ->
                        val id = document.data.getStringOrNull(Common.UID) ?: document.id
                        val documentUpdatedAt = ISO_DATE_TIME_OFFSET.parse(document.updatedAt)
                        val updatedAt = document.data.getStringOrNull(UPDATED_AT).let {
                            it?.toLongOrNull() ?: documentUpdatedAt.toInstantUsingOffset().toEpochMilliseconds()
                        }
                        MetadataModel(id = id, updatedAt = updatedAt)
                    }
                }

                override suspend fun deleteAllItems(targetUser: String) {
                    database.deleteDocuments(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        queries = listOf(Query.equal(USER_ID, targetUser)),
                    )
                }

                override suspend fun deleteItemById(id: String) {
                    database.deleteDocument(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        documentId = id,
                    )
                }

                override suspend fun deleteItemsByIds(ids: List<String>) {
                    database.deleteDocuments(
                        databaseId = databaseId,
                        collectionId = collectionId,
                        queries = listOf(Query.equal(Common.UID, ids)),
                    )
                }

                override suspend fun observeEvents(): Flow<DatabaseEvent<T>> {
                    return realtime.subscribe(
                        channels = Channels.documents(databaseId, collectionId),
                        payloadType = nestedType,
                    ).map { response ->
                        response.copy(
                            events = response.events.filter {
                                it.contains(databaseId) && it.contains(collectionId)
                            }
                        )
                    }.filter { response ->
                        response.events.isNotEmpty()
                    }.map { response ->
                        if (response.events.any { it.contains("create") }) {
                            DatabaseEvent.Create(data = response.payload, documentId = response.payload.id)
                        } else if (response.events.any { it.contains("delete") }) {
                            DatabaseEvent.Delete(documentId = response.payload.id)
                        } else {
                            DatabaseEvent.Update(data = response.payload, documentId = response.payload.id)
                        }
                    }
                }
            }
        }
    }
}

/**
 * Set of remote operations for a collection of documents.
 *
 * Usually used in [RemoteDataSource.FullSynced.MultipleDocuments] implementations.
 */
interface RemoteMultipleDocumentsCommands<T : BaseMultipleRemotePojo> {

    /** Upload or overwrite a single document in Appwrite */
    suspend fun addOrUpdateItem(item: T)

    /** Upload or overwrite multiple documents in a single batch */
    suspend fun addOrUpdateItems(items: List<T>)

    /** Observe changes of a document by its ID */
    suspend fun fetchItemById(id: String): Flow<T?>

    /** Observe changes of multiple documents by their IDs */
    suspend fun fetchItemsByIds(ids: List<String>): Flow<List<T>>

    /** Observe all user-owned documents */
    suspend fun fetchAllItems(targetUser: String): Flow<List<T>>

    /** Fetch metadata for conflict resolution */
    suspend fun fetchAllMetadata(targetUser: String): List<MetadataModel>

    /** Remove all documents owned by a specific user */
    suspend fun deleteAllItems(targetUser: String)

    /** Remove a single document by ID */
    suspend fun deleteItemById(id: String)

    /** Remove multiple documents by their IDs */
    suspend fun deleteItemsByIds(ids: List<String>)
}

/**
 * Remote operations for a single user-owned document.
 *
 * Used in [RemoteDataSource.FullSynced.SingleDocument] implementations.
 */
interface RemoteSingleDocumentCommands<T : BaseRemotePojo> {

    /** Upload or overwrite the document */
    suspend fun addOrUpdateItem(item: T)

    /** Observe changes to the document */
    suspend fun fetchItem(): Flow<T?>

    /** Fetch minimal metadata for conflict resolution */
    suspend fun fetchMetadata(): MetadataModel?

    /** Remove the document */
    suspend fun deleteItem()
}