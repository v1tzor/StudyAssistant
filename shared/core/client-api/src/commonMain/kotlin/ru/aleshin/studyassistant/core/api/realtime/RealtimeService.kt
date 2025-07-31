/*
 * Copyright 2025 Stanislav Aleshin
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

package ru.aleshin.studyassistant.core.api.realtime

import co.touchlab.kermit.Logger
import dev.tmapps.konnection.Konnection
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.parameter
import io.ktor.client.request.url
import io.ktor.http.HttpMethod
import io.ktor.utils.io.CancellationException
import io.ktor.websocket.CloseReason
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
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.io.IOException
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.json.JsonElement
import ru.aleshin.studyassistant.core.api.BaseAppwriteService
import ru.aleshin.studyassistant.core.api.client.AppwriteClient
import ru.aleshin.studyassistant.core.api.models.RealtimeResponse
import ru.aleshin.studyassistant.core.api.models.RealtimeResponseEvent
import ru.aleshin.studyassistant.core.api.models.mapData
import ru.aleshin.studyassistant.core.api.realtime.RealtimeService.Companion.MAX_RECONNECT_ATTEMPTS
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteException
import ru.aleshin.studyassistant.core.common.extensions.extractAllItemToSet
import ru.aleshin.studyassistant.core.common.extensions.jsonCast
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.tryFromJson
import ru.aleshin.studyassistant.core.common.functional.Constants.App.LOGGER_TAG
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import kotlin.coroutines.CoroutineContext

/**
 * A service responsible for managing real-time WebSocket connections to the Appwrite server.
 *
 * It supports subscribing to real-time events from various Appwrite services
 * (e.g., databases, storage, accounts) and ensures automatic reconnection on
 * network loss or socket errors.
 *
 * This service maintains a shared WebSocket session and multiplexes subscriptions over it.
 * It handles reconnection backoff, cancellation, and proper cleanup of resources.
 *
 * @author Develop by Stanislav Aleshin on 31.07.2025.
 */
