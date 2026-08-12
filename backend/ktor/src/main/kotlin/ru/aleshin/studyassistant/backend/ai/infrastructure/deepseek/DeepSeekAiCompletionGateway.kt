/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek

import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.sync.Semaphore
import kotlinx.io.readByteArray
import kotlinx.serialization.SerializationException
import org.slf4j.Logger
import ru.aleshin.studyassistant.backend.ai.domain.gateway.AiCompletionGateway
import ru.aleshin.studyassistant.backend.ai.domain.model.AiCompletionRequest
import ru.aleshin.studyassistant.backend.ai.domain.result.AiProviderResult
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekChatResponseDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.mappers.DeepSeekMapper
import java.io.IOException
import java.time.Clock
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.min
import kotlin.random.Random

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class DeepSeekAiCompletionGateway(
    private val httpClient: HttpClient,
    private val config: DeepSeekConfig,
    private val mapper: DeepSeekMapper,
    private val clock: Clock,
    private val random: Random,
    private val logger: Logger,
) : AiCompletionGateway {

    private val concurrencyLimiter = Semaphore(config.maxConcurrentRequests)

    override suspend fun complete(request: AiCompletionRequest): AiProviderResult {
        return withTimeoutOrNull(config.totalTimeoutMs) {
            val acquired = withTimeoutOrNull(config.queueTimeoutMs) {
                concurrencyLimiter.acquire()
                true
            } ?: false
            if (!acquired) return@withTimeoutOrNull AiProviderResult.Unavailable

            try {
                completeWithinDeadline(request = request)
            } finally {
                concurrencyLimiter.release()
            }
        } ?: AiProviderResult.Unavailable
    }

    private suspend fun completeWithinDeadline(request: AiCompletionRequest): AiProviderResult {
        val providerRequest = mapper.mapRequest(request = request)

        repeat(config.maxRetries + 1) { attempt ->
            val response = try {
                httpClient.post(config.baseUrl + CHAT_COMPLETIONS_PATH) {
                    bearerAuth(config.apiKey)
                    contentType(ContentType.Application.Json)
                    setBody(providerRequest)
                }
            } catch (cause: CancellationException) {
                throw cause
            } catch (_: HttpRequestTimeoutException) {
                if (attempt == config.maxRetries) {
                    return AiProviderResult.Unavailable
                }
                delayBeforeRetry(attempt = attempt, retryAfter = null)
                return@repeat
            } catch (_: IOException) {
                if (attempt == config.maxRetries) {
                    return AiProviderResult.Unavailable
                }
                delayBeforeRetry(attempt = attempt, retryAfter = null)
                return@repeat
            }

            val retryAfter = response.headers[HttpHeaders.RetryAfter]

            if (response.status == HttpStatusCode.OK) {
                val providerResponse = readProviderResponse(response = response)

                val insufficientResources = providerResponse
                    ?.choices
                    ?.singleOrNull()
                    ?.finishReason == INSUFFICIENT_RESOURCES_FINISH_REASON

                val completion = providerResponse
                    ?.takeUnless { insufficientResources }
                    ?.let { response ->
                        mapper.mapResponse(
                            response = response,
                            request = request,
                        )
                    }

                if (completion != null) {
                    return AiProviderResult.Success(completion = completion)
                }

                if (attempt == config.maxRetries) {
                    logger.warn("DeepSeek returned an invalid or incomplete response")
                    return AiProviderResult.Unavailable
                }

                delayBeforeRetry(attempt = attempt, retryAfter = null)
                return@repeat
            }

            response.bodyAsChannel().cancel(cause = null)

            when (response.status.value) {
                400, 422 -> {
                    logger.error("DeepSeek rejected the server request with status {}", response.status.value)
                    return AiProviderResult.InvalidRequest
                }

                401, 403 -> {
                    logger.error("DeepSeek credentials were rejected with status {}", response.status.value)
                    return AiProviderResult.Unauthorized
                }

                402 -> {
                    logger.error("DeepSeek account has insufficient balance")
                    return AiProviderResult.InsufficientBalance
                }

                429 -> {
                    if (attempt == config.maxRetries) {
                        return AiProviderResult.RateLimited(
                            retryAfterSeconds = retryAfterSeconds(value = retryAfter),
                        )
                    }
                }

                in 500..599 -> {
                    if (attempt == config.maxRetries) {
                        return AiProviderResult.Unavailable
                    }
                }

                else -> {
                    logger.error("DeepSeek returned unexpected status {}", response.status.value)
                    return AiProviderResult.InvalidRequest
                }
            }

            delayBeforeRetry(attempt = attempt, retryAfter = retryAfter)
        }

        return AiProviderResult.Unavailable
    }

    private suspend fun readProviderResponse(
        response: HttpResponse,
    ): DeepSeekChatResponseDto? {
        val channel = response.bodyAsChannel()
        val bytes = try {
            channel
                .readRemaining(config.maxResponseBodyBytes + 1L)
                .readByteArray()
        } catch (cause: CancellationException) {
            throw cause
        } catch (_: IOException) {
            return null
        }

        if (bytes.size > config.maxResponseBodyBytes) {
            channel.cancel(cause = null)
            logger.warn("DeepSeek response exceeded the configured body limit")
            return null
        }

        return try {
            DeepSeekJson.decodeFromString<DeepSeekChatResponseDto>(bytes.decodeToString())
        } catch (_: SerializationException) {
            null
        } catch (_: IllegalArgumentException) {
            null
        }
    }

    private suspend fun delayBeforeRetry(attempt: Int, retryAfter: String?) {
        val retryAfterMs = retryAfterMillis(value = retryAfter)
        val delayMs = retryAfterMs ?: run {
            val multiplier = 1L shl min(attempt, MAX_SHIFT)
            val exponential = if (config.baseRetryDelayMs > config.maxRetryDelayMs / multiplier) {
                config.maxRetryDelayMs
            } else {
                config.baseRetryDelayMs * multiplier
            }
            val jitter = if (config.retryJitterMs == 0L) {
                0L
            } else {
                random.nextLong(from = 0L, until = config.retryJitterMs + 1L)
            }
            exponential + jitter
        }

        delay(delayMs)
    }

    private fun retryAfterSeconds(value: String?): Long? {
        val millis = retryAfterMillis(value = value) ?: return null
        return (millis + MILLIS_PER_SECOND - 1L) / MILLIS_PER_SECOND
    }

    private fun retryAfterMillis(value: String?): Long? {
        if (value.isNullOrBlank()) {
            return null
        }

        value.toLongOrNull()?.let { seconds ->
            return seconds
                .takeIf { it >= 0L }
                ?.let { value ->
                    runCatching {
                        Math.multiplyExact(value, MILLIS_PER_SECOND)
                    }.getOrNull()
                }
        }

        return runCatching {
            val retryAt = ZonedDateTime
                .parse(value, DateTimeFormatter.RFC_1123_DATE_TIME)
                .toInstant()
            Duration
                .between(clock.instant(), retryAt)
                .toMillis()
                .coerceAtLeast(0L)
        }.getOrNull()
    }

    private companion object {

        const val CHAT_COMPLETIONS_PATH = "/chat/completions"
        const val INSUFFICIENT_RESOURCES_FINISH_REASON = "insufficient_system_resource"
        const val MILLIS_PER_SECOND = 1_000L
        const val MAX_SHIFT = 30
    }
}
