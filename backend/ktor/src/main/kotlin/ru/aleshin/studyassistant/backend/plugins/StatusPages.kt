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
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.PayloadTooLargeException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.plugins.statuspages.exception
import io.ktor.server.response.header
import io.ktor.server.response.respond
import kotlinx.coroutines.CancellationException
import ru.aleshin.studyassistant.backend.common.api.ApiErrorResponse
import ru.aleshin.studyassistant.backend.common.api.ApiException

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
fun Application.configureStatusPages() {
    install(StatusPages) {

        exception<ApiException> { call, cause ->
            val retryAt = cause.retryAt ?: cause.quotaResetAt
            if (retryAt != null) {
                val retryAfterSeconds = ((retryAt - System.currentTimeMillis()).coerceAtLeast(0L) + 999L) /
                    MILLIS_PER_SECOND
                call.response.header(HttpHeaders.RetryAfter, retryAfterSeconds.toString())
            }
            call.respond(
                status = cause.status,
                message = ApiErrorResponse(
                    errorCode = cause.errorCode,
                    retryAt = cause.retryAt,
                    quotaResetAt = cause.quotaResetAt,
                )
            )
        }

        exception<PayloadTooLargeException> { call, _ ->
            call.respond(
                status = HttpStatusCode.PayloadTooLarge,
                message = ApiErrorResponse(
                    errorCode = "too_large",
                ),
            )
        }

        exception<ContentTransformationException> { call, _ ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ApiErrorResponse(
                    errorCode = "invalid"
                ),
            )
        }

        exception<BadRequestException> { call, _ ->
            call.respond(
                status = HttpStatusCode.BadRequest,
                message = ApiErrorResponse(
                    errorCode = "invalid"
                ),
            )
        }

        exception<CancellationException> { _, cause ->
            throw cause
        }

        exception<Throwable> { call, cause ->
            call.application.log.error("Unhandled server error", cause)

            call.respond(
                status = HttpStatusCode.InternalServerError,
                message = ApiErrorResponse(
                    errorCode = "server_error",
                ),
            )
        }

        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(
                status = status,
                message = ApiErrorResponse(errorCode = "not_found"),
            )
        }

        status(HttpStatusCode.MethodNotAllowed) { call, status ->
            call.respond(
                status = status,
                message = ApiErrorResponse(errorCode = "method_not_allowed"),
            )
        }
    }
}

private const val MILLIS_PER_SECOND = 1_000L
