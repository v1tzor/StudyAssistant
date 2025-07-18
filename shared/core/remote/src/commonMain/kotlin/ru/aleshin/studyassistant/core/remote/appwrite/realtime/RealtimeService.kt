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

package ru.aleshin.studyassistant.core.remote.appwrite.realtime

import co.touchlab.kermit.Logger
import dev.tmapps.konnection.Konnection
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.websocket.Frame
import io.ktor.websocket.close
import io.ktor.websocket.readReason
import io.ktor.websocket.readText
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonElement
import ru.aleshin.studyassistant.core.common.extensions.extractAllItemToSet
import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.jsonCast
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.tryFromJson
import ru.aleshin.studyassistant.core.common.functional.Constants.App.LOGGER_TAG
import ru.aleshin.studyassistant.core.remote.appwrite.AppwriteException
import ru.aleshin.studyassistant.core.remote.appwrite.AppwriteService
import ru.aleshin.studyassistant.core.remote.appwrite.client.AppwriteClient
import ru.aleshin.studyassistant.core.remote.models.appwrite.RealtimeResponse
import ru.aleshin.studyassistant.core.remote.models.appwrite.RealtimeResponseEvent
import ru.aleshin.studyassistant.core.remote.models.appwrite.mapData
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 09.07.2025.
 */
class RealtimeService(
    client: AppwriteClient,
    private val connectionManager: Konnection,
) : AppwriteService(client), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = client.coroutineManager.backgroundDispatcher + mainJob

    private val endpoint: String
        get() = client.endpointRealtime ?: ""

    private val realtimeEndpoint: String
        get() = if (endpoint.endsWith("/")) "${endpoint}realtime" else "$endpoint/realtime"

    private val mutex = Mutex()
    private val mainJob = Job()
    private var sessionJob: Job? = null

    private companion object Companion {
        private const val TYPE_ERROR = "error"
        private const val TYPE_EVENT = "event"

        private const val CLEAN_UP_DELAY = 25L

        private var session: DefaultClientWebSocketSession? = null
        private val activeChannels = mutableMapOf<String, List<String>>()
        private var reconnectAttempts = 0
        private var reconnect = true

        private val responseFlow = MutableSharedFlow<Result<RealtimeResponseEvent<JsonElement>>>()
    }

    suspend fun subscribe(channels: List<String>): Flow<RealtimeResponseEvent<JsonElement>> {
        val subscribeKey = randomUUID()

        updateSession(subscribeKey, channels)

        return responseFlow.map { response ->
            response.getOrThrow()
        }.filter { event ->
            event.channels.any { channels.contains(it) }
        }.onCompletion {
            cleanUpSession(subscribeKey)
        }
    }

    suspend fun <T> subscribe(
        channels: List<String>,
        payloadType: DeserializationStrategy<T>,
    ): Flow<RealtimeResponseEvent<T>> {
        val subscribeKey = randomUUID()

        updateSession(subscribeKey, channels)

        return responseFlow.filter { event ->
            val eventFilter = event.getOrNull()?.channels?.any { channels.contains(it) } == true
            val payloadFilter = event.getOrNull()?.payload?.tryFromJson(payloadType) != null
            return@filter eventFilter && payloadFilter
        }.map { response ->
            response.getOrThrow().mapData(payloadType)
        }.onCompletion {
            cleanUpSession(subscribeKey)
        }
    }

    private suspend fun updateSession(subscribeKey: String, channels: List<String>) = mutex.withLock {
        val launchedChannels = activeChannels.values.toList().extractAllItemToSet()
        activeChannels[subscribeKey] = channels
        val updatedChannels = activeChannels.values.toList().extractAllItemToSet()
        if (launchedChannels != updatedChannels) {
            launchOrRefreshSession(updatedChannels)
        }
    }

    private suspend fun cleanUpSession(subscribeKey: String) = mutex.withLock {
        val launchedChannels = activeChannels.values.toList().extractAllItemToSet()
        activeChannels.remove(subscribeKey)
        val updatedChannels = activeChannels.values.toList().extractAllItemToSet()
        if (launchedChannels != updatedChannels) {
            delay(CLEAN_UP_DELAY)
            launchOrRefreshSession(updatedChannels)
        }
    }

    private fun launchOrRefreshSession(channels: Set<String>) {
        sessionJob?.cancel()

        sessionJob = launch {
            if (session != null) {
                reconnect = false
                closeSession()
            }

            if (channels.isEmpty()) {
                reconnect = false
                closeSession()
                return@launch
            }

            if (!connectionManager.isConnected()) {
                connectionManager.observeHasConnection().filter { it }.first()
            }

            session = try {
                val httpClient = client.createOrGetClient()
                httpClient.webSocketSession {
                    method = HttpMethod.Get
                    url(realtimeEndpoint)
                    parameter("project", client.config["project"])
                    channels.forEach { channel -> parameter("channels[]", channel) }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
            try {
                sessionWork(channels)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun sessionWork(
        channels: Set<String>
    ) = session?.incoming?.receiveAsFlow()?.collect { frame ->
        if (frame is Frame.Text) {
            if (reconnectAttempts > 0) reconnectAttempts = 0
            val rawData = frame.readText()
            val response = rawData.fromJson(RealtimeResponse.serializer(JsonElement.serializer()))
            when (response.type) {
                TYPE_ERROR -> {
                    val exception = response.data.jsonCast(
                        serializer = JsonElement.serializer(),
                        deserializer = AppwriteException.serializer()
                    )
                    responseFlow.emit(Result.failure(exception))
                }
                TYPE_EVENT -> {
                    val event = response.data.jsonCast(
                        serializer = JsonElement.serializer(),
                        deserializer = RealtimeResponseEvent.serializer(JsonElement.serializer()),
                    )
                    responseFlow.emit(Result.success(event))
                }
            }
        } else if (frame is Frame.Close) {
            if (!reconnect) {
                reconnect = true
            } else {
                val timeout = getTimeout()
                val reason = frame.readReason()
                val exception = AppwriteException(
                    message = reason?.message ?: "Realtime disconnected.",
                    code = reason?.code?.toInt(),
                )
                Logger.e(LOGGER_TAG, exception) {
                    "Realtime disconnected. Re-connecting in ${timeout / 1000} seconds."
                }
                delay(timeout)
                reconnectAttempts++
                launchOrRefreshSession(channels)
            }
        }
    }

    private suspend fun closeSession() {
        session?.close()
        session = null
    }

    private fun getTimeout() = when {
        reconnectAttempts < 5 -> 1000L
        reconnectAttempts < 15 -> 5000L
        reconnectAttempts < 100 -> 10000L
        else -> 60000L
    }
}