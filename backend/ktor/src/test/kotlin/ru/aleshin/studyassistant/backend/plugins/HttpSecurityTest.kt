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

package ru.aleshin.studyassistant.backend.plugins

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

class HttpSecurityTest {

    @Test
    fun validProxyRequestReceivesSecurityHeaders() = testApplication {
        environment {
            config = securityConfig()
        }
        application {
            configureSerialization()
            configureHttpSecurity()
            routing {
                get("/") { call.respondText("ok") }
            }
        }

        val response = client.get("/") {
            productionHeaders()
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("nosniff", response.headers["X-Content-Type-Options"])
        assertEquals("no-store", response.headers[HttpHeaders.CacheControl])
        assertEquals(
            "max-age=31536000; includeSubDomains",
            response.headers[HttpHeaders.StrictTransportSecurity],
        )
    }

    @Test
    fun unexpectedHostIsRejected() = testApplication {
        environment {
            config = securityConfig()
        }
        application {
            configureSerialization()
            configureHttpSecurity()
            routing {
                get("/") { call.respondText("ok") }
            }
        }

        val response = client.get("/") {
            header(HttpHeaders.Host, "attacker.example")
            header("X-Forwarded-Host", "attacker.example")
            header("X-Forwarded-Proto", "https")
        }

        assertEquals(HttpStatusCode(421, "Misdirected Request"), response.status)
    }

    private fun securityConfig(): MapApplicationConfig {
        return MapApplicationConfig(
            "httpSecurity.expectedHost" to "api.studyassistant-app.ru",
            "httpSecurity.trustProxyHeaders" to "true",
            "httpSecurity.requireHttps" to "true",
        )
    }

    private fun io.ktor.client.request.HttpRequestBuilder.productionHeaders() {
        header(HttpHeaders.Host, "api.studyassistant-app.ru")
        header("X-Forwarded-Host", "api.studyassistant-app.ru")
        header("X-Forwarded-Proto", "https")
    }
}
