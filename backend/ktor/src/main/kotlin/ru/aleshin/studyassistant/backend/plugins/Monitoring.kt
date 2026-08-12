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

import io.ktor.http.HttpHeaders
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.http.HttpRequestLifecycle
import io.ktor.server.plugins.callid.CallId
import io.ktor.server.plugins.callid.callIdMdc
import io.ktor.server.plugins.callid.generate
import io.ktor.server.plugins.calllogging.CallLogging
import io.ktor.server.request.httpMethod
import io.ktor.server.request.path
import org.slf4j.event.Level
import java.util.UUID

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
fun Application.configureMonitoring() {
    install(CallId) {
        header(HttpHeaders.XRequestId)
        replyToHeader(HttpHeaders.XRequestId)
        generate {
            UUID.randomUUID().toString()
        }
        verify { callId ->
            REQUEST_ID_PATTERN.matches(callId)
        }
    }

    install(CallLogging) {
        level = Level.INFO
        callIdMdc(MDC_REQUEST_ID)
        format { call ->
            "${call.request.httpMethod.value} ${call.request.path()} ${call.response.status()?.value ?: 0}"
        }
    }

    install(HttpRequestLifecycle) {
        cancelCallOnClose = true
    }
}

private const val MDC_REQUEST_ID = "requestId"

private val REQUEST_ID_PATTERN = Regex("^[a-z0-9-]{1,64}$")
