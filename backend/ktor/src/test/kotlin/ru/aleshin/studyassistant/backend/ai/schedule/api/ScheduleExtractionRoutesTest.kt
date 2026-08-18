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

package ru.aleshin.studyassistant.backend.ai.schedule.api

import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import ru.aleshin.studyassistant.backend.ai.domain.model.AiQuota
import ru.aleshin.studyassistant.backend.ai.domain.repository.AiQuotaRepository
import ru.aleshin.studyassistant.backend.ai.domain.result.AiQuotaReservationResult
import ru.aleshin.studyassistant.backend.ai.schedule.api.mappers.ScheduleExtractionRequestMapper
import ru.aleshin.studyassistant.backend.ai.schedule.api.mappers.ScheduleExtractionResponseMapper
import ru.aleshin.studyassistant.backend.ai.schedule.api.validation.ScheduleExtractionRequestValidator
import ru.aleshin.studyassistant.backend.ai.schedule.domain.gateway.ScheduleExtractionGateway
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleDraft
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleDraftEntry
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleEventType
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleExtractionRequest
import ru.aleshin.studyassistant.backend.ai.schedule.domain.result.ScheduleProviderResult
import ru.aleshin.studyassistant.backend.ai.schedule.services.ScheduleExtractionService
import ru.aleshin.studyassistant.backend.ai.schedule.testScheduleExtractionRequestDto
import ru.aleshin.studyassistant.backend.ai.services.AiQuotaService
import ru.aleshin.studyassistant.backend.ai.testAiConfig
import ru.aleshin.studyassistant.backend.common.api.INSTALLATION_TOKEN_HEADER
import ru.aleshin.studyassistant.backend.plugins.BackendJson
import ru.aleshin.studyassistant.backend.plugins.configureSerialization
import ru.aleshin.studyassistant.backend.plugins.configureStatusPages
import ru.aleshin.studyassistant.backend.security.InstallationHasher
import ru.aleshin.studyassistant.backend.security.InstallationCredentialService
import ru.aleshin.studyassistant.backend.security.PayloadCipher

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class ScheduleExtractionRoutesTest {

    private val credentialService = InstallationCredentialService(
        secret = ByteArray(32) { 7 },
    )
    private val installationToken = credentialService.issue()

    @Test
    fun validRequestShouldReturnDraftAndFinalizeQuota() = testApplication {
        val repository = FakeAiQuotaRepository()

        application {
            configureSerialization()
            configureStatusPages()
            routing {
                scheduleExtractionRoutes(
                    service = service(repository),
                    credentialService = credentialService,
                    maxRequestBodyBytes = 1_048_576,
                )
            }
        }

        val response = client.post("/api/v1/ai/schedule-extractions") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(INSTALLATION_TOKEN_HEADER, installationToken)
            setBody(validRequestBody())
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains("\"subject\":\"Math\""))
        assertEquals(listOf(true), repository.finalizedResults)
    }

    @Test
    fun invalidRequestShouldReturnJsonBadRequestWithoutReservingQuota() = testApplication {
        val repository = FakeAiQuotaRepository()

        application {
            configureSerialization()
            configureStatusPages()
            routing {
                scheduleExtractionRoutes(
                    service = service(repository),
                    credentialService = credentialService,
                    maxRequestBodyBytes = 1_048_576,
                )
            }
        }

        val response = client.post("/api/v1/ai/schedule-extractions") {
            header(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            header(INSTALLATION_TOKEN_HEADER, installationToken)
            setBody(validRequestBody().replace("\"numberOfWeeks\":1", "\"numberOfWeeks\":4"))
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains("\"errorCode\":\"invalid\""))
        assertEquals(0, repository.reserveCalls)
    }

    private fun service(repository: AiQuotaRepository): ScheduleExtractionService {
        val config = testAiConfig()
        val clock = Clock.fixed(
            Instant.parse("2026-08-12T10:00:00Z"),
            ZoneOffset.UTC,
        )
        return ScheduleExtractionService(
            validator = ScheduleExtractionRequestValidator(config),
            requestMapper = ScheduleExtractionRequestMapper(),
            responseMapper = ScheduleExtractionResponseMapper(),
            quotaService = AiQuotaService(
                repository = repository,
                installationHasher = InstallationHasher(secret = ByteArray(32) { 1 }),
                clock = clock,
            ),
            extractionGateway = object : ScheduleExtractionGateway {
                override suspend fun extract(
                    request: ScheduleExtractionRequest,
                ): ScheduleProviderResult {
                    return ScheduleProviderResult.Success(
                        ScheduleDraft(
                            title = "Schedule",
                            entries = listOf(
                                ScheduleDraftEntry(
                                    repeatWeek = 1,
                                    dayOfWeek = 1,
                                    classNumber = 1,
                                    startTime = "09:00",
                                    endTime = "10:30",
                                    subject = "Math",
                                    eventType = ScheduleEventType.LECTURE,
                                    teacher = null,
                                    office = "101",
                                ),
                            ),
                        ),
                    )
                }
            },
            clock = clock,
            payloadCipher = PayloadCipher(ByteArray(32) { 2 }),
        )
    }

    private fun validRequestBody(): String {
        return BackendJson.encodeToString(testScheduleExtractionRequestDto())
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
                reservationGeneration = 1,
            )
        }

        override suspend fun finalize(
            installationHash: ByteArray,
            messageId: UUID,
            succeeded: Boolean,
            reservationGeneration: Int,
            now: Instant,
        ) {
            finalizedResults += succeeded
        }

        override suspend fun saveResponse(
            installationHash: ByteArray,
            messageId: UUID,
            executionHash: ByteArray,
            responsePayload: ByteArray,
            responseNonce: ByteArray,
        ) = Unit
    }

}
