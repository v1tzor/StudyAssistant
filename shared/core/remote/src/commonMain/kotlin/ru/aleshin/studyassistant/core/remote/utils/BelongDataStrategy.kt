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

/**
 * Strategy that defines how remote documents in a collection are associated with a specific user.
 *
 * This is used by remote data sources to determine:
 * - how to fetch documents owned by the currently authenticated user,
 * - how to construct correct queries for retrieval,
 * - or how to compute a unique document ID for single-user resources.
 *
 * There are two main strategies:
 *
 * 1. [SingleDocument] — for collections that store exactly one document per user,
 *    typically with a fixed `documentId` equal to the user ID (or derived from it).
 *
 * 2. [Queries] — for collections that store many documents per user, where ownership
 *    must be determined by applying a filter query (e.g., `userId == $currentUserId`).
 *
 * @author Stanislav Aleshin on 21.07.2025.
 */
sealed class BelongDataStrategy {
    /**
     * Strategy for collections where each user owns a single document,
     * and the document ID is deterministic (usually equals to the user ID).
     *
     * This is useful when:
     * - each user maintains a single object (e.g., `UserProfile`, `CalendarSettings`)
     * - the document can be directly accessed by ID without searching or filtering
     *
     * @param documentId the fixed document ID (e.g., current user's UID)
     */
    data class SingleDocument(val documentId: String) : BelongDataStrategy()

    /**
     * Strategy for collections where users can own multiple documents,
     * and ownership must be determined through filtering queries.
     *
     * This is useful for multi-entity resources like:
     * - `Todos`, `Goals`, `Schedules`, etc.
     *
     * The queries are typically based on the user ID field in the document, like:
     * `Query.equal("userId", currentUserId)`
     *
     * @param queries list of Appwrite-style query strings to filter the user-owned documents.
     */
    data class Queries(val queries: List<String>) : BelongDataStrategy()
}