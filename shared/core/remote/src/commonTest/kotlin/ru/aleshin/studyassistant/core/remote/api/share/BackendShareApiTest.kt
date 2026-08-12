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

package ru.aleshin.studyassistant.core.remote.api.share

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
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import ru.aleshin.studyassistant.core.domain.entities.share.ShareException
import ru.aleshin.studyassistant.core.remote.ktor.NetworkConnectionChecker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class BackendShareApiTest {

    @Test
    fun createScheduleSendsInstallationHeaderAndWrappedPayload() = runTest {
        var requestBody = ""
        val engine = MockEngine { request ->
            assertEquals("/api/v1/shares/schedule/create", request.url.encodedPath)
            assertEquals("installation-token", request.headers[INSTALLATION_HEADER])
            requestBody = (request.body as OutgoingContent.ByteArrayContent)
                .bytes()
                .decodeToString()
            respond(
                content = CREATE_RESPONSE,
                status = HttpStatusCode.Created,
                headers = JSON_HEADERS,
            )
        }
        val api = createApi(engine)

        val link = api.createSchedule(
            share = buildJsonObject { put("name", "Schedule") },
            installationToken = "installation-token",
        )

        val share = JSON.parseToJsonElement(requestBody)
            .jsonObject
            .getValue("share")
            .jsonObject
        assertEquals("\"Schedule\"", share.getValue("name").toString())
        assertEquals("ABC123", link.code)
        assertEquals(1_786_550_400_000L, link.expiresAt)
    }

    @Test
    fun claimScheduleMapsConsumedError() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"errorCode":"consumed"}""",
                status = HttpStatusCode.Gone,
                headers = JSON_HEADERS,
            )
        }
        val api = createApi(engine)

        assertFailsWith<ShareException.Consumed> {
            api.claimSchedule("ABC123", "installation-token")
        }
    }

    private fun createApi(engine: MockEngine): BackendShareApi {
        val client = HttpClient(engine) {
            defaultRequest {
                url("https://backend.studyassistant.example")
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) { json(JSON) }
        }
        return BackendShareApi(
            httpClient = client,
            connectionChecker = NetworkConnectionChecker { true },
            json = JSON,
        )
    }

    private companion object {
        const val INSTALLATION_HEADER = "X-Installation-Token"
        const val CREATE_RESPONSE = """
            {
              "link": {
                "code": "ABC123",
                "createdAt": 1786464000000,
                "expiresAt": 1786550400000
              }
            }
        """

        val JSON = Json {
            ignoreUnknownKeys = true
            explicitNulls = false
        }
        val JSON_HEADERS = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
    }
}
