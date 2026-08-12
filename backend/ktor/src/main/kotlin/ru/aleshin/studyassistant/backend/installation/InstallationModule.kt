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

package ru.aleshin.studyassistant.backend.installation

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing
import ru.aleshin.studyassistant.backend.database.DatabaseFactory
import ru.aleshin.studyassistant.backend.installation.api.installationRoutes
import ru.aleshin.studyassistant.backend.installation.api.mappers.InstallationRegistrationResponseMapper
import ru.aleshin.studyassistant.backend.installation.domain.repository.InstallationRegistrationRepository
import ru.aleshin.studyassistant.backend.installation.infrastructure.InstallationRegistrationRepositoryImpl
import ru.aleshin.studyassistant.backend.installation.services.InstallationRegistrationService
import ru.aleshin.studyassistant.backend.security.InstallationCredentialService
import ru.aleshin.studyassistant.backend.security.NetworkHasher
import java.time.Clock

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
fun Application.installationModule() {
    val config = InstallationConfig.from(applicationConfig = environment.config)

    dependencies {
        provide<InstallationConfig> { config }
        provide<InstallationRegistrationRepository> {
            InstallationRegistrationRepositoryImpl(
                database = resolve<DatabaseFactory>().database,
                config = config,
            )
        }
        provide<InstallationRegistrationService> {
            InstallationRegistrationService(
                repository = resolve<InstallationRegistrationRepository>(),
                credentialService = resolve<InstallationCredentialService>(),
                networkHasher = resolve<NetworkHasher>(),
                clock = Clock.systemUTC(),
            )
        }
        provide<InstallationRegistrationResponseMapper> {
            InstallationRegistrationResponseMapper()
        }
    }

    val service: InstallationRegistrationService by dependencies
    val responseMapper: InstallationRegistrationResponseMapper by dependencies

    routing {
        installationRoutes(
            service = service,
            responseMapper = responseMapper,
        )
    }
}
