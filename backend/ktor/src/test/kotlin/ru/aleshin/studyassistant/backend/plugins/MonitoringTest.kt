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
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
class MonitoringTest {

    @Test
    fun requestIdShouldBeReturnedExactlyOnce() = testApplication {
        application {
            configureMonitoring()
            routing {
                get("/") { call.respondText("ok") }
            }
        }

        val response = client.get("/") {
            header(HttpHeaders.XRequestId, REQUEST_ID)
        }

        assertEquals(
            expected = listOf(REQUEST_ID),
            actual = response.headers.getAll(HttpHeaders.XRequestId),
        )
    }

    private companion object {

        const val REQUEST_ID = "monitoring-test-request-id"
    }
}
