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

package ru.aleshin.studyassistant.backend.ai.api

import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import ru.aleshin.studyassistant.backend.ai.api.dto.AiCompletionRequestDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiMessageDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiMessageRoleDto
import ru.aleshin.studyassistant.backend.ai.api.mappers.AiCompletionRequestMapper
import ru.aleshin.studyassistant.backend.ai.api.mappers.AiCompletionResponseMapper
import ru.aleshin.studyassistant.backend.ai.api.validation.AiCompletionRequestValidator
import ru.aleshin.studyassistant.backend.ai.domain.gateway.AiCompletionGateway
import ru.aleshin.studyassistant.backend.ai.domain.model.AiCompletion
import ru.aleshin.studyassistant.backend.ai.domain.model.AiFinishReason
import ru.aleshin.studyassistant.backend.ai.domain.model.AiQuota
import ru.aleshin.studyassistant.backend.ai.domain.repository.AiQuotaRepository
import ru.aleshin.studyassistant.backend.ai.domain.result.AiProviderResult
import ru.aleshin.studyassistant.backend.ai.domain.result.AiQuotaReservationResult
import ru.aleshin.studyassistant.backend.ai.domain.services.AiAssistantPromptFactory
import ru.aleshin.studyassistant.backend.ai.domain.services.AiCompletionRequestFactory
import ru.aleshin.studyassistant.backend.ai.domain.services.AiCompletionFingerprintFactory
import ru.aleshin.studyassistant.backend.ai.domain.tools.AiToolCatalog
import ru.aleshin.studyassistant.backend.ai.services.AiCompletionService
import ru.aleshin.studyassistant.backend.ai.services.AiQuotaService
import ru.aleshin.studyassistant.backend.ai.testAiConfig
import ru.aleshin.studyassistant.backend.plugins.BackendJson
import ru.aleshin.studyassistant.backend.plugins.configureSerialization
import ru.aleshin.studyassistant.backend.plugins.configureStatusPages
import ru.aleshin.studyassistant.backend.security.InstallationHasher
import ru.aleshin.studyassistant.backend.security.InstallationCredentialService
import ru.aleshin.studyassistant.backend.common.api.INSTALLATION_TOKEN_HEADER
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class AiCompletionRoutesTest {

    private val credentialService = InstallationCredentialService(
        secret = ByteArray(32) { 7 },
    )
    private val installationToken = credentialService.issue()

    @Test
    fun validRequestShouldReturnCompletionAndFinalizeQuota() = testApplication {
        val repository = FakeAiQuotaRepository()
        val service = service(repository = repository)

        application {
            configureSerialization()
            configureStatusPages()
            routing {
                aiCompletionRoutes(
                    service = service,
                    credentialService = credentialService,
                    maxRequestBodyBytes = 4_096,
                )
            }
        }

        val response = client.post("/api/v1/ai/completions") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(INSTALLATION_TOKEN_HEADER, installationToken)
            setBody(validRequestBody())
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"content\":\"Hello\""))
        assertEquals(listOf(true), repository.finalizedResults)
    }

    @Test
    fun missingInstallationHeaderShouldReturnJsonBadRequest() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                aiCompletionRoutes(
                    service = service(repository = FakeAiQuotaRepository()),
                    credentialService = credentialService,
                    maxRequestBodyBytes = 4_096,
                )
            }
        }

        val response = client.post("/api/v1/ai/completions") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            setBody(validRequestBody())
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("\"errorCode\":\"invalid\""))
    }

    @Test
    fun malformedInstallationHeaderShouldReturnJsonBadRequestWithoutQuotaCall() = testApplication {
        val repository = FakeAiQuotaRepository()

        application {
            configureSerialization()
            configureStatusPages()
            routing {
                aiCompletionRoutes(
                    service = service(repository = repository),
                    credentialService = credentialService,
                    maxRequestBodyBytes = 4_096,
                )
            }
        }

        val response = client.post("/api/v1/ai/completions") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(INSTALLATION_TOKEN_HEADER, "attacker-rotated-token")
            setBody(validRequestBody())
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("\"errorCode\":\"invalid\""))
        assertEquals(0, repository.reserveCalls)
    }

    @Test
    fun oversizedBodyShouldReturnJsonPayloadTooLarge() = testApplication {
        application {
            configureSerialization()
            configureStatusPages()
            routing {
                aiCompletionRoutes(
                    service = service(repository = FakeAiQuotaRepository()),
                    credentialService = credentialService,
                    maxRequestBodyBytes = 64,
                )
            }
        }

        val response = client.post("/api/v1/ai/completions") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(INSTALLATION_TOKEN_HEADER, installationToken)
            setBody("x".repeat(128))
        }

        assertEquals(HttpStatusCode.PayloadTooLarge, response.status)
        assertTrue(response.bodyAsText().contains("\"errorCode\":\"too_large\""))
    }

    private fun service(repository: AiQuotaRepository): AiCompletionService {
        val config = testAiConfig()
        val clock = Clock.fixed(
            Instant.parse("2026-08-12T10:00:00Z"),
            ZoneOffset.UTC,
        )
        val toolCatalog = AiToolCatalog()

        return AiCompletionService(
            validator = AiCompletionRequestValidator(
                config = config,
                json = BackendJson,
                toolCatalog = toolCatalog,
            ),
            requestMapper = AiCompletionRequestMapper(),
            responseMapper = AiCompletionResponseMapper(),
            requestFactory = AiCompletionRequestFactory(
                toolCatalog = toolCatalog,
                promptFactory = AiAssistantPromptFactory(),
            ),
            fingerprintFactory = AiCompletionFingerprintFactory(),
            quotaService = AiQuotaService(
                repository = repository,
                installationHasher = InstallationHasher(secret = ByteArray(32) { 1 }),
                clock = clock,
            ),
            completionGateway = object : AiCompletionGateway {
                override suspend fun complete(
                    request: ru.aleshin.studyassistant.backend.ai.domain.model.AiCompletionRequest,
                ): AiProviderResult {
                    return AiProviderResult.Success(
                        completion = AiCompletion(
                            content = "Hello",
                            toolCalls = emptyList(),
                            finishReason = AiFinishReason.STOP,
                            usage = null,
                        ),
                    )
                }
            },
            clock = clock,
        )
    }

    private fun validRequestBody(): String {
        return BackendJson.encodeToString(
            AiCompletionRequestDto(
                messageId = UUID.randomUUID().toString(),
                locale = "en-US",
                timeZone = "UTC",
                messages = listOf(
                    AiMessageDto(
                        role = AiMessageRoleDto.USER,
                        content = "Hello",
                    ),
                ),
            ),
        )
    }

    private class FakeAiQuotaRepository : AiQuotaRepository {

        var reserveCalls = 0
        val finalizedResults = mutableListOf<Boolean>()

        override suspend fun reserve(
            installationHash: ByteArray,
            messageId: UUID,
            requestHash: ByteArray,
            executionHash: ByteArray,
            now: Instant,
        ): AiQuotaReservationResult {
            reserveCalls++
            return AiQuotaReservationResult.Reserved(
                quota = AiQuota(used = 1, limit = 12, rewardedResetsRemaining = 3),
                resetAt = Instant.parse("2026-08-13T00:00:00Z"),
                isNewMessage = true,
            )
        }

        override suspend fun finalize(
            installationHash: ByteArray,
            messageId: UUID,
            succeeded: Boolean,
            now: Instant,
        ) {
            finalizedResults += succeeded
        }
    }

}
