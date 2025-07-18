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

package ru.aleshin.studyassistant.core.remote.appwrite.databases

import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonElement
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.remote.appwrite.AppwriteService
import ru.aleshin.studyassistant.core.remote.appwrite.client.AppwriteClient
import ru.aleshin.studyassistant.core.remote.appwrite.realtime.RealtimeService
import ru.aleshin.studyassistant.core.remote.appwrite.utils.Channels
import ru.aleshin.studyassistant.core.remote.models.appwrite.ClientParam
import ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentListPojo
import ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentPojo
import ru.aleshin.studyassistant.core.remote.models.appwrite.asDocument

/**
 * @author Stanislav Aleshin on 09.07.2025.
 */
class DatabaseService(
    client: AppwriteClient,
    private val realtime: RealtimeService,
) : AppwriteService(client) {

    /**
     * List documents
     *
     * Get a list of all the user&#039;s documents in a given collection. You can use the query params to filter your results.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection).
     * @param queries Array of query strings generated using the Query class provided by the SDK. [Learn more about queries](https://appwrite.io/docs/queries). Maximum of 100 queries are allowed, each 4096 characters long.
     * @return [ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentListPojo]
     */
    suspend fun <T> listDocuments(
        databaseId: String,
        collectionId: String,
        queries: List<String>? = null,
        nestedType: KSerializer<T>,
    ): DocumentListPojo<DocumentPojo<T>> {
        val apiPath = "/databases/{databaseId}/collections/{collectionId}/documents"
            .replace("{databaseId}", databaseId)
            .replace("{collectionId}", collectionId)

        val apiParams = listOf(ClientParam.ListParam("queries", queries ?: emptyList()))
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        val documents = client.call(
            method = HttpMethod.Get,
            path = apiPath,
            deserializer = DocumentListPojo.serializer(JsonElement.serializer()),
            headers = apiHeaders,
            params = apiParams,
        )
        return DocumentListPojo(
            total = documents.total,
            documents = documents.documents.map { it.asDocument(nestedType) },
        )
    }

    /**
     * List documents
     *
     * Get a list of all the user&#039;s documents in a given collection. You can use the query params to filter your results.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection).
     * @param queries Array of query strings generated using the Query class provided by the SDK. [Learn more about queries](https://appwrite.io/docs/queries). Maximum of 100 queries are allowed, each 4096 characters long.
     * @return [ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentListPojo]
     */
    suspend fun listDocuments(
        databaseId: String,
        collectionId: String,
        queries: List<String>? = null,
    ): DocumentListPojo<DocumentPojo<JsonElement>> {
        return listDocuments(databaseId, collectionId, queries, JsonElement.serializer())
    }

    /**
     * List documents flow
     *
     * Get a list of all the user&#039;s documents in a given collection. You can use the query params to filter your results.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection).
     * @param queries Array of query strings generated using the Query class provided by the SDK. [Learn more about queries](https://appwrite.io/docs/queries). Maximum of 100 queries are allowed, each 4096 characters long.
     * @return [ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentListPojo]
     */
    fun <T> listDocumentsFlow(
        databaseId: String,
        collectionId: String,
        queries: List<String>? = null,
        nestedType: KSerializer<T>,
    ) = channelFlow {
        val result = listDocuments(
            databaseId = databaseId,
            collectionId = collectionId,
            queries = queries,
            nestedType = nestedType,
        )
        send(result.documents.map { it.data })

        realtime.subscribe(
            channels = Channels.documents(databaseId, collectionId),
            payloadType = nestedType,
        ).collect { response ->
            val result = listDocuments(
                databaseId = databaseId,
                collectionId = collectionId,
                queries = queries,
                nestedType = nestedType,
            )
            send(result.documents.map { it.data })
        }
    }

    /**
     * Create document
     *
     * Create a new DocumentPojo. Before using this route, you should create a new collection resource using either a [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection) API or directly from your database console.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection). Make sure to define attributes before creating documents.
     * @param documentId DocumentPojo ID. Choose a custom ID or generate a random ID with `ID.unique()`. Valid chars are a-z, A-Z, 0-9, period, hyphen, and underscore. Can't start with a special char. Max length is 36 chars.
     * @param data DocumentPojo data as JSON object.
     * @param permissions An array of permissions strings. By default, only the current user is granted all permissions. [Learn more about permissions](https://appwrite.io/docs/permissions).
     * @return [ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentPojo]
     */
    suspend fun <T> createDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: T,
        permissions: List<String>? = null,
        nestedType: KSerializer<T>,
    ): DocumentPojo<T> {
        val apiPath = "/databases/{databaseId}/collections/{collectionId}/documents"
            .replace("{databaseId}", databaseId)
            .replace("{collectionId}", collectionId)

        val apiParams = listOf(
            ClientParam.StringParam("documentId", documentId),
            ClientParam.StringParam("data", data.toJson(nestedType)),
            ClientParam.ListParam("permissions", permissions ?: emptyList()),
        )
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        return client.call(
            method = HttpMethod.Post,
            path = apiPath,
            deserializer = JsonElement.serializer(),
            headers = apiHeaders,
            params = apiParams,
        ).asDocument(nestedType)
    }

    /**
     * Create document
     *
     * Create a new DocumentPojo. Before using this route, you should create a new collection resource using either a [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection) API or directly from your database console.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection). Make sure to define attributes before creating documents.
     * @param documentId DocumentPojo ID. Choose a custom ID or generate a random ID with `ID.unique()`. Valid chars are a-z, A-Z, 0-9, period, hyphen, and underscore. Can't start with a special char. Max length is 36 chars.
     * @param data DocumentPojo data as JSON object.
     * @param permissions An array of permissions strings. By default, only the current user is granted all permissions. [Learn more about permissions](https://appwrite.io/docs/permissions).
     * @return [ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentPojo]
     */
    suspend fun createDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: JsonElement,
        permissions: List<String>? = null,
    ): DocumentPojo<JsonElement> {
        return createDocument(databaseId, collectionId, documentId, data, permissions, JsonElement.serializer())
    }

    /**
     * Get document
     *
     * Get a document by its unique ID. This endpoint response returns a JSON object with the document data.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection).
     * @param documentId DocumentPojo ID.
     * @param queries Array of query strings generated using the Query class provided by the SDK. [Learn more about queries](https://appwrite.io/docs/queries). Maximum of 100 queries are allowed, each 4096 characters long.
     * @return [ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentPojo]
     */
    suspend fun <T> getDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        queries: List<String>? = null,
        nestedType: KSerializer<T>,
    ): DocumentPojo<T> {
        val apiPath = "/databases/{databaseId}/collections/{collectionId}/documents/{documentId}"
            .replace("{databaseId}", databaseId)
            .replace("{collectionId}", collectionId)
            .replace("{documentId}", documentId)

        val apiParams = listOf(ClientParam.ListParam("queries", queries ?: emptyList()))
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        return client.call(
            method = HttpMethod.Get,
            path = apiPath,
            deserializer = JsonElement.serializer(),
            headers = apiHeaders,
            params = apiParams,
        ).asDocument(nestedType)
    }

    /**
     * Get document
     *
     * Get a document by its unique ID. This endpoint response returns a JSON object with the document data.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection).
     * @param documentId DocumentPojo ID.
     * @param queries Array of query strings generated using the Query class provided by the SDK. [Learn more about queries](https://appwrite.io/docs/queries). Maximum of 100 queries are allowed, each 4096 characters long.
     * @return [ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentPojo]
     */
    suspend fun getDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        queries: List<String>? = null,
    ): DocumentPojo<JsonElement> {
        return getDocument(databaseId, collectionId, documentId, queries, JsonElement.serializer())
    }

    /**
     * Get document
     *
     * Get a document by its unique ID. This endpoint response returns a JSON object with the document data.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection).
     * @param documentId DocumentPojo ID.
     * @param queries Array of query strings generated using the Query class provided by the SDK. [Learn more about queries](https://appwrite.io/docs/queries). Maximum of 100 queries are allowed, each 4096 characters long.
     * @return [ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentPojo]
     */
    suspend fun <T> getDocumentOrNull(
        databaseId: String,
        collectionId: String,
        documentId: String,
        queries: List<String>? = null,
        nestedType: KSerializer<T>,
    ): DocumentPojo<T>? = handleResult {
        getDocument(databaseId, collectionId, documentId, queries, nestedType)
    }

    /**
     * Get document flow
     *
     * Get a document by its unique ID. This endpoint response returns a JSON object with the document data.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection).
     * @param documentId DocumentPojo ID.
     * @param queries Array of query strings generated using the Query class provided by the SDK. [Learn more about queries](https://appwrite.io/docs/queries). Maximum of 100 queries are allowed, each 4096 characters long.
     * @return [kotlinx.coroutines.flow.Flow]
     */
    fun <T> getDocumentFlow(
        databaseId: String,
        collectionId: String,
        documentId: String,
        queries: List<String>? = null,
        nestedType: KSerializer<T>,
    ) = channelFlow {
        val document = getDocumentOrNull(
            databaseId = databaseId,
            collectionId = collectionId,
            documentId = documentId,
            queries = queries,
            nestedType = nestedType,
        )
        send(document?.data)

        realtime.subscribe(
            channels = Channels.document(databaseId, collectionId, documentId),
            payloadType = nestedType,
        ).collect { response ->
            send(response.payload)
        }
    }

    /**
     * Create or update a Document. Before using this route, you should create a new collection resource using either a [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection) API or directly from your database console.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID.
     * @param documentId Document ID.
     * @param data Document data as JSON object. Include all required attributes of the document to be created or updated.
     * @param permissions An array of permissions strings. By default, the current permissions are inherited. [Learn more about permissions](https://appwrite.io/docs/permissions).
     * @return [ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentPojo]
     */
    suspend fun <T> upsertDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: T,
        permissions: List<String>? = null,
        nestedType: KSerializer<T>,
    ): DocumentPojo<T> {
        val apiPath = "/databases/{databaseId}/collections/{collectionId}/documents/{documentId}"
            .replace("{databaseId}", databaseId)
            .replace("{collectionId}", collectionId)
            .replace("{documentId}", documentId)

        val apiParams = listOf(
            ClientParam.StringParam("data", data.toJson(nestedType)),
            ClientParam.ListParam("permissions", permissions ?: emptyList()),
        )
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        return client.call(
            method = HttpMethod.Put,
            path = apiPath,
            deserializer = JsonElement.serializer(),
            headers = apiHeaders,
            params = apiParams,
        ).asDocument(nestedType)
    }

    /**
     * Create or update a Document. Before using this route, you should create a new collection resource using either a [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection) API or directly from your database console.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID.
     * @param documentId Document ID.
     * @param data Document data as JSON object. Include all required attributes of the document to be created or updated.
     * @param permissions An array of permissions strings. By default, the current permissions are inherited. [Learn more about permissions](https://appwrite.io/docs/permissions).
     * @return [ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentPojo]
     */
    suspend fun upsertDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: JsonElement,
        permissions: List<String>? = null,
    ): DocumentPojo<JsonElement> {
        return upsertDocument(databaseId, collectionId, documentId, data, permissions, JsonElement.serializer())
    }

    /**
     * Update document
     *
     * Update a document by its unique ID. Using the patch method you can pass only specific fields that will get updated.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID.
     * @param documentId DocumentPojo ID.
     * @param data DocumentPojo data as JSON object. Include only attribute and value pairs to be updated.
     * @param permissions An array of permissions strings. By default, the current permissions are inherited. [Learn more about permissions](https://appwrite.io/docs/permissions).
     * @return [ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentPojo]
     */
    suspend fun <T> updateDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: T,
        permissions: List<String>? = null,
        nestedType: KSerializer<T>,
    ): DocumentPojo<T> {
        val apiPath = "/databases/{databaseId}/collections/{collectionId}/documents/{documentId}"
            .replace("{databaseId}", databaseId)
            .replace("{collectionId}", collectionId)
            .replace("{documentId}", documentId)

        val apiParams = listOf(
            ClientParam.StringParam("data", data.toJson(nestedType)),
            ClientParam.ListParam("permissions", permissions ?: emptyList()),
        )
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        return client.call(
            method = HttpMethod.Patch,
            path = apiPath,
            deserializer = JsonElement.serializer(),
            headers = apiHeaders,
            params = apiParams,
        ).asDocument(nestedType)
    }

    /**
     * Update document
     *
     * Update a document by its unique ID. Using the patch method you can pass only specific fields that will get updated.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID.
     * @param documentId DocumentPojo ID.
     * @param data DocumentPojo data as JSON object. Include only attribute and value pairs to be updated.
     * @param permissions An array of permissions strings. By default, the current permissions are inherited. [Learn more about permissions](https://appwrite.io/docs/permissions).
     * @return [ru.aleshin.studyassistant.core.remote.models.appwrite.DocumentPojo]
     */
    suspend fun updateDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: JsonElement,
        permissions: List<String>? = null,
    ): DocumentPojo<JsonElement> {
        return updateDocument(databaseId, collectionId, documentId, data, permissions, JsonElement.serializer())
    }

    /**
     * Delete document
     *
     * Delete a document by its unique ID.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection).
     * @param documentId DocumentPojo ID.
     * @return [Any]
     */
    suspend fun deleteDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
    ) {
        val apiPath = "/databases/{databaseId}/collections/{collectionId}/documents/{documentId}"
            .replace("{databaseId}", databaseId)
            .replace("{collectionId}", collectionId)
            .replace("{documentId}", documentId)

        val apiHeaders = mutableMapOf("content-type" to "application/json")

        return client.call(
            method = HttpMethod.Delete,
            path = apiPath,
            deserializer = Unit.serializer(),
            headers = apiHeaders,
            params = emptyList(),
        )
    }

    private suspend fun <T> handleResult(block: suspend () -> T): T? {
        return try {
            block.invoke()
        } catch (e: Exception) {
            if (e.message?.contains("not be found") == true) {
                null
            } else {
                throw e
            }
        }
    }
}