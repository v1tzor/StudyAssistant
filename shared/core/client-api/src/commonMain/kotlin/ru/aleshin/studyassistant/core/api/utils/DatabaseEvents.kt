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

package ru.aleshin.studyassistant.core.api.utils

/**
 * @author Stanislav Aleshin on 21.07.2025.
 */
object DatabaseEvents {

    fun allDatabases() = "databases."

    fun allCollections(databaseId: String) = "databases.$databaseId.collections"

    fun allAttributes(databaseId: String, collectionId: String) =
        "databases.$databaseId.collections.$collectionId.attributes"

    fun attributeCreated(databaseId: String, collectionId: String, attributeKey: String) =
        "databases.$databaseId.collections.$collectionId.attributes.$attributeKey.create"

    fun attributeDeleted(databaseId: String, collectionId: String, attributeKey: String) =
        "databases.$databaseId.collections.$collectionId.attributes.$attributeKey.delete"

    fun collectionCreated(databaseId: String, collectionId: String) =
        "databases.$databaseId.collections.$collectionId.create"

    fun collectionDeleted(databaseId: String, collectionId: String) =
        "databases.$databaseId.collections.$collectionId.delete"

    fun collectionUpdated(databaseId: String, collectionId: String) =
        "databases.$databaseId.collections.$collectionId.update"

    fun allDocuments(databaseId: String, collectionId: String) =
        "databases.$databaseId.collections.$collectionId.documents"

    fun documentCreated(databaseId: String, collectionId: String, documentId: String) =
        "databases.$databaseId.collections.$collectionId.documents.$documentId.create"

    fun documentDeleted(databaseId: String, collectionId: String, documentId: String) =
        "databases.$databaseId.collections.$collectionId.documents.$documentId.delete"

    fun documentUpdated(databaseId: String, collectionId: String, documentId: String) =
        "databases.$databaseId.collections.$collectionId.documents.$documentId.update"

    fun allIndexes(databaseId: String, collectionId: String) =
        "databases.$databaseId.collections.$collectionId.indexes"

    fun indexCreated(databaseId: String, collectionId: String, indexKey: String) =
        "databases.$databaseId.collections.$collectionId.indexes.$indexKey.create"

    fun indexDeleted(databaseId: String, collectionId: String, indexKey: String) =
        "databases.$databaseId.collections.$collectionId.indexes.$indexKey.delete"

    fun databaseCreated(databaseId: String) =
        "databases.$databaseId.create"

    fun databaseDeleted(databaseId: String) =
        "databases.$databaseId.delete"

    fun databaseUpdated(databaseId: String) =
        "databases.$databaseId.update"
}