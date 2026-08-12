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
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import org.slf4j.helpers.NOPLogger
import ru.aleshin.studyassistant.backend.ai.domain.model.AiCompletionRequest
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessage
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessageRole
import ru.aleshin.studyassistant.backend.ai.domain.result.AiProviderResult
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.mappers.DeepSeekMapper
import ru.aleshin.studyassistant.backend.ai.testDeepSeekConfig
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class DeepSeekAiCompletionGatewayTest {

    @Test
    fun transientFailureShouldRetryAndReturnCompletion() = runBlocking {
        val attempts = AtomicInteger()
        val config = testDeepSeekConfig(maxRetries = 1)
        val client = client(
            engine = MockEngine {
                if (attempts.getAndIncrement() == 0) {
                    respond(
                        content = ByteReadChannel(""),
                        status = HttpStatusCode.ServiceUnavailable,
                    )
                } else {
                    jsonResponse(status = HttpStatusCode.OK, content = SUCCESS_RESPONSE)
                }
            },
        )

        client.use {
            val result = gateway(client = client, config = config).complete(request = request())

            assertIs<AiProviderResult.Success>(result)
            assertEquals("Hello", result.completion.content)
            assertEquals(2, attempts.get())
        }
    }

    @Test
    fun unauthorizedShouldNotRetry() = runBlocking {
        val attempts = AtomicInteger()
        val config = testDeepSeekConfig(maxRetries = 2)
        val client = client(
            engine = MockEngine {
                attempts.incrementAndGet()
                respond(
                    content = ByteReadChannel(""),
                    status = HttpStatusCode.Unauthorized,
                )
            },
        )

        client.use {
            val result = gateway(client = client, config = config).complete(request = request())

            assertIs<AiProviderResult.Unauthorized>(result)
            assertEquals(1, attempts.get())
        }
    }

    @Test
    fun finalRateLimitShouldExposeRetryAfterWithoutProviderBody() = runBlocking {
        val config = testDeepSeekConfig()
        val client = client(
            engine = MockEngine {
                respond(
                    content = ByteReadChannel("sensitive provider details"),
                    status = HttpStatusCode.TooManyRequests,
                    headers = headersOf(HttpHeaders.RetryAfter, "7"),
                )
            },
        )

        client.use {
            val result = gateway(client = client, config = config).complete(request = request())

            assertIs<AiProviderResult.RateLimited>(result)
            assertEquals(7, result.retryAfterSeconds)
        }
    }

    @Test
    fun malformedSuccessShouldBeUnavailable() = runBlocking {
        val config = testDeepSeekConfig()
        val client = client(
            engine = MockEngine {
                jsonResponse(
                    status = HttpStatusCode.OK,
                    content = "{\"choices\":[]}",
                )
            },
        )

        client.use {
            val result = gateway(client = client, config = config).complete(request = request())

            assertIs<AiProviderResult.Unavailable>(result)
        }
        Unit
    }

    @Test
    fun oversizedSuccessShouldBeUnavailable() = runBlocking {
        val config = testDeepSeekConfig(maxResponseBodyBytes = 16)
        val client = client(
            engine = MockEngine {
                jsonResponse(
                    status = HttpStatusCode.OK,
                    content = SUCCESS_RESPONSE,
                )
            },
        )

        client.use {
            val result = gateway(client = client, config = config).complete(request = request())

            assertIs<AiProviderResult.Unavailable>(result)
        }
        Unit
    }

    @Test
    fun concurrencyLimitShouldRejectExcessRequest() = runBlocking {
        val firstRequestStarted = CompletableDeferred<Unit>()
        val releaseFirstRequest = CompletableDeferred<Unit>()
        val config = testDeepSeekConfig(maxConcurrentRequests = 1)
        val client = client(
            engine = MockEngine {
                firstRequestStarted.complete(Unit)
                releaseFirstRequest.await()
                jsonResponse(status = HttpStatusCode.OK, content = SUCCESS_RESPONSE)
            },
        )

        client.use {
            val gateway = gateway(client = client, config = config)
            val first = async { gateway.complete(request = request()) }
            firstRequestStarted.await()

            assertIs<AiProviderResult.Unavailable>(gateway.complete(request = request()))

            releaseFirstRequest.complete(Unit)
            assertIs<AiProviderResult.Success>(first.await())
        }
        Unit
    }

    private fun client(engine: MockEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(DeepSeekJson)
            }
        }
    }

    private fun gateway(
        client: HttpClient,
        config: DeepSeekConfig,
    ): DeepSeekAiCompletionGateway {
        return DeepSeekAiCompletionGateway(
            httpClient = client,
            config = config,
            mapper = DeepSeekMapper(config = config),
            clock = Clock.fixed(
                Instant.parse("2026-08-12T00:00:00Z"),
                ZoneOffset.UTC,
            ),
            random = Random(seed = 0),
            logger = NOPLogger.NOP_LOGGER,
        )
    }

    private fun request(): AiCompletionRequest {
        return AiCompletionRequest(
            messages = listOf(
                AiMessage(
                    role = AiMessageRole.USER,
                    content = "Hello",
                ),
            ),
            tools = emptyList(),
        )
    }

    private fun io.ktor.client.engine.mock.MockRequestHandleScope.jsonResponse(
        status: HttpStatusCode,
        content: String,
    ) = respond(
        content = ByteReadChannel(content),
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    private companion object {

        const val SUCCESS_RESPONSE =
            "{\"choices\":[{\"message\":{\"role\":\"assistant\",\"content\":\"Hello\"}," +
                "\"finish_reason\":\"stop\"}],\"usage\":{\"prompt_tokens\":1," +
                "\"completion_tokens\":1,\"total_tokens\":2}}"
    }
}
