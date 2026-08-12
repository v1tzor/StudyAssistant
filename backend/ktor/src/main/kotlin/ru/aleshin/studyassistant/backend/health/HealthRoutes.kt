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

package ru.aleshin.studyassistant.backend.health

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.route

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
fun Route.healthRoutes(
    service: HealthService,
) {
    route("/health") {

        get("/live") {
            call.respond(
                status = HttpStatusCode.OK,
                message = service.live(),
            )
        }

        get("/ready") {
            if (service.isReady()) {
                call.respond(
                    status = HttpStatusCode.OK,
                    message = HealthResponse(status = "ready"),
                )
            } else {
                call.respond(
                    status = HttpStatusCode.ServiceUnavailable,
                    message = HealthResponse(status = "not_ready"),
                )
            }
        }
    }
}