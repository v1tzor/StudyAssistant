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

package ru.aleshin.studyassistant.core.api.databases

import io.ktor.http.HttpMethod
import kotlinx.coroutines.flow.channelFlow
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonElement
import ru.aleshin.studyassistant.core.api.BaseAppwriteService
import ru.aleshin.studyassistant.core.api.client.AppwriteClient
import ru.aleshin.studyassistant.core.api.client.AppwriteClientType
import ru.aleshin.studyassistant.core.api.models.ClientParam
import ru.aleshin.studyassistant.core.api.models.DocumentListPojo
import ru.aleshin.studyassistant.core.api.models.DocumentPojo
import ru.aleshin.studyassistant.core.api.models.asDocument
import ru.aleshin.studyassistant.core.api.realtime.RealtimeService
import ru.aleshin.studyassistant.core.api.utils.Channels
import ru.aleshin.studyassistant.core.common.extensions.jsonCast
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.extensions.toJsonElementWithAppendParams

/**
 * @author Stanislav Aleshin on 09.07.2025.
 */
class DatabaseService(
    client: AppwriteClient,
    private val realtime: RealtimeService,
) : BaseAppwriteService(client) {

    /**
     * List documents
     *
     * Get a list of all the user&#039;s documents in a given collection. You can use the query params to filter your results.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection).
     * @param queries Array of query strings generated using the Query class provided by the SDK. [Learn more about queries](https://appwrite.io/docs/queries). Maximum of 100 queries are allowed, each 4096 characters long.
     * @return [ru.aleshin.studyassistant.core.api.models.DocumentListPojo]
     */
    suspend fun <T> listDocuments(
        databaseId: String,
        collectionId: String,
        queries: List<String>? = null,
        nestedType: KSerializer<T>,
    ): DocumentListPojo<DocumentPojo<T>> {
        val apiPath = "/databases/$databaseId/collections/$collectionId/documents"

        val apiParams = buildList {
            if (!queries.isNullOrEmpty()) {
                add(ClientParam.ListParam("queries", queries))
            }
        }
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
     * @return [ru.aleshin.studyassistant.core.api.models.DocumentListPojo]
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
     * @return [ru.aleshin.studyassistant.core.api.models.DocumentListPojo]
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
        send(result.documents)

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
            send(result.documents)
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
     */
    suspend fun <T> createDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: T,
        permissions: List<String>? = null,
        nestedType: KSerializer<T>,
    ) {
        val apiPath = "/databases/$databaseId/collections/$collectionId/documents"

        val apiParams = buildList {
            add(ClientParam.StringParam("documentId", documentId))
            add(ClientParam.StringParam("data", data.toJson(nestedType)))
            if (!permissions.isNullOrEmpty()) {
                add(ClientParam.ListParam("permissions", permissions))
            }
        }
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        client.call(
            method = HttpMethod.Post,
            path = apiPath,
            deserializer = JsonElement.serializer(),
            headers = apiHeaders,
            params = apiParams,
        )
    }

    /**
     * Create new Documents. Before using this route, you should create a new collection resource using either a [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection) API or directly from your database console.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection). Make sure to define attributes before creating documents.
     * @param documents Array of documents data as JSON objects.
     */
    suspend fun <T> createDocuments(
        databaseId: String,
        collectionId: String,
        documents: List<T>,
        nestedType: KSerializer<T>,
        permissions: List<String>? = null,
    ) {
        val apiPath = "/databases/$databaseId/collections/$collectionId/documents"

        val data = if (permissions.isNullOrEmpty()) {
            documents.map { it.jsonCast(nestedType, JsonElement.serializer()) }
        } else {
            val permissionsParams = listOf(Pair("\$permissions", permissions))
            documents.map { it.toJsonElementWithAppendParams(nestedType, permissionsParams) }
        }

        val apiParams = buildList {
            add(ClientParam.JsonListParam("documents", data))
        }
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        client.call(
            method = HttpMethod.Post,
            path = apiPath,
            clientType = AppwriteClientType.SERVER,
            deserializer = JsonElement.serializer(),
            headers = apiHeaders,
            params = apiParams,
        )
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
     * @return [ru.aleshin.studyassistant.core.api.models.DocumentPojo]
     */
    suspend fun <T> getDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        queries: List<String>? = null,
        nestedType: KSerializer<T>,
    ): DocumentPojo<T> {
        val apiPath = "/databases/$databaseId/collections/$collectionId/documents/$documentId"

        val apiParams = buildList {
            if (!queries.isNullOrEmpty()) {
                add(ClientParam.ListParam("queries", queries))
            }
        }
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        val response = client.call(
            method = HttpMethod.Get,
            path = apiPath,
            deserializer = JsonElement.serializer(),
            headers = apiHeaders,
            params = apiParams,
        )

        return response.asDocument(nestedType)
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
     * @return [ru.aleshin.studyassistant.core.api.models.DocumentPojo]
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
     * @return [ru.aleshin.studyassistant.core.api.models.DocumentPojo]
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
        send(document)

        realtime.subscribe(
            channels = Channels.document(databaseId, collectionId, documentId)
        ).collect { response ->
            send(response.payload.asDocument(nestedType))
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
     */
    suspend fun <T> upsertDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: T,
        permissions: List<String>? = null,
        nestedType: KSerializer<T>,
    ) {
        val apiPath = "/databases/$databaseId/collections/$collectionId/documents/$documentId"

        val apiParams = buildList {
            add(ClientParam.StringParam("data", data.toJson(nestedType)))
            if (!permissions.isNullOrEmpty()) {
                add(ClientParam.ListParam("permissions", permissions))
            }
        }
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        client.call(
            method = HttpMethod.Put,
            path = apiPath,
            deserializer = JsonElement.serializer(),
            headers = apiHeaders,
            params = apiParams,
        )
    }

    /**
     * Create or update Documents. Before using this route, you should create a new collection resource using either a [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection) API or directly from your database console.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID.
     * @param documents Array of document data as JSON objects. May contain partial documents.
     */
    suspend fun <T> upsertDocuments(
        databaseId: String,
        collectionId: String,
        documents: List<T>,
        nestedType: KSerializer<T>,
        permissions: List<String>? = null,
    ) {
        val apiPath = "/databases/$databaseId/collections/$collectionId/documents"

        val data = if (permissions.isNullOrEmpty()) {
            documents.map { it.jsonCast(nestedType, JsonElement.serializer()) }
        } else {
            val permissionsParams = listOf(Pair("\$permissions", permissions))
            documents.map { it.toJsonElementWithAppendParams(nestedType, permissionsParams) }
        }

        val apiParams = buildList {
            add(ClientParam.JsonListParam("documents", data))
        }
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        client.call(
            method = HttpMethod.Put,
            path = apiPath,
            clientType = AppwriteClientType.SERVER,
            headers = apiHeaders,
            params = apiParams,
        )
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
     */
    suspend fun <T> updateDocument(
        databaseId: String,
        collectionId: String,
        documentId: String,
        data: T,
        permissions: List<String>? = null,
        nestedType: KSerializer<T>,
    ) {
        val apiPath = "/databases/$databaseId/collections/$collectionId/documents/$documentId"

        val apiParams = buildList {
            add(ClientParam.StringParam("data", data.toJson(nestedType)))
            if (!permissions.isNullOrEmpty()) {
                add(ClientParam.ListParam("permissions", permissions))
            }
        }
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        client.call(
            method = HttpMethod.Patch,
            path = apiPath,
            headers = apiHeaders,
            params = apiParams,
        )
    }

    /**
     * Update all documents that match your queries, if no queries are submitted then all documents are updated. You can pass only specific fields to be updated.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID.
     * @param documents Document data as JSON object. Include only attribute and value pairs to be updated.
     * @param queries Array of query strings generated using the Query class provided by the SDK. [Learn more about queries](https://appwrite.io/docs/queries). Maximum of 100 queries are allowed, each 4096 characters long.
     */
    suspend fun <T> updateDocuments(
        databaseId: String,
        collectionId: String,
        documents: List<T>,
        queries: List<String>? = null,
        nestedType: KSerializer<T>,
        permissions: List<String>? = null,
    ) {
        val apiPath = "/databases/$databaseId/collections/$collectionId/documents"

        val data = if (permissions.isNullOrEmpty()) {
            documents.map { it.jsonCast(nestedType, JsonElement.serializer()) }
        } else {
            val permissionsParams = listOf(Pair("\$permissions", permissions))
            documents.map { it.toJsonElementWithAppendParams(nestedType, permissionsParams) }
        }

        val apiParams = buildList {
            add(ClientParam.JsonListParam("documents", data))
            if (!queries.isNullOrEmpty()) {
                add(ClientParam.ListParam("queries", queries))
            }
        }
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        client.call(
            method = HttpMethod.Patch,
            path = apiPath,
            clientType = AppwriteClientType.SERVER,
            headers = apiHeaders,
            params = apiParams,
        )
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
        val apiPath = "/databases/$databaseId/collections/$collectionId/documents/$documentId"

        val apiHeaders = mutableMapOf("content-type" to "application/json")

        return client.call(
            method = HttpMethod.Delete,
            path = apiPath,
            headers = apiHeaders,
            params = emptyList(),
        )
    }

    /**
     * Bulk delete documents using queries, if no queries are passed then all documents are deleted.
     *
     * @param databaseId Database ID.
     * @param collectionId Collection ID. You can create a new collection using the Database service [server integration](https://appwrite.io/docs/server/databases#databasesCreateCollection).
     * @param queries Array of query strings generated using the Query class provided by the SDK. [Learn more about queries](https://appwrite.io/docs/queries). Maximum of 100 queries are allowed, each 4096 characters long.
     */
    suspend fun deleteDocuments(
        databaseId: String,
        collectionId: String,
        queries: List<String>? = null,
    ) {
        val apiPath = "/databases/$databaseId/collections/$collectionId/documents"

        val apiParams = buildList {
            if (!queries.isNullOrEmpty()) {
                add(ClientParam.ListParam("queries", queries))
            }
        }
        val apiHeaders = mutableMapOf("content-type" to "application/json")

        client.call(
            method = HttpMethod.Delete,
            path = apiPath,
            clientType = AppwriteClientType.SERVER,
            headers = apiHeaders,
            params = apiParams,
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