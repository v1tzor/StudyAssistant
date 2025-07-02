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

import io.appwrite.services.Databases
import ru.aleshin.studyassistant.core.remote.appwrite.databases.AppwriteDatabase
import ru.aleshin.studyassistant.core.remote.appwrite.databases.Document
import ru.aleshin.studyassistant.core.remote.appwrite.databases.DocumentList
import kotlin.reflect.KClass

/**
 * @author Stanislav Aleshin on 26.06.2025.
 */
class AppwriteDatabaseAndroid(
    private val database: Databases
) : AppwriteDatabase {

    override suspend fun <T : Any> listDocuments(
        databaseId: String,
        collectionId: String,
        queries: List<String>?,
        nestedType: KClass<T>,
    ): DocumentList<T> {
        return database.listDocuments(
            databaseId = databaseId,
            collectionId = collectionId,
            queries = queries,
            nestedType = nestedType.javaObjectType,
        ).convertToCommon()
    }

    override suspend fun listDocuments(
        databaseId: String,
        collectionId: String,
        queries: List<String>?,
    ): DocumentList<Map<String, Any>> {
        return database.listDocuments(
            databaseId = databaseId,
            collectionId = collectionId,
            queries = queries,
        ).convertToCommon()
    }

    override suspend fun <T : Any> createDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: Any,
        permissions: List<String>?,
        nestedType: KClass<T>
    ): Document<T> {
        return database.createDocument(
            databaseId = databaseId,
            collectionId = collectionId,
            documentId = documentId,
            data = data,
            permissions = permissions,
            nestedType = nestedType.javaObjectType
        ).convertToCommon()
    }

    override suspend fun createDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: Any,
        permissions: List<String>?
    ): Document<Map<String, Any>> {
        return database.createDocument(
            databaseId = databaseId,
            collectionId = collectionId,
            documentId = documentId,
            data = data,
            permissions = permissions
        ).convertToCommon()
    }

    override suspend fun <T : Any> getDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        queries: List<String>?,
        nestedType: KClass<T>
    ): Document<T> {
        return database.getDocument(
            databaseId = databaseId,
            collectionId = collectionId,
            documentId = documentId,
            queries = queries,
            nestedType = nestedType.javaObjectType
        ).convertToCommon()
    }

    override suspend fun getDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        queries: List<String>?
    ): Document<Map<String, Any>> {
        return database.getDocument(
            databaseId = databaseId,
            collectionId = collectionId,
            documentId = documentId,
            queries = queries
        ).convertToCommon()
    }

    override suspend fun <T : Any> upsertDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: Any,
        permissions: List<String>?,
        nestedType: KClass<T>
    ): Document<T> {
        return database.upsertDocument(
            databaseId = databaseId,
            collectionId = collectionId,
            documentId = documentId,
            data = data,
            permissions = permissions,
            nestedType = nestedType.javaObjectType
        ).convertToCommon()
    }

    override suspend fun upsertDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: Any,
        permissions: List<String>?
    ): Document<Map<String, Any>> {
        return database.upsertDocument(
            databaseId = databaseId,
            collectionId = collectionId,
            documentId = documentId,
            data = data,
            permissions = permissions
        ).convertToCommon()
    }

    override suspend fun <T : Any> updateDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: Any?,
        permissions: List<String>?,
        nestedType: KClass<T>
    ): Document<T> {
        return database.updateDocument(
            databaseId = databaseId,
            collectionId = collectionId,
            documentId = documentId,
            data = data,
            permissions = permissions,
            nestedType = nestedType.javaObjectType
        ).convertToCommon()
    }

    override suspend fun updateDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: Any?,
        permissions: List<String>?
    ): Document<Map<String, Any>> {
        return database.updateDocument(
            databaseId = databaseId,
            collectionId = collectionId,
            documentId = documentId,
            data = data,
            permissions = permissions
        ).convertToCommon()
    }

    override suspend fun deleteDocument(
        databaseId: String,
        collectionId: String,
        documentId: String
    ): Any {
        return database.deleteDocument(
            databaseId = databaseId,
            collectionId = collectionId,
            documentId = documentId
        )
    }
}