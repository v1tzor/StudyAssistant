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

package ru.aleshin.studyassistant.core.api.client

import dev.tmapps.konnection.Konnection
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngineConfig
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.onDownload
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.parameter
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.io.IOException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.addAll
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import ru.aleshin.studyassistant.core.api.AppwriteApi.Client.ENDPOINT
import ru.aleshin.studyassistant.core.api.AppwriteApi.Client.ENDPOINT_REALTIME
import ru.aleshin.studyassistant.core.api.AppwriteApi.Client.PROJECT_ID
import ru.aleshin.studyassistant.core.api.BuildKonfig.APPWRITE_SERVER_KEY
import ru.aleshin.studyassistant.core.api.BuildKonfig.IS_DEBUG
import ru.aleshin.studyassistant.core.api.cookies.PreferencesCookiesStorage
import ru.aleshin.studyassistant.core.api.models.ClientParam
import ru.aleshin.studyassistant.core.api.models.FilePojo
import ru.aleshin.studyassistant.core.api.models.ProgressPojo
import ru.aleshin.studyassistant.core.api.models.UploadProgressPojo
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteException
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.extensions.Decode
import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.getString
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.extensions.toJsonElement
import ru.aleshin.studyassistant.core.common.functional.Constants.App.LOGGER_TAG
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
class AppwriteClient private constructor(
    internal val endpoint: String,
    internal val endpointRealtime: String?,
    internal val coroutineManager: CoroutineManager,
    private val cookiesStorage: PreferencesCookiesStorage,
    private val connectionManager: Konnection,
    private val baseHttpClient: HttpClient,
    private val serverHttpClient: HttpClient,
    private val serverHeaders: Map<String, String>,
    private val clientHeaders: Map<String, String>,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = coroutineManager.backgroundDispatcher + job

    internal val projectId: String?
        get() = clientHeaders["x-appwrite-project"]

    private val job = Job()

    companion object {

        private const val CHUNK_SIZE = 5 * 1024 * 1024 // 5MB

        class Creator(
            headersProvider: AppwriteHeadersProvider,
            internal val coroutineManager: CoroutineManager,
            private val httpClientEngineFactory: HttpClientEngineFactory<HttpClientEngineConfig>,
            private val cookiesStorage: PreferencesCookiesStorage,
            private val connectionManager: Konnection,
        ) {
            private val clientHeaders = headersProvider.fetchBaseClientHeaders()

            private val serverHeaders = headersProvider.fetchBaseServerHeaders()

            fun setup(
                endpoint: String = ENDPOINT,
                endpointRealtime: String = ENDPOINT_REALTIME,
                projectId: String = PROJECT_ID,
                serverKey: String = APPWRITE_SERVER_KEY,
            ): AppwriteClient {
                setProject(projectId)
                setKey(serverKey)
                addHeader("X-Appwrite-Dev-Key", "b613d52e6e989fb42bf900d6c83d061ccb43c464954e1aca193332a2b4831e951ff5aa40c3ce6028c405da17c8534f746ea5f3651b86eda5bdaeffd6af67d270d259c517375144ee72910ddd8ec0c26ba9d13214c634b5bf09f57086ae62875852b746efc2dd1c65ba9311e18388355a28eda9dab7b6c1a82376e023063edcd0")

                val baseHttpClient = createHttpClient(AppwriteClientType.CLIENT, endpoint)
                val serverHttpClient = createHttpClient(AppwriteClientType.SERVER, endpoint)

                return AppwriteClient(
                    coroutineManager = coroutineManager,
                    connectionManager = connectionManager,
                    cookiesStorage = cookiesStorage,
                    baseHttpClient = baseHttpClient,
                    serverHttpClient = serverHttpClient,
                    clientHeaders = clientHeaders,
                    serverHeaders = serverHeaders,
                    endpoint = endpoint,
                    endpointRealtime = endpointRealtime,
                )
            }

            private fun addHeader(key: String, value: String) {
                clientHeaders[key] = value
                serverHeaders[key] = value
            }

            private fun setProject(value: String) {
                addHeader("x-appwrite-project", value)
            }

            private fun setKey(value: String) {
                if (value.isEmpty()) return
                serverHeaders["x-appwrite-key"] = value
            }

            private fun createHttpClient(type: AppwriteClientType, endpoint: String): HttpClient {
                return HttpClient(httpClientEngineFactory) {
                    if (type == AppwriteClientType.CLIENT) {
                        install(HttpCookies) { storage = cookiesStorage }
                    }
                    install(WebSockets) { pingInterval = 20.seconds }
                    install(ContentNegotiation) { json(Json.Decode) }
                    install(Logging) {
                        level = if (IS_DEBUG) LogLevel.ALL else LogLevel.NONE
                        logger = object : Logger {
                            override fun log(message: String) {
                                co.touchlab.kermit.Logger.i(LOGGER_TAG) { message }
                            }
                        }
                    }
                    install(HttpTimeout) {
                        socketTimeoutMillis = 10_000
                        requestTimeoutMillis = 15_000
                        connectTimeoutMillis = 10_000
                    }
                    install(DefaultRequest) {
                        if (endpoint.endsWith("/")) url(endpoint) else url("$endpoint/")
                        when (type) {
                            AppwriteClientType.CLIENT -> clientHeaders.forEach {
                                headers.append(it.key, it.value)
                            }
                            AppwriteClientType.SERVER -> serverHeaders.forEach {
                                headers.append(it.key, it.value)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Send the HTTP request
     *
     * @param method
     * @param path
     * @param headers
     * @param params
     *
     * @return String
     */
    suspend fun <T> call(
        method: HttpMethod,
        path: String,
        deserializer: DeserializationStrategy<T>,
        headers: Map<String, String> = mapOf(),
        params: List<ClientParam> = emptyList(),
        clientType: AppwriteClientType = AppwriteClientType.CLIENT,
        onUpload: ((ProgressPojo) -> Unit)? = null,
        onDownload: ((ProgressPojo) -> Unit)? = null,
    ): T {
        if (!connectionManager.isConnected()) throw InternetConnectionException()

        try {
            val path = path.replaceFirst("/", "")
            val httpClient = when (clientType) {
                AppwriteClientType.CLIENT -> baseHttpClient
                AppwriteClientType.SERVER -> serverHttpClient
            }
            val response = httpClient.request(path) {
                this.method = method
                if (method == HttpMethod.Get) {
                    setupGetRequest(params)
                } else if (headers["content-type"] == ContentType.MultiPart.FormData.toString()) {
                    setupPostMultipartDataRequest(params)
                } else {
                    setupPostJsonRequest(params)
                }

                headers.forEach { this.headers.append(it.key, it.value) }

                if (onUpload != null) {
                    onUpload { sent, total ->
                        onUpload(ProgressPojo(total ?: 0L, sent))
                    }
                }
                if (onDownload != null) {
                    onDownload { received, total ->
                        onDownload(ProgressPojo(total ?: 0, received))
                    }
                }
            }
            val body = response.bodyAsText()
            return if (response.status.isSuccess()) {
                body.fromJson(deserializer)
            } else {
                throw body.fromJson<AppwriteException>()
            }
        } catch (e: IOException) {
            throw InternetConnectionException()
        }
    }

    /**
     * Send the HTTP request
     *
     * @param method
     * @param path
     * @param headers
     * @param params
     *
     */
    suspend fun call(
        method: HttpMethod,
        path: String,
        headers: Map<String, String> = mapOf(),
        params: List<ClientParam> = emptyList(),
        clientType: AppwriteClientType = AppwriteClientType.CLIENT,
        onUpload: ((ProgressPojo) -> Unit)? = null,
        onDownload: ((ProgressPojo) -> Unit)? = null,
    ) {
        if (!connectionManager.isConnected()) throw InternetConnectionException()

        val path = path.replaceFirst("/", "")
        try {
            val response = httpClient(clientType).request(path) {
                this.method = method
                if (method == HttpMethod.Get) {
                    setupGetRequest(params)
                } else if (headers["content-type"] == ContentType.MultiPart.FormData.toString()) {
                    setupPostMultipartDataRequest(params)
                } else {
                    setupPostJsonRequest(params)
                }

                headers.forEach {
                    this.headers.append(it.key, it.value)
                }

                if (onUpload != null) {
                    onUpload { sent, total ->
                        onUpload(ProgressPojo(total ?: 0L, sent))
                    }
                }
                if (onDownload != null) {
                    onDownload { received, total ->
                        onDownload(ProgressPojo(total ?: 0, received))
                    }
                }
            }
            if (!response.status.isSuccess()) {
                throw response.bodyAsText().fromJson<AppwriteException>()
            }
        } catch (e: IOException) {
            throw InternetConnectionException()
        }
    }

    /**
     * Upload a file in chunks
     *
     * @param path
     * @param headers
     * @param params
     *
     * @return [T]
     */
    suspend fun <T> chunkedUpload(
        path: String,
        deserializer: DeserializationStrategy<T>,
        headers: MutableMap<String, String>,
        params: List<ClientParam>,
        clientType: AppwriteClientType = AppwriteClientType.CLIENT,
        onProgress: ((UploadProgressPojo) -> Unit)? = null,
    ): T {
        val fileId = params.find { it is ClientParam.StringParam && it.key == "fileId" } as? ClientParam.StringParam
            ?: throw IllegalArgumentException("chunkedUpload required StringParam (key: fileId) in params")

        var fileParam = params.find { it is ClientParam.FileParam } as? ClientParam.FileParam
            ?: throw IllegalArgumentException("chunkedUpload required FileParm in params")

        val size = fileParam.data.size.toLong()

        if (size < CHUNK_SIZE) {
            return call(
                method = HttpMethod.Post,
                path = path,
                deserializer = deserializer,
                headers = headers,
                params = params,
                clientType = clientType,
            )
        }

        val buffer = ByteArray(CHUNK_SIZE)
        var offset = 0L
        var result: JsonElement? = null

        val current = call(
            method = HttpMethod.Get,
            path = "$path/${fileId.value}",
            deserializer = FilePojo.serializer(),
            headers = headers,
            params = emptyList(),
            clientType = clientType,
        )
        val chunksUploaded = current.chunksUploaded
        offset = chunksUploaded * CHUNK_SIZE

        while (offset < size) {
            val end = if (offset + CHUNK_SIZE < size) offset + CHUNK_SIZE - 1 else size - 1

            fileParam.data.copyInto(
                buffer,
                startIndex = offset.toInt(),
                endIndex = end.toInt()
            )

            fileParam = ClientParam.FileParam(fileParam.fileName, buffer)

            headers["Content-Range"] = "bytes $offset-${((offset + CHUNK_SIZE) - 1).coerceAtMost(size - 1)}/$size"

            result = call(
                method = HttpMethod.Post,
                path = path,
                deserializer = JsonElement.serializer(),
                headers = headers,
                params = params,
                clientType = clientType,
            )

            offset += CHUNK_SIZE

            headers["x-appwrite-id"] = result.getString("\$id")

            onProgress?.invoke(
                UploadProgressPojo(
                    id = result.getString("\$id"),
                    progress = offset.coerceAtMost(size).toDouble() / size * 100,
                    sizeUploaded = offset.coerceAtMost(size),
                    chunksTotal = result.getString("chunksTotal").toInt(),
                    chunksUploaded = result.getString("chunksUploaded").toInt(),
                )
            )
        }

        return checkNotNull(result?.fromJson(deserializer)) {
            "chunkedUpload result is null"
        }
    }

    fun httpClient(type: AppwriteClientType = AppwriteClientType.CLIENT) = when (type) {
        AppwriteClientType.CLIENT -> baseHttpClient
        AppwriteClientType.SERVER -> serverHttpClient
    }

    fun clearCookies() {
        cookiesStorage.cleanStorage()
    }

    private fun HttpRequestBuilder.setupGetRequest(params: List<ClientParam>) {
        params.forEach { param ->
            when (param) {
                is ClientParam.FileParam -> {}
                is ClientParam.ListParam -> {
                    param.value.forEach { paramValue ->
                        parameter("${param.key}[]", paramValue)
                    }
                }
                is ClientParam.JsonListParam -> {}
                is ClientParam.StringParam -> {
                    parameter(param.key, param.value)
                }
                is ClientParam.MapParam -> {}
            }
        }
    }

    private fun HttpRequestBuilder.setupPostMultipartDataRequest(params: List<ClientParam>) {
        val formDataContent = formData {
            params.forEach { param ->
                when (param) {
                    is ClientParam.FileParam -> {
                        val headers = Headers.build {
                            append(HttpHeaders.ContentType, ContentType.Application.OctetStream.toString())
                            append(HttpHeaders.ContentDisposition, "filename=\"${param.fileName}\"")
                        }
                        append("file", param.data, headers)
                    }
                    is ClientParam.ListParam -> {
                        param.value.forEach { paramValue ->
                            append("${param.key}[]", paramValue)
                        }
                    }
                    is ClientParam.JsonListParam -> {
                        param.value.forEach { paramValue ->
                            append("${param.key}[]", paramValue.toString())
                        }
                    }
                    is ClientParam.StringParam -> {
                        append(param.key, param.value.toString())
                    }
                    is ClientParam.MapParam -> {}
                }
            }
        }
        setBody(MultiPartFormDataContent(formDataContent))
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun HttpRequestBuilder.setupPostJsonRequest(params: List<ClientParam>) {
        val bodyMap = buildJsonObject {
            params.forEach { param ->
                when (param) {
                    is ClientParam.FileParam -> {}
                    is ClientParam.ListParam -> {
                        putJsonArray(param.key) {
                            addAll(param.value)
                        }
                    }
                    is ClientParam.JsonListParam -> {
                        putJsonArray(param.key) {
                            addAll(param.value)
                        }
                    }
                    is ClientParam.StringParam -> {
                        put(param.key, param.value)
                    }
                    is ClientParam.MapParam -> {
                        putJsonObject(param.key) {
                            param.value.forEach { entry ->
                                put(entry.key, entry.value.toJsonElement())
                            }
                        }
                    }
                }
            }
        }
        setBody(bodyMap.toJson())
    }
}