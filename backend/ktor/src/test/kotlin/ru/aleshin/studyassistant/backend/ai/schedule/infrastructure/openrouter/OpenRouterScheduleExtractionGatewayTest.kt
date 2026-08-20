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

package ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.openrouter

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.toByteArray
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.ByteReadChannel
import kotlinx.coroutines.runBlocking
import org.slf4j.helpers.NOPLogger
import ru.aleshin.studyassistant.backend.ai.schedule.api.validation.ScheduleImageDecoder
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleExtractionRequest
import ru.aleshin.studyassistant.backend.ai.schedule.domain.result.ScheduleProviderResult
import ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.deepseek.mappers.DeepSeekScheduleExtractionMapper
import ru.aleshin.studyassistant.backend.ai.schedule.testJpegBytes
import ru.aleshin.studyassistant.backend.ai.testAiConfig
import ru.aleshin.studyassistant.backend.ai.testOpenRouterConfig
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
class OpenRouterScheduleExtractionGatewayTest {

    @Test
    fun rateLimitShouldExposeRetryAfterWithoutProviderBody() = runBlocking {
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
            val result = gateway(client = client).extract(request = request())

            assertIs<ScheduleProviderResult.RateLimited>(result)
            assertEquals(7, result.retryAfterSeconds)
        }
    }

    @Test
    fun arrayContentAndLengthFinishReasonShouldStillMap() = runBlocking {
        val client = client(
            engine = MockEngine {
                jsonResponse(status = HttpStatusCode.OK, content = ARRAY_CONTENT_RESPONSE)
            },
        )

        client.use {
            val result = gateway(client = client).extract(request = request())

            assertIs<ScheduleProviderResult.Success>(result)
            assertEquals("Semester", result.draft.title)
            assertEquals(1, result.draft.entries.size)
        }
    }

    @Test
    fun emptyDraftShouldBeUnavailable() = runBlocking {
        val client = client(
            engine = MockEngine {
                jsonResponse(status = HttpStatusCode.OK, content = EMPTY_RESPONSE)
            },
        )

        client.use {
            val result = gateway(client = client).extract(request = request())

            assertIs<ScheduleProviderResult.Unavailable>(result)
        }
    }

    @Test
    fun markdownJsonShouldStillMap() = runBlocking {
        val client = client(
            engine = MockEngine {
                jsonResponse(status = HttpStatusCode.OK, content = MARKDOWN_RESPONSE)
            },
        )

        client.use {
            val result = gateway(client = client).extract(request = request())

            assertIs<ScheduleProviderResult.Success>(result)
            assertEquals(1, result.draft.entries.size)
        }
    }

    private fun gateway(client: HttpClient): OpenRouterScheduleExtractionGateway {
        return OpenRouterScheduleExtractionGateway(
            httpClient = client,
            config = testOpenRouterConfig(),
            mapper = DeepSeekScheduleExtractionMapper(config = testAiConfig()),
            imageNormalizer = ScheduleImageNormalizer(),
            clock = Clock.fixed(Instant.parse("2026-08-16T10:00:00Z"), ZoneOffset.UTC),
            random = Random.Default,
            logger = NOPLogger.NOP_LOGGER,
        )
    }

    private fun client(engine: MockEngine): HttpClient {
        return HttpClient(engine) {
            install(ContentNegotiation) {
                json(OpenRouterJson)
            }
        }
    }

    private fun MockRequestHandleScope.jsonResponse(
        status: HttpStatusCode,
        content: String,
    ) = respond(
        content = ByteReadChannel(content),
        status = status,
        headers = headersOf(HttpHeaders.ContentType, "application/json"),
    )

    private fun request(): ScheduleExtractionRequest {
        return ScheduleExtractionRequest(
            imageBytes = testJpegBytes(),
            imageMimeType = ScheduleImageDecoder.IMAGE_JPEG,
            note = "9б",
            locale = "ru-RU",
            timeZone = "Europe/Moscow",
            numberOfWeeks = 1,
            todayDate = "2026-08-16",
        )
    }

    private companion object {

        const val SUCCESS_RESPONSE =
            """{"choices":[{"finish_reason":"stop","message":{"content":"{\"title\":null,\"entries\":[{\"repeatWeek\":1,\"dayOfWeek\":1,\"classNumber\":1,\"startTime\":\"09:00\",\"endTime\":\"10:30\",\"subject\":\"Mathematics\",\"eventType\":null,\"teacher\":null,\"office\":null,\"location\":null}]}"}}]}"""
        const val EMPTY_RESPONSE =
            """{"choices":[{"finish_reason":"stop","message":{"content":"{\"title\":null,\"entries\":[]}"}}]}"""
        const val ARRAY_CONTENT_RESPONSE =
            """{"choices":[{"finish_reason":"length","message":{"content":[{"type":"text","text":"{\"title\":\"Semester\",\"entries\":[{\"repeatWeek\":1,\"dayOfWeek\":1,\"classNumber\":1,\"startTime\":\"09:00\",\"endTime\":\"10:30\",\"subject\":\"Mathematics\",\"eventType\":null,\"teacher\":null,\"office\":null,\"location\":null}]}"}]}}]}"""
        const val MARKDOWN_RESPONSE =
            """{"choices":[{"finish_reason":"stop","message":{"content":"```json\n{\"title\":null,\"entries\":[{\"repeatWeek\":1,\"dayOfWeek\":1,\"classNumber\":1,\"startTime\":\"09:00\",\"endTime\":\"10:30\",\"subject\":\"Mathematics\",\"eventType\":null,\"teacher\":null,\"office\":null,\"location\":null}]}\n```"}}]}"""
    }
}
