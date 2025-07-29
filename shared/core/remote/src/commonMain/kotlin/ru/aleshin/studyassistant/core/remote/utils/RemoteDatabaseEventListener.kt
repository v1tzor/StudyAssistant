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

/**
 * Interface for listening to real-time remote database events.
 *
 * This is typically implemented by remote data sources that use reactive
 * subscriptions (e.g., Appwrite Realtime) to track document-level changes
 * in a remote collection.
 *
 * Implementers must return a [Flow] of [DatabaseEvent] objects,
 * which allows the sync manager or repository layer to react to remote changes
 * such as creation, updates, or deletion of documents.
 *
 * @param T the data type representing the remote document.
 *
 * @author Stanislav Aleshin on 21.07.2025.
 */
interface RemoteDatabaseEventListener<T> {

    /**
     * Subscribes to changes in a remote data collection and emits real-time events
     * as a cold [Flow].
     *
     * Typical use cases:
     * - Listening for changes to user-owned resources (e.g., schedules, tasks)
     * - Triggering automatic sync when a remote change occurs
     * - Observing external changes made from another device or user
     *
     * @return a [Flow] emitting [DatabaseEvent]s.
     */
    suspend fun observeEvents(): Flow<DatabaseEvent<T>>
}

/**
 * A sealed representation of a remote database change event.
 *
 * These events are emitted by [RemoteDatabaseEventListener.observeEvents] and
 * can be used to trigger reactive synchronization in the app.
 *
 * @param T the data model affected by the event.
 */
sealed interface DatabaseEvent<T> {

    val documentId: String

    /**
     * Represents a new document that was created remotely.
     */
    data class Create<T>(val data: T, override val documentId: String) : DatabaseEvent<T>

    /**
     * Represents an existing document that was updated remotely.
     */
    data class Update<T>(val data: T, override val documentId: String) : DatabaseEvent<T>

    /**
     * Represents a document that was deleted remotely.
     */
    data class Delete<T>(override val documentId: String) : DatabaseEvent<T>

    /**
     * Notification of multiple collection updates
     */
    data class BatchUpdate<T>(override val documentId: String) : DatabaseEvent<T>
}