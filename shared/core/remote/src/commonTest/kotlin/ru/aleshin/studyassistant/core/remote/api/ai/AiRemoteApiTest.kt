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
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceException
import ru.aleshin.studyassistant.core.remote.ktor.NetworkConnectionChecker
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionMessagePojo
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionMessageRolePojo
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionRequestPojo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class AiRemoteApiTest {

    @Test
    fun completeSendsTypedRequestAndMapsQuotaResponse() = runTest {
        var requestBody = ""
        val engine = MockEngine { request ->
            assertEquals("/api/v1/ai/completions", request.url.encodedPath)
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

        val response = api.complete(
            request = AiCompletionRequestPojo(
                messageId = "message-1",
                locale = "ru",
                timeZone = "Europe/Moscow",
                toolProtocolVersion = 1,
                messages = listOf(
                    AiCompletionMessagePojo(
                        role = AiCompletionMessageRolePojo.USER,
                        content = "Добавь задачу",
                    ),
                ),
                toolNames = listOf("create_todo"),
            ),
            installationToken = "installation-token",
        )

        val requestJson = JSON.parseToJsonElement(requestBody).jsonObject
        assertEquals("message-1", requestJson.getValue("messageId").jsonPrimitive.content)
        assertEquals(
            "create_todo",
            requestJson.getValue("toolNames").jsonArray.single().jsonPrimitive.content,
        )
        assertEquals("TOOL_CALLS", response.finishReason.name)
        assertEquals("create_todo", response.message.toolCalls.single().name)
        assertEquals(11, response.quotaRemaining)
        assertEquals(12, response.quotaLimit)
        assertEquals(3, response.rewardedResetsRemaining)
        assertEquals(1_786_550_400_000L, response.quotaResetAt)
    }

    @Test
    fun completeMapsStructuredQuotaError() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"errorCode":"quota","quotaResetAt":1786550400000}""",
                status = HttpStatusCode.TooManyRequests,
                headers = JSON_HEADERS,
            )
        }
        val api = createApi(engine)

        val error = assertFailsWith<AiServiceException.QuotaExceeded> {
            api.complete(REQUEST, "installation-token")
        }

        assertEquals(1_786_550_400_000L, error.resetAtEpochMillis)
    }

    @Test
    fun completeRejectsMalformedSuccessPayload() = runTest {
        val engine = MockEngine {
            respond(
                content = "{}",
                status = HttpStatusCode.OK,
                headers = JSON_HEADERS,
            )
        }
        val api = createApi(engine)

        assertFailsWith<AiServiceException.ServerUnavailable> {
            api.complete(REQUEST, "installation-token")
        }
    }

    private fun createApi(engine: MockEngine): AiRemoteApi {
        val client = HttpClient(engine) {
            defaultRequest {
                url("https://backend.studyassistant.example")
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) { json(JSON) }
        }
        return AiRemoteApi.Backend(
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
        val REQUEST = AiCompletionRequestPojo(
            messageId = "message-1",
            locale = "en",
            timeZone = "UTC",
            toolProtocolVersion = 1,
            messages = listOf(
                AiCompletionMessagePojo(
                    role = AiCompletionMessageRolePojo.USER,
                    content = "Hello",
                ),
            ),
            toolNames = emptyList(),
        )
        const val SUCCESS_RESPONSE = """
            {
              "message": {
                "content": null,
                "toolCalls": [
                  {
                    "id": "call-1",
                    "name": "create_todo",
                    "arguments": "{\"name\":\"Read\"}"
                  }
                ]
              },
              "finishReason": "tool_calls",
              "usage": {
                "promptTokens": 100,
                "completionTokens": 20,
                "totalTokens": 120
              },
              "quotaRemaining": 11,
              "quotaLimit": 12,
              "rewardedResetsRemaining": 3,
              "quotaResetAt": 1786550400000
            }
        """
    }
}
