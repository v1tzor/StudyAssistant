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
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.plugins.forwardedheaders.XForwardedHeaders
import io.ktor.server.plugins.origin
import io.ktor.server.response.header
import io.ktor.server.response.respond
import ru.aleshin.studyassistant.backend.common.api.ApiErrorResponse

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
fun Application.configureHttpSecurity() {
    val config = HttpSecurityConfig.from(environment.config)

    if (config.trustProxyHeaders) {
        install(XForwardedHeaders) {
            useLastProxy()
        }
    }

    intercept(ApplicationCallPipeline.Setup) {
        call.response.header("X-Content-Type-Options", "nosniff")
        call.response.header("X-Frame-Options", "DENY")
        call.response.header("Referrer-Policy", "no-referrer")
        call.response.header(HttpHeaders.CacheControl, "no-store")
        call.response.header(HttpHeaders.Pragma, "no-cache")
        call.response.header("Content-Security-Policy", CONTENT_SECURITY_POLICY)
        call.response.header("Permissions-Policy", PERMISSIONS_POLICY)
        call.response.header("Cross-Origin-Resource-Policy", "same-site")
        if (config.requireHttps) {
            call.response.header(
                HttpHeaders.StrictTransportSecurity,
                "max-age=31536000; includeSubDomains",
            )
        }
    }

    intercept(ApplicationCallPipeline.Plugins) {
        val expectedHost = config.expectedHost
        if (expectedHost != null && call.request.origin.serverHost.lowercase() != expectedHost) {
            call.respond(
                status = MISDIRECTED_REQUEST_STATUS,
                message = ApiErrorResponse(errorCode = "misdirected_request"),
            )
            finish()
            return@intercept
        }
        if (config.requireHttps && call.request.origin.scheme != HTTPS_SCHEME) {
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ApiErrorResponse(errorCode = "https_required"),
            )
            finish()
        }
    }
}

private const val HTTPS_SCHEME = "https"

private val MISDIRECTED_REQUEST_STATUS = HttpStatusCode(421, "Misdirected Request")

private const val CONTENT_SECURITY_POLICY =
    "default-src 'none'; frame-ancestors 'none'; base-uri 'none'; form-action 'none'"

private const val PERMISSIONS_POLICY =
    "accelerometer=(), camera=(), geolocation=(), gyroscope=(), microphone=(), payment=(), usb=()"
