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

package ru.aleshin.studyassistant.backend.sharing

import io.ktor.server.application.Application
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing
import ru.aleshin.studyassistant.backend.database.DatabaseFactory
import ru.aleshin.studyassistant.backend.security.ClaimTokenService
import ru.aleshin.studyassistant.backend.security.InstallationHasher
import ru.aleshin.studyassistant.backend.security.InstallationCredentialService
import ru.aleshin.studyassistant.backend.security.PayloadCipher
import ru.aleshin.studyassistant.backend.security.ShareCodeGenerator
import ru.aleshin.studyassistant.backend.security.ShareCodeHasher
import ru.aleshin.studyassistant.backend.sharing.api.homeworkSharingRoutes
import ru.aleshin.studyassistant.backend.sharing.api.scheduleSharingRoutes
import ru.aleshin.studyassistant.backend.sharing.api.validation.SharePayloadValidator
import ru.aleshin.studyassistant.backend.plugins.BackendJson
import ru.aleshin.studyassistant.backend.sharing.services.HomeworkSharingService
import ru.aleshin.studyassistant.backend.sharing.services.ScheduleSharingService
import ru.aleshin.studyassistant.backend.sharing.domain.repository.HomeworkSharingRepository
import ru.aleshin.studyassistant.backend.sharing.domain.repository.ScheduleSharingRepository
import ru.aleshin.studyassistant.backend.sharing.infrastructure.HomeworkSharingRepositoryImpl
import ru.aleshin.studyassistant.backend.sharing.infrastructure.ScheduleSharingRepositoryImpl
import java.time.Clock

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
fun Application.sharingModule() {
    val config = SharingConfig.from(
        applicationConfig = environment.config,
    )

    dependencies {

        provide<SharingConfig> { config }
        provide<SharePayloadValidator> {
            SharePayloadValidator(
                config = config,
                json = BackendJson,
            )
        }

        provide<HomeworkSharingRepository> {
            HomeworkSharingRepositoryImpl(
                database = resolve<DatabaseFactory>().database,
                config = config,
            )
        }

        provide<ScheduleSharingRepository> {
            val databaseFactory = resolve<DatabaseFactory>()

            ScheduleSharingRepositoryImpl(
                database = databaseFactory.database,
                config = config,
            )
        }

        provide<HomeworkSharingService> {
            HomeworkSharingService(
                repository = resolve<HomeworkSharingRepository>(),
                installationHasher = resolve<InstallationHasher>(),
                shareCodeGenerator = resolve<ShareCodeGenerator>(),
                shareCodeHasher = resolve<ShareCodeHasher>(),
                payloadCipher = resolve<PayloadCipher>(),
                payloadValidator = resolve<SharePayloadValidator>(),
                config = config,
                clock = Clock.systemUTC(),
            )
        }

        provide<ScheduleSharingService> {
            ScheduleSharingService(
                repository = resolve<ScheduleSharingRepository>(),
                installationHasher = resolve<InstallationHasher>(),
                shareCodeGenerator = resolve<ShareCodeGenerator>(),
                shareCodeHasher = resolve<ShareCodeHasher>(),
                claimTokenService = resolve<ClaimTokenService>(),
                payloadCipher = resolve<PayloadCipher>(),
                payloadValidator = resolve<SharePayloadValidator>(),
                config = config,
                clock = Clock.systemUTC(),
            )
        }
    }

    val homeworkService: HomeworkSharingService by dependencies

    val scheduleService: ScheduleSharingService by dependencies
    val credentialService: InstallationCredentialService by dependencies

    routing {
        homeworkSharingRoutes(
            service = homeworkService,
            credentialService = credentialService,
            maxRequestBodyBytes = config.maxRequestBodyBytes,
        )
        scheduleSharingRoutes(
            service = scheduleService,
            credentialService = credentialService,
            maxRequestBodyBytes = config.maxRequestBodyBytes,
        )
    }
}
