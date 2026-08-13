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

package ru.aleshin.studyassistant.backend.ads.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import ru.aleshin.studyassistant.backend.ads.api.dto.AdRewardChallengeRequestDto
import ru.aleshin.studyassistant.backend.ads.services.AdRewardService
import ru.aleshin.studyassistant.backend.common.api.requireInstallationToken
import ru.aleshin.studyassistant.backend.security.InstallationCredentialService

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
fun Route.adRewardRoutes(
    service: AdRewardService,
    credentialService: InstallationCredentialService,
) {
    route("/api/v1/ad-rewards") {
        post("/challenges") {
            val installationToken = call.requireInstallationToken(credentialService)
            val request = call.receive<AdRewardChallengeRequestDto>()
            call.respond(
                status = HttpStatusCode.Created,
                message = service.createChallenge(installationToken, request),
            )
        }
        post("/challenges/{challengeId}/complete") {
            val installationToken = call.requireInstallationToken(credentialService)
            val challengeId = call.parameters["challengeId"].orEmpty()
            call.respond(
                status = HttpStatusCode.OK,
                message = service.completeChallenge(installationToken, challengeId),
            )
        }
    }
}
