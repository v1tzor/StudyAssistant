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

package ru.aleshin.studyassistant.core.remote.api.ai

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.OutgoingContent
import io.ktor.http.contentType
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceException
import ru.aleshin.studyassistant.core.remote.ktor.NetworkConnectionChecker
import ru.aleshin.studyassistant.core.remote.models.ai.schedule.ScheduleExtractionRequestPojo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class ScheduleExtractionRemoteApiTest {

    @Test
    fun extractSendsTypedRequestAndMapsDraft() = runTest {
        var requestBody = ""
        val engine = MockEngine { request ->
            assertEquals("/api/v1/ai/schedule-extractions", request.url.encodedPath)
            assertEquals("installation-token", request.headers[INSTALLATION_HEADER])
            requestBody = (request.body as OutgoingContent.ByteArrayContent)
                .bytes()
                .decodeToString()
            respond(
                content = SUCCESS_RESPONSE,
                status = HttpStatusCode.OK,
                headers = JSON_HEADERS,
            )
        }
        val api = createApi(engine)

        val response = api.extract(REQUEST, "installation-token")

        val requestJson = JSON.parseToJsonElement(requestBody).jsonObject
        assertEquals("request-1", requestJson.getValue("requestId").jsonPrimitive.content)
        assertEquals(2, requestJson.getValue("numberOfWeeks").jsonPrimitive.content.toInt())
        assertEquals("Математика", response.draft.entries.single().subject)
        assertEquals(11, response.quotaRemaining)
        assertEquals(12, response.quotaLimit)
        assertEquals(3, response.rewardedResetsRemaining)
        assertEquals(1_786_550_400_000L, response.quotaResetAt)
    }

    @Test
    fun extractMapsStructuredRateLimitError() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"errorCode":"rate_limit","retryAt":1786547000000}""",
                status = HttpStatusCode.TooManyRequests,
                headers = JSON_HEADERS,
            )
        }
        val api = createApi(engine)

        assertFailsWith<AiServiceException.RateLimited> {
            api.extract(REQUEST, "installation-token")
        }
    }

    @Test
    fun extractRejectsMalformedSuccessPayload() = runTest {
        val engine = MockEngine {
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = JSON_HEADERS,
            )
        }
        val api = createApi(engine)

        assertFailsWith<AiServiceException.ServerUnavailable> {
            api.extract(REQUEST, "installation-token")
        }
    }

    private fun createApi(engine: MockEngine): ScheduleExtractionRemoteApi {
        val client = HttpClient(engine) {
            defaultRequest {
                url("https://backend.studyassistant.example")
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) { json(JSON) }
        }
        return ScheduleExtractionRemoteApi.Backend(
            httpClient = client,
            connectionChecker = NetworkConnectionChecker { true },
            json = JSON,
        )
    }

    private companion object {
        const val INSTALLATION_HEADER = "X-Installation-Token"

        val JSON = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        val JSON_HEADERS = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
        val REQUEST = ScheduleExtractionRequestPojo(
            requestId = "request-1",
            imageBase64 = "AAA=",
            imageMimeType = "image/jpeg",
            note = "9б",
            locale = "ru",
            timeZone = "Europe/Moscow",
            numberOfWeeks = 2,
            todayDate = "2026-08-16",
        )
        const val SUCCESS_RESPONSE = """
            {
              "draft": {
                "title": "Расписание",
                "entries": [
                  {
                    "repeatWeek": 1,
                    "dayOfWeek": 1,
                    "classNumber": 1,
                    "startTime": "09:00",
                    "endTime": "10:30",
                    "subject": "Математика",
                    "eventType": "LECTURE",
                    "teacher": null,
                    "office": "101",
                    "location": null,
                    "organization": null,
                    "notes": null
                  }
                ],
                "unparsedLines": []
              },
              "quotaRemaining": 11,
              "quotaLimit": 12,
              "rewardedResetsRemaining": 3,
              "quotaResetAt": 1786550400000
            }
        """
    }
}
