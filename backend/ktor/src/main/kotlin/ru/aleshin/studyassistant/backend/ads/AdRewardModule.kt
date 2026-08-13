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

package ru.aleshin.studyassistant.backend.ads

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing
import ru.aleshin.studyassistant.backend.ads.api.adRewardRoutes
import ru.aleshin.studyassistant.backend.ads.domain.repository.AdRewardRepository
import ru.aleshin.studyassistant.backend.ads.infrastructure.AdRewardRepositoryImpl
import ru.aleshin.studyassistant.backend.ads.services.AdRewardService
import ru.aleshin.studyassistant.backend.ai.AiConfig
import ru.aleshin.studyassistant.backend.database.DatabaseFactory
import ru.aleshin.studyassistant.backend.security.InstallationCredentialService
import ru.aleshin.studyassistant.backend.security.InstallationHasher
import java.time.Clock

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
fun Application.adRewardModule() {
    dependencies {
        provide<AdRewardRepository> {
            AdRewardRepositoryImpl(
                database = resolve<DatabaseFactory>().database,
                config = resolve<AiConfig>(),
            )
        }
        provide<AdRewardService> {
            AdRewardService(
                repository = resolve<AdRewardRepository>(),
                installationHasher = resolve<InstallationHasher>(),
                clock = Clock.systemUTC(),
            )
        }
    }

    val service: AdRewardService by dependencies
    val credentialService: InstallationCredentialService by dependencies
    routing {
        adRewardRoutes(
            service = service,
            credentialService = credentialService,
        )
    }
}
