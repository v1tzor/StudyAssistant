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

package ru.aleshin.studyassistant.core.remote.appwrite.client

import dev.tmapps.konnection.Konnection
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.cache.HttpCache
import io.ktor.client.plugins.cache.storage.CacheStorage
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.CookiesStorage
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
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.addAll
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.common.extensions.Decode
import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.getString
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.extensions.toJsonElement
import ru.aleshin.studyassistant.core.common.functional.Constants.App.LOGGER_TAG
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.remote.BuildKonfig
import ru.aleshin.studyassistant.core.remote.appwrite.AppwriteException
import ru.aleshin.studyassistant.core.remote.ktor.HttpEngineFactory
import ru.aleshin.studyassistant.core.remote.models.appwrite.ClientParam
import ru.aleshin.studyassistant.core.remote.models.appwrite.FilePojo
import ru.aleshin.studyassistant.core.remote.models.appwrite.ProgressPojo
import ru.aleshin.studyassistant.core.remote.models.appwrite.UploadProgressPojo
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
class AppwriteClient(
    headersProvider: AppwriteHeadersProvider,
    internal val coroutineManager: CoroutineManager,
    private val engineFactory: HttpEngineFactory,
    private val cookiesStorage: CookiesStorage,
    private val cacheStorage: CacheStorage,
    private val connectionManager: Konnection,
) : CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = coroutineManager.backgroundDispatcher + job

    internal lateinit var httpClient: HttpClient

    internal val headers: MutableMap<String, String> = headersProvider.fetchHeaders()
    internal val config: MutableMap<String, String> = mutableMapOf()

    internal var endpoint: String = "https://cloud.appwrite.io/v1"
    internal var endpointRealtime: String? = null
    internal var selfSigned: Boolean = false

    private val job = Job()
    private var updated: Boolean = true

    private companion object {
        const val CHUNK_SIZE = 5 * 1024 * 1024 // 5MB
    }

    /**
     * Set Project
     *
     * Your project ID
     *
     * @param {string} project
     *
     * @return this
     */
    fun setProject(value: String): AppwriteClient {
        config["project"] = value
        addHeader("x-appwrite-project", value)
        return this
    }

    /**
     * Set Key
     *
     * Your secret API key
     *
     * @param {string} key
     *
     * @return this
     */
    fun setKey(value: String): AppwriteClient {
        if (value.isEmpty()) return this
        config["key"] = value
        addHeader("x-appwrite-key", value)
        return this
    }

    /**
     * Set JWT
     *
     * Your secret JSON Web Token
     *
     * @param {string} jwt
     *
     * @return this
     */
    fun setJWT(value: String): AppwriteClient {
        config["jWT"] = value
        addHeader("x-appwrite-jwt", value)
        return this
    }

    /**
     * Set Locale
     *
     * @param {string} locale
     *
     * @return this
     */
    fun setLocale(value: String): AppwriteClient {
        config["locale"] = value
        addHeader("x-appwrite-locale", value)
        return this
    }

    /**
     * Set Session
     *
     * The user session to authenticate with
     *
     * @param {string} session
     *
     * @return this
     */
    fun setSession(value: String): AppwriteClient {
        config["session"] = value
        addHeader("x-appwrite-session", value)
        return this
    }

    /**
     * Set self Signed
     *
     * @param status
     *
     * @return this
     */
    fun setSelfSigned(status: Boolean): AppwriteClient {
        selfSigned = status
        updated = true
        return this
    }

    /**
     * Set endpoint and realtime endpoint.
     *
     * @param endpoint
     *
     * @return this
     */
    fun setEndpoint(endpoint: String): AppwriteClient {
        this.endpoint = endpoint

        if (this.endpointRealtime == null && endpoint.startsWith("http")) {
            this.endpointRealtime = endpoint.replaceFirst("http", "ws")
        }
        updated = true
        return this
    }

    /**
     * Set realtime endpoint
     *
     * @param endpoint
     *
     * @return this
     */
    fun setEndpointRealtime(endpoint: String): AppwriteClient {
        this.endpointRealtime = endpoint
        updated = true
        return this
    }

    /**
     * Add Header
     *
     * @param key
     * @param value
     *
     * @return this
     */
    fun addHeader(
        key: String,
        value: String,
    ): AppwriteClient {
        headers[key] = value
        updated = true
        return this
    }

    internal fun createOrGetClient(): HttpClient {
        if (!updated) return httpClient
        httpClient = HttpClient(engineFactory.createEngine()) {
            install(HttpCache)
            install(HttpCookies) {
                storage = cookiesStorage
            }
            install(Logging) {
                level = if (BuildKonfig.IS_DEBUG) LogLevel.ALL else LogLevel.NONE
                logger = object : Logger {
                    override fun log(message: String) {
                        co.touchlab.kermit.Logger.i(LOGGER_TAG) { message }
                    }
                }
            }
            install(DefaultRequest) {
                if (endpoint.endsWith("/")) {
                    url(endpoint)
                } else {
                    url("$endpoint/")
                }
                this.headers.append(HttpHeaders.CacheControl, "private, max-age=3600")
                this@AppwriteClient.headers.forEach {
                    headers.append(it.key, it.value)
                }
            }
            install(WebSockets) {
                pingInterval = 0.seconds
            }
            install(ContentNegotiation) {
                json(Json.Decode)
            }
        }
        updated = false
        return httpClient
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
        onUpload: ((ProgressPojo) -> Unit)? = null,
        onDownload: ((ProgressPojo) -> Unit)? = null,
    ): T {
        return try {
            val path = path.replaceFirst("/", "")
            if (connectionManager.isConnected()) {
                val httpClient = createOrGetClient()
                val response = httpClient.request(path) {
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
                val body = response.bodyAsText()
                if (response.status.isSuccess()) {
                    body.fromJson(deserializer)
                } else {
                    throw body.fromJson<AppwriteException>()
                }
            } else {
                throw InternetConnectionException()
            }
        } catch (exception: Exception) {
            exception.printStackTrace()
            throw exception
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
        onUpload: ((ProgressPojo) -> Unit)? = null,
        onDownload: ((ProgressPojo) -> Unit)? = null,
    ) {
        if (!connectionManager.isConnected()) throw InternetConnectionException()

        val path = path.replaceFirst("/", "")
        val httpClient = createOrGetClient()
        val response = httpClient.request(path) {
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
    }

    private fun HttpRequestBuilder.setupGetRequest(
        params: List<ClientParam> = emptyList(),
    ) {
        params.forEach { param ->
            when (param) {
                is ClientParam.FileParam -> {}
                is ClientParam.ListParam -> {
                    param.value.forEach { paramValue ->
                        parameter("${param.key}[]", paramValue)
                    }
                }

                is ClientParam.StringParam -> {
                    parameter(param.key, param.value)
                }

                is ClientParam.MapParam -> {}
            }
        }
    }

    private fun HttpRequestBuilder.setupPostMultipartDataRequest(
        params: List<ClientParam> = emptyList(),
    ) {
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
    private fun HttpRequestBuilder.setupPostJsonRequest(
        params: List<ClientParam> = emptyList(),
    ) {
        val bodyMap = buildJsonObject {
            params.forEach { param ->
                when (param) {
                    is ClientParam.FileParam -> {}
                    is ClientParam.ListParam -> {
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
}