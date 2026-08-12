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

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import ru.aleshin.studyassistant.backend.ai.domain.result.AiQuotaReservationResult
import ru.aleshin.studyassistant.backend.ai.schedule.api.dto.ScheduleExtractionRequestDto
import ru.aleshin.studyassistant.backend.ai.schedule.api.dto.ScheduleExtractionResponseDto
import ru.aleshin.studyassistant.backend.ai.schedule.api.mappers.ScheduleExtractionRequestMapper
import ru.aleshin.studyassistant.backend.ai.schedule.api.mappers.ScheduleExtractionResponseMapper
import ru.aleshin.studyassistant.backend.ai.schedule.api.validation.ScheduleExtractionRequestValidator
import ru.aleshin.studyassistant.backend.ai.schedule.domain.gateway.ScheduleExtractionGateway
import ru.aleshin.studyassistant.backend.ai.schedule.domain.model.ScheduleExtractionRequest
import ru.aleshin.studyassistant.backend.ai.schedule.domain.result.ScheduleProviderResult
import ru.aleshin.studyassistant.backend.ai.services.AiQuotaService
import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import ru.aleshin.studyassistant.backend.common.api.QuotaExceededException
import ru.aleshin.studyassistant.backend.common.api.RateLimitException
import ru.aleshin.studyassistant.backend.common.api.ServerUnavailableException
import java.time.Clock

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class ScheduleExtractionService(
    private val validator: ScheduleExtractionRequestValidator,
    private val requestMapper: ScheduleExtractionRequestMapper,
    private val responseMapper: ScheduleExtractionResponseMapper,
    private val quotaService: AiQuotaService,
    private val extractionGateway: ScheduleExtractionGateway,
    private val clock: Clock,
) {

    suspend fun extract(
        installationToken: String,
        request: ScheduleExtractionRequestDto,
    ): ScheduleExtractionResponseDto {
        validator.validate(request = request)
        val command = requestMapper.map(request = request)

        val reservation = when (
            val result = quotaService.reserve(
                installationToken = installationToken,
                messageId = command.requestId,
                requestFingerprint = command.request.fingerprint(),
            )
        ) {
            is AiQuotaReservationResult.Reserved -> result
            is AiQuotaReservationResult.QuotaExceeded -> throw QuotaExceededException(
                quotaResetAt = result.resetAt.toEpochMilli(),
            )
            is AiQuotaReservationResult.RateLimited -> throw RateLimitException(
                retryAt = result.retryAt?.toEpochMilli(),
            )
            AiQuotaReservationResult.MessageExecutionLimitExceeded -> throw InvalidRequestException()
            AiQuotaReservationResult.IdempotencyConflict -> throw InvalidRequestException()
            AiQuotaReservationResult.IdempotencyReplay -> throw InvalidRequestException()
        }

        var succeeded = false

        try {
            return when (val result = extractionGateway.extract(request = command.request)) {
                is ScheduleProviderResult.Success -> {
                    succeeded = true
                    responseMapper.map(
                        draft = result.draft,
                        quota = reservation.quota,
                        quotaResetAt = reservation.resetAt,
                    )
                }
                is ScheduleProviderResult.RateLimited -> throw RateLimitException(
                    retryAt = result.retryAfterSeconds?.let { seconds ->
                        clock.instant().plusSeconds(seconds).toEpochMilli()
                    },
                )
                ScheduleProviderResult.Unauthorized,
                ScheduleProviderResult.InsufficientBalance,
                ScheduleProviderResult.InvalidRequest,
                ScheduleProviderResult.Unavailable, -> throw ServerUnavailableException()
            }
        } finally {
            withContext(NonCancellable) {
                quotaService.finalize(
                    installationToken = installationToken,
                    messageId = command.requestId,
                    succeeded = succeeded,
                )
            }
        }
    }

    private fun ScheduleExtractionRequest.fingerprint(): String {
        return buildString {
            append(SCHEDULE_FINGERPRINT_PREFIX)
            append(numberOfWeeks)
            append(FINGERPRINT_SEPARATOR)
            append(locale)
            append(FINGERPRINT_SEPARATOR)
            append(timeZone)
            append(FINGERPRINT_SEPARATOR)
            append(rawText)
        }
    }

    private companion object {

        const val SCHEDULE_FINGERPRINT_PREFIX = "schedule\u0000"
        const val FINGERPRINT_SEPARATOR = '\u0000'
    }
}
