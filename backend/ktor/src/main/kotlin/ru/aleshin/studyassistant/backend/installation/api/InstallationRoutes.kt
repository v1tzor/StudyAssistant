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

package ru.aleshin.studyassistant.backend.installation.api

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.bodylimit.RequestBodyLimit
import io.ktor.server.plugins.origin
import io.ktor.server.request.contentLength
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ru.aleshin.studyassistant.backend.installation.api.mappers.InstallationRegistrationResponseMapper
import ru.aleshin.studyassistant.backend.installation.services.InstallationRegistrationService
import ru.aleshin.studyassistant.backend.common.api.PayloadTooLargeApiException

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
fun Route.installationRoutes(
    service: InstallationRegistrationService,
    responseMapper: InstallationRegistrationResponseMapper,
) {
    route("/api/v1/installations") {
        install(RequestBodyLimit) {
            bodyLimit { MAX_REGISTRATION_BODY_BYTES }
        }

        post("/register") {
            val contentLength = call.request.contentLength() ?: 0L
            if (contentLength > 0L ||
                call.request.headers[HttpHeaders.TransferEncoding] != null
            ) {
                throw PayloadTooLargeApiException()
            }
            val registration = service.register(
                remoteAddress = call.request.origin.remoteAddress,
            )

            call.respond(
                status = HttpStatusCode.Created,
                message = responseMapper.map(registration = registration),
            )
        }
    }
}

private const val MAX_REGISTRATION_BODY_BYTES = 1L
