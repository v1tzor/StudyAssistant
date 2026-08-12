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

package ru.aleshin.studyassistant.core.remote.api.installation

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.exceptions.InternetConnectionException
import ru.aleshin.studyassistant.core.remote.ktor.NetworkConnectionChecker
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class InstallationRemoteApiTest {

    @Test
    fun registerShouldUseDedicatedEndpointAndParseCredential() = runTest {
        val engine = MockEngine { request ->
            assertEquals("/api/v1/installations/register", request.url.encodedPath)
            respond(
                content = """{"credential":"$CREDENTIAL"}""",
                status = HttpStatusCode.Created,
                headers = JSON_HEADERS,
            )
        }

        assertEquals(CREDENTIAL, createApi(engine).register().credential)
    }

    @Test
    fun registerShouldRejectNonSuccessResponse() = runTest {
        val engine = MockEngine {
            respond(
                content = """{"errorCode":"rate_limit"}""",
                status = HttpStatusCode.TooManyRequests,
                headers = JSON_HEADERS,
            )
        }

        assertFailsWith<InternetConnectionException> {
            createApi(engine).register()
        }
    }

    private fun createApi(engine: MockEngine): InstallationRemoteApi {
        return InstallationRemoteApi.Backend(
            httpClient = HttpClient(engine) {
                defaultRequest {
                    url("https://backend.studyassistant.example")
                }
            },
            connectionChecker = NetworkConnectionChecker { true },
            json = Json,
        )
    }

    private companion object {

        val CREDENTIAL = "v1.${"A".repeat(43)}.${"B".repeat(43)}"
        val JSON_HEADERS = headersOf(HttpHeaders.ContentType, "application/json")
    }
}
