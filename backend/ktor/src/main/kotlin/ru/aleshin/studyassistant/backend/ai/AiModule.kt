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

package ru.aleshin.studyassistant.backend.ai

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStopped
import io.ktor.server.plugins.di.dependencies
import io.ktor.server.routing.routing
import org.slf4j.LoggerFactory
import ru.aleshin.studyassistant.backend.ai.api.aiCompletionRoutes
import ru.aleshin.studyassistant.backend.ai.api.mappers.AiCompletionRequestMapper
import ru.aleshin.studyassistant.backend.ai.api.mappers.AiCompletionResponseMapper
import ru.aleshin.studyassistant.backend.ai.api.validation.AiCompletionRequestValidator
import ru.aleshin.studyassistant.backend.ai.domain.gateway.AiCompletionGateway
import ru.aleshin.studyassistant.backend.ai.domain.repository.AiQuotaRepository
import ru.aleshin.studyassistant.backend.ai.domain.services.AiAssistantPromptFactory
import ru.aleshin.studyassistant.backend.ai.domain.services.AiCompletionRequestFactory
import ru.aleshin.studyassistant.backend.ai.domain.services.AiCompletionFingerprintFactory
import ru.aleshin.studyassistant.backend.ai.domain.tools.AiToolCatalog
import ru.aleshin.studyassistant.backend.ai.infrastructure.AiQuotaRepositoryImpl
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.DeepSeekAiCompletionGateway
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.DeepSeekConfig
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.createDeepSeekHttpClient
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.mappers.DeepSeekMapper
import ru.aleshin.studyassistant.backend.ai.schedule.api.mappers.ScheduleExtractionRequestMapper
import ru.aleshin.studyassistant.backend.ai.schedule.api.mappers.ScheduleExtractionResponseMapper
import ru.aleshin.studyassistant.backend.ai.schedule.api.scheduleExtractionRoutes
import ru.aleshin.studyassistant.backend.ai.schedule.api.validation.ScheduleExtractionRequestValidator
import ru.aleshin.studyassistant.backend.ai.schedule.domain.gateway.ScheduleExtractionGateway
import ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.deepseek.DeepSeekScheduleExtractionGateway
import ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.deepseek.mappers.DeepSeekScheduleExtractionMapper
import ru.aleshin.studyassistant.backend.ai.schedule.services.ScheduleExtractionService
import ru.aleshin.studyassistant.backend.ai.services.AiCompletionService
import ru.aleshin.studyassistant.backend.ai.services.AiQuotaService
import ru.aleshin.studyassistant.backend.database.DatabaseFactory
import ru.aleshin.studyassistant.backend.plugins.BackendJson
import ru.aleshin.studyassistant.backend.security.InstallationHasher
import ru.aleshin.studyassistant.backend.security.InstallationCredentialService
import java.time.Clock
import kotlin.random.Random

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
fun Application.aiModule() {
    val config = AiConfig.from(applicationConfig = environment.config)
    val deepSeekConfig = DeepSeekConfig.from(applicationConfig = environment.config)
    val clock = Clock.systemUTC()
    val httpClient = createDeepSeekHttpClient(config = deepSeekConfig)

    monitor.subscribe(ApplicationStopped) {
        httpClient.close()
    }

    dependencies {
        provide<AiConfig> { config }
        provide<DeepSeekConfig> { deepSeekConfig }

        provide<AiQuotaRepository> {
            AiQuotaRepositoryImpl(
                database = resolve<DatabaseFactory>().database,
                config = config,
            )
        }

        provide<AiQuotaService> {
            AiQuotaService(
                repository = resolve<AiQuotaRepository>(),
                installationHasher = resolve<InstallationHasher>(),
                clock = clock,
            )
        }

        provide<AiToolCatalog> { AiToolCatalog() }
        provide<AiAssistantPromptFactory> { AiAssistantPromptFactory() }
        provide<AiCompletionFingerprintFactory> { AiCompletionFingerprintFactory() }
        provide<AiCompletionRequestFactory> {
            AiCompletionRequestFactory(
                toolCatalog = resolve<AiToolCatalog>(),
                promptFactory = resolve<AiAssistantPromptFactory>(),
            )
        }

        provide<AiCompletionRequestValidator> {
            AiCompletionRequestValidator(
                config = config,
                json = BackendJson,
                toolCatalog = resolve<AiToolCatalog>(),
            )
        }
        provide<AiCompletionRequestMapper> { AiCompletionRequestMapper() }
        provide<AiCompletionResponseMapper> { AiCompletionResponseMapper() }
        provide<DeepSeekMapper> { DeepSeekMapper(config = deepSeekConfig) }
        provide<ScheduleExtractionRequestValidator> {
            ScheduleExtractionRequestValidator(config = config)
        }
        provide<ScheduleExtractionRequestMapper> { ScheduleExtractionRequestMapper() }
        provide<ScheduleExtractionResponseMapper> { ScheduleExtractionResponseMapper() }
        provide<DeepSeekScheduleExtractionMapper> {
            DeepSeekScheduleExtractionMapper(config = config)
        }

        provide<AiCompletionGateway> {
            DeepSeekAiCompletionGateway(
                httpClient = httpClient,
                config = deepSeekConfig,
                mapper = resolve<DeepSeekMapper>(),
                clock = clock,
                random = Random.Default,
                logger = LoggerFactory.getLogger(DeepSeekAiCompletionGateway::class.java),
            )
        }

        provide<AiCompletionService> {
            AiCompletionService(
                validator = resolve<AiCompletionRequestValidator>(),
                requestMapper = resolve<AiCompletionRequestMapper>(),
                responseMapper = resolve<AiCompletionResponseMapper>(),
                requestFactory = resolve<AiCompletionRequestFactory>(),
                fingerprintFactory = resolve<AiCompletionFingerprintFactory>(),
                quotaService = resolve<AiQuotaService>(),
                completionGateway = resolve<AiCompletionGateway>(),
                clock = clock,
            )
        }

        provide<ScheduleExtractionGateway> {
            DeepSeekScheduleExtractionGateway(
                completionGateway = resolve<AiCompletionGateway>(),
                mapper = resolve<DeepSeekScheduleExtractionMapper>(),
            )
        }
        provide<ScheduleExtractionService> {
            ScheduleExtractionService(
                validator = resolve<ScheduleExtractionRequestValidator>(),
                requestMapper = resolve<ScheduleExtractionRequestMapper>(),
                responseMapper = resolve<ScheduleExtractionResponseMapper>(),
                quotaService = resolve<AiQuotaService>(),
                extractionGateway = resolve<ScheduleExtractionGateway>(),
                clock = clock,
            )
        }
    }

    val completionService: AiCompletionService by dependencies
    val scheduleExtractionService: ScheduleExtractionService by dependencies
    val credentialService: InstallationCredentialService by dependencies

    routing {
        aiCompletionRoutes(
            service = completionService,
            credentialService = credentialService,
            maxRequestBodyBytes = config.maxRequestBodyBytes,
        )
        scheduleExtractionRoutes(
            service = scheduleExtractionService,
            credentialService = credentialService,
            maxRequestBodyBytes = config.maxRequestBodyBytes,
        )
    }
}
