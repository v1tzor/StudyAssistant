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

package ru.aleshin.studyassistant.backend.sharing.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.bodylimit.RequestBodyLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.ClaimScheduleShareRequest
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.ConfirmScheduleShareRequest
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.CreateScheduleShareRequest
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.ReleaseScheduleShareRequest
import ru.aleshin.studyassistant.backend.sharing.services.ScheduleSharingService
import ru.aleshin.studyassistant.backend.common.api.requireInstallationToken
import ru.aleshin.studyassistant.backend.security.InstallationCredentialService

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
fun Route.scheduleSharingRoutes(
    service: ScheduleSharingService,
    credentialService: InstallationCredentialService,
    maxRequestBodyBytes: Long,
) {
    route("/api/v1/shares/schedule") {

        install(RequestBodyLimit) {
            bodyLimit {
                maxRequestBodyBytes
            }
        }

        post("/create") {
            val installationToken = call.requireInstallationToken(
                credentialService = credentialService,
            )
            val request = call.receive<CreateScheduleShareRequest>()

            val response = service.create(
                installationToken = installationToken,
                request = request,
            )

            call.respond(
                status = HttpStatusCode.Created,
                message = response,
            )
        }

        post("/claim") {
            val installationToken = call.requireInstallationToken(
                credentialService = credentialService,
            )
            val request = call.receive<ClaimScheduleShareRequest>()

            val response = service.claim(
                installationToken = installationToken,
                request = request,
            )

            call.respond(
                status = HttpStatusCode.OK,
                message = response,
            )
        }

        post("/confirm") {
            val installationToken = call.requireInstallationToken(
                credentialService = credentialService,
            )
            val request = call.receive<ConfirmScheduleShareRequest>()

            call.respond(
                status = HttpStatusCode.OK,
                message = service.confirm(
                    installationToken = installationToken,
                    request = request,
                ),
            )
        }

        post("/release") {
            val request = call.receive<ReleaseScheduleShareRequest>()

            call.respond(
                status = HttpStatusCode.OK,
                message = service.release(
                    request = request,
                ),
            )
        }
    }
}
