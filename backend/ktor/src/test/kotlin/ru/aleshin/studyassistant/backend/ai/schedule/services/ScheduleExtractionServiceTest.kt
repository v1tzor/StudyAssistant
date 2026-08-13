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

package ru.aleshin.studyassistant.backend.ai.schedule.services

import kotlinx.coroutines.runBlocking
import ru.aleshin.studyassistant.backend.ai.domain.model.AiQuota
import ru.aleshin.studyassistant.backend.ai.domain.repository.AiQuotaRepository
import ru.aleshin.studyassistant.backend.ai.domain.result.AiQuotaReservationResult
import ru.aleshin.studyassistant.backend.ai.schedule.api.dto.ScheduleExtractionRequestDto
import ru.aleshin.studyassistant.backend.ai.schedule.api.mappers.ScheduleExtractionRequestMapper
import ru.aleshin.studyassistant.backend.ai.schedule.api.mappers.ScheduleExtractionResponseMapper
import ru.aleshin.studyassistant.backend.ai.schedule.api.validation.ScheduleExtractionRequestValidator
import ru.aleshin.studyassistant.backend.ai.schedule.domain.gateway.ScheduleExtractionGateway
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleDraft
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleExtractionRequest
import ru.aleshin.studyassistant.backend.ai.schedule.domain.result.ScheduleProviderResult
import ru.aleshin.studyassistant.backend.ai.services.AiQuotaService
import ru.aleshin.studyassistant.backend.ai.testAiConfig
import ru.aleshin.studyassistant.backend.common.api.ServerUnavailableException
import ru.aleshin.studyassistant.backend.security.InstallationHasher
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class ScheduleExtractionServiceTest {

    @Test
    fun successfulExtractionShouldFinalizeReservation() = runBlocking {
        val repository = FakeAiQuotaRepository()
        val service = service(
            repository = repository,
            gatewayResult = ScheduleProviderResult.Success(
                draft = ScheduleDraft(
                    title = null,
                    entries = emptyList(),
                    unparsedLines = emptyList(),
                ),
            ),
        )

        val response = service.extract(
            installationToken = "installation-token",
            request = request(),
        )

        assertEquals(11, response.quotaRemaining)
        assertEquals(listOf(true), repository.finalizedResults)
        assertEquals(HASH_SIZE_BYTES, repository.requestHash?.size)
    }

    @Test
    fun providerFailureShouldReleaseReservation() = runBlocking {
        val repository = FakeAiQuotaRepository()
        val service = service(
            repository = repository,
            gatewayResult = ScheduleProviderResult.Unavailable,
        )

        assertFailsWith<ServerUnavailableException> {
            service.extract(
                installationToken = "installation-token",
                request = request(),
            )
        }
        assertEquals(listOf(false), repository.finalizedResults)
    }

    private fun service(
        repository: AiQuotaRepository,
        gatewayResult: ScheduleProviderResult,
    ): ScheduleExtractionService {
        val config = testAiConfig()
        val clock = Clock.fixed(
            Instant.parse("2026-08-12T10:00:00Z"),
            ZoneOffset.UTC,
        )

        return ScheduleExtractionService(
            validator = ScheduleExtractionRequestValidator(config = config),
            requestMapper = ScheduleExtractionRequestMapper(),
            responseMapper = ScheduleExtractionResponseMapper(),
            quotaService = AiQuotaService(
                repository = repository,
                installationHasher = InstallationHasher(secret = ByteArray(32) { 1 }),
                clock = clock,
            ),
            extractionGateway = object : ScheduleExtractionGateway {
                override suspend fun extract(request: ScheduleExtractionRequest): ScheduleProviderResult {
                    return gatewayResult
                }
            },
            clock = clock,
        )
    }

    private fun request(): ScheduleExtractionRequestDto {
        return ScheduleExtractionRequestDto(
            requestId = UUID.randomUUID().toString(),
            rawText = "Monday 09:00 Mathematics",
            locale = "en-US",
            timeZone = "Europe/Moscow",
            numberOfWeeks = 1,
        )
    }

    private class FakeAiQuotaRepository : AiQuotaRepository {

        val finalizedResults = mutableListOf<Boolean>()

        var requestHash: ByteArray? = null

        override suspend fun reserve(
            installationHash: ByteArray,
            messageId: UUID,
            requestHash: ByteArray,
            executionHash: ByteArray,
            now: Instant,
        ): AiQuotaReservationResult {
            this.requestHash = requestHash
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

    private companion object {

        const val HASH_SIZE_BYTES = 32
    }
}