class RealtimeService(
    client: AppwriteClient,
    private val connectionManager: Konnection,
    private val crashlyticsService: CrashlyticsService,
) : BaseAppwriteService(client), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = client.coroutineManager.backgroundDispatcher + mainJob

    internal val endpoint: String
        get() = client.endpointRealtime ?: ""

    internal val realtimeEndpoint: String
        get() = if (endpoint.endsWith("/")) "${endpoint}realtime" else "$endpoint/realtime"

    private val mainJob = Job()
    private var sessionJob: Job? = null
    private var changeJob: Job? = null

    private var session: DefaultClientWebSocketSession? = null
    private val activeSubscriptions = mutableMapOf<String, List<String>>()
    private var currentListenedChannels = setOf<String>()
    private var reconnectAttempts = 0

    private val responseFlow = MutableSharedFlow<RealtimeResponseEvent<JsonElement>>()

    private companion object Companion {
        private const val TYPE_ERROR = "error"
        private const val TYPE_EVENT = "event"
        private const val ERROR_TAG = "RealtimeServiceError"

        private const val WAIT_MULTIPLE_CHANGES_DELAY = 250L
        private const val MAX_RECONNECT_ATTEMPTS = 100
    }

    /**
     * Subscribes to a list of channels and returns a Flow emitting raw JSON events.
     *
     * @param channels List of Appwrite channel strings.
     */
    fun subscribe(channels: List<String>): Flow<RealtimeResponseEvent<JsonElement>> {
        val subscribeKey = randomUUID()

        appendSubscription(subscribeKey, channels)

        return responseFlow.filter { event ->
            event.channels.any { channels.contains(it) }
        }.onCompletion {
            removeSubscription(subscribeKey)
        }
    }

    /**
     * Subscribes to a list of channels and returns a typed Flow of events.
     *
     * @param channels List of Appwrite channel strings.
     * @param payloadType The deserialization strategy for the event payload.
     */
    fun <T> subscribe(
        channels: List<String>,
        payloadType: DeserializationStrategy<T>,
    ): Flow<RealtimeResponseEvent<T>> {
        val subscribeKey = randomUUID()

        appendSubscription(subscribeKey, channels)

        return responseFlow.filter { event ->
            event.channels.any { channels.contains(it) } && event.payload.tryFromJson(payloadType) != null
        }.map { response ->
            response.mapData(payloadType)
        }.onCompletion {
            removeSubscription(subscribeKey)
        }
    }

    /**
     * Forces a delayed session refresh based on the current active subscription list.
     *
     * Useful when changes are made to subscriptions indirectly and a session update
     * is required. This method ensures that only the latest invocation results in a
     * session refresh, preventing redundant WebSocket restarts.
     */
    fun refreshSession() {
        changeJob?.cancel()
        changeJob = launch {
            delay(WAIT_MULTIPLE_CHANGES_DELAY)
            val updatedChannels = activeSubscriptions.values.toList().extractAllItemToSet()
            launchOrRefreshSession(updatedChannels)
        }
    }

    /**
     * Adds a new subscription entry associated with a unique [subscribeKey] and given [channels].
     *
     * This function schedules a delayed session refresh to avoid launching multiple redundant
     * WebSocket sessions when multiple subscriptions are added concurrently from different threads.
     *
     * If called repeatedly in a short time, only the last scheduled refresh will take effect,
     * as previous jobs are cancelled via [changeJob].
     *
     * @param subscribeKey A unique identifier for this subscription.
     * @param channels List of Appwrite channel strings to subscribe to.
     */
    private fun appendSubscription(subscribeKey: String, channels: List<String>) {
        activeSubscriptions[subscribeKey] = channels
        changeJob?.cancel()
        changeJob = launch {
            delay(WAIT_MULTIPLE_CHANGES_DELAY)
            val updatedChannels = activeSubscriptions.values.toList().extractAllItemToSet()
            if (updatedChannels != currentListenedChannels || session?.isActive != true) {
                launchOrRefreshSession(updatedChannels)
            }
        }
    }

    /**
     * Removes the subscription associated with the provided [subscribeKey].
     *
     * This function also schedules a delayed session refresh to efficiently reflect the
     * updated channel list in the WebSocket connection. Previous refresh jobs are cancelled
     * to ensure only the final update triggers the session change.
     *
     * @param subscribeKey A unique identifier for the subscription to be removed.
     */
    private fun removeSubscription(subscribeKey: String) {
        activeSubscriptions.remove(subscribeKey)
        changeJob?.cancel()
        changeJob = launch {
            delay(WAIT_MULTIPLE_CHANGES_DELAY)
            val updatedChannels = activeSubscriptions.values.toList().extractAllItemToSet()
            if (updatedChannels != currentListenedChannels || session?.isActive != true) {
                launchOrRefreshSession(updatedChannels)
            }
        }
    }

    /**
     * Starts or refreshes the WebSocket session with the provided set of channels.
     *
     * Handles connectivity checks and session cleanup on failure.
     */
    private fun launchOrRefreshSession(channels: Set<String>) {
        sessionJob?.cancel()
        sessionJob = launch {
            if (session != null) {
                closeSession()
            }

            if (channels.isEmpty()) {
                return@launch closeSession()
            }

            if (!connectionManager.isConnected()) {
                connectionManager.observeHasConnection().filter { it }.first()
            }

            try {
                session = client.httpClient().webSocketSession {
                    method = HttpMethod.Get
                    url(realtimeEndpoint)
                    parameter("project", client.projectId)
                    channels.forEach { channel -> parameter("channels[]", channel) }
                }
                currentListenedChannels = channels
                startSessionListener(channels)
            } catch (_: IOException) {
                reconnectWithBackoff(channels)
            } catch (_: CancellationException) {
            } catch (exception: Exception) {
                exception.printStackTrace()
                crashlyticsService.recordException(ERROR_TAG, "", exception)
                closeSession()
            }
        }
    }

    /**
     * Listens to incoming WebSocket frames and emits valid events to the response flow.
     *
     * Reconnects automatically if the session is closed by the server.
     */
    private suspend fun startSessionListener(channels: Set<String>) {
        session?.incoming?.receiveAsFlow()?.collect { frame ->
            if (frame is Frame.Text) {
                if (reconnectAttempts > 0) reconnectAttempts = 0
                val rawData = frame.readText()
                val response = rawData.tryFromJson(RealtimeResponse.serializer(JsonElement.serializer()))
                when (response?.type) {
                    TYPE_ERROR -> {
                        val exception = response.data.jsonCast(
                            serializer = JsonElement.serializer(),
                            deserializer = AppwriteException.serializer()
                        )
                        crashlyticsService.recordException(ERROR_TAG, response.data.toString(), exception)
                    }
                    TYPE_EVENT -> {
                        val event = response.data.jsonCast(
                            serializer = JsonElement.serializer(),
                            deserializer = RealtimeResponseEvent.serializer(JsonElement.serializer()),
                        )
                        responseFlow.emit(event)
                    }
                }
            } else if (frame is Frame.Close) {
                val reason = frame.readReason()
                reconnectWithBackoff(channels, reason)
            }
        }
    }

    /**
     * Closes the current session and clears the active channel state.
     */
    private suspend fun closeSession() {
        session?.close()
        session = null
        currentListenedChannels = emptySet()
    }

    /**
     * Reconnects the WebSocket session with exponential backoff.
     *
     * Stops trying after [MAX_RECONNECT_ATTEMPTS].
     */
    private suspend fun reconnectWithBackoff(channels: Set<String>, reason: CloseReason? = null) {
        val timeout = getTimeout()
        val exception = AppwriteException(
            message = reason?.message ?: "Realtime disconnected.",
            code = reason?.code?.toInt(),
        )
        Logger.e(LOGGER_TAG, exception) {
            "Realtime disconnected. Re-connecting in ${timeout / 1000} seconds."
        }
        delay(timeout)
        reconnectAttempts++
        if (reconnectAttempts <= MAX_RECONNECT_ATTEMPTS) {
            launchOrRefreshSession(channels)
        } else {
            closeSession()
        }
    }

    /**
     * Calculates reconnect timeout based on the current number of attempts.
     */
    private fun getTimeout() = when {
        reconnectAttempts < 5 -> 1_000L
        reconnectAttempts < 15 -> 5_000L
        reconnectAttempts < 40 -> 10_000L
        else -> 60_000L
    }
}