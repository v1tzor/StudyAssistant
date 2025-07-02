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

package ru.aleshin.studyassistant.core.remote.appwrite

import ru.aleshin.studyassistant.core.remote.appwrite.databases.AppwriteDatabase
import ru.aleshin.studyassistant.core.remote.appwrite.databases.Document
import ru.aleshin.studyassistant.core.remote.appwrite.databases.DocumentList
import ru.aleshin.studyassistant.core.remote.appwrite.databases.mapData
import kotlin.reflect.KClass

/**
 * @author Stanislav Aleshin on 28.06.2025.
 */
abstract class AppwriteDatabaseApple : AppwriteDatabase {

    override suspend fun <T : Any> listDocuments(
        databaseId: String,
        collectionId: String,
        queries: List<String>?,
        nestedType: KClass<T>
    ): DocumentList<T> {
        val documentList = listDocuments(
            databaseId = databaseId,
            collectionId = collectionId,
            queries = queries,
        )

        return DocumentList(
            total = documentList.total,
            documents = documentList.documents.map { it.mapData(nestedType) },
        )
    }

    override suspend fun <T : Any> createDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: Any,
        permissions: List<String>?,
        nestedType: KClass<T>
    ): Document<T> {
        val document = createDocument(databaseId, collectionId, documentId, data, permissions)
        return document.mapData(nestedType)
    }

    override suspend fun <T : Any> getDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        queries: List<String>?,
        nestedType: KClass<T>
    ): Document<T> {
        val document = getDocument(databaseId, collectionId, documentId, queries)
        return document.mapData(nestedType)
    }

    override suspend fun <T : Any> upsertDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: Any,
        permissions: List<String>?,
        nestedType: KClass<T>
    ): Document<T> {
        val document = upsertDocument(databaseId, collectionId, documentId, data, permissions)
        return document.mapData(nestedType)
    }

    override suspend fun <T : Any> updateDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: Any?,
        permissions: List<String>?,
        nestedType: KClass<T>
    ): Document<T> {
        val document = updateDocument(databaseId, collectionId, documentId, data, permissions)
        return document.mapData(nestedType)
    }
}