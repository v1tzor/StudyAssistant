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

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing
import ru.aleshin.studyassistant.backend.database.DatabaseProbe

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
fun Application.healthModule() {
    dependencies {
        provide<HealthService> { HealthService(databaseProbe = resolve<DatabaseProbe>()) }
    }

    val service: HealthService by dependencies

    routing {
        healthRoutes(
            service = service,
        )
    }
}