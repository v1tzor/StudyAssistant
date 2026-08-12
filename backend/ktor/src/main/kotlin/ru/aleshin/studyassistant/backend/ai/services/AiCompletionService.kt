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

package ru.aleshin.studyassistant.backend.ai.services

import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import ru.aleshin.studyassistant.backend.ai.api.dto.AiCompletionRequestDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiCompletionResponseDto
import ru.aleshin.studyassistant.backend.ai.api.mappers.AiCompletionRequestMapper
import ru.aleshin.studyassistant.backend.ai.api.mappers.AiCompletionResponseMapper
import ru.aleshin.studyassistant.backend.ai.api.validation.AiCompletionRequestValidator
import ru.aleshin.studyassistant.backend.ai.domain.gateway.AiCompletionGateway
import ru.aleshin.studyassistant.backend.ai.domain.result.AiProviderResult
import ru.aleshin.studyassistant.backend.ai.domain.result.AiQuotaReservationResult
import ru.aleshin.studyassistant.backend.ai.domain.services.AiCompletionFingerprintFactory
import ru.aleshin.studyassistant.backend.ai.domain.services.AiCompletionRequestFactory
import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import ru.aleshin.studyassistant.backend.common.api.QuotaExceededException
import ru.aleshin.studyassistant.backend.common.api.RateLimitException
import ru.aleshin.studyassistant.backend.common.api.ServerUnavailableException
import java.time.Clock

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class AiCompletionService(
    private val validator: AiCompletionRequestValidator,
    private val requestMapper: AiCompletionRequestMapper,
    private val responseMapper: AiCompletionResponseMapper,
    private val requestFactory: AiCompletionRequestFactory,
    private val fingerprintFactory: AiCompletionFingerprintFactory,
    private val quotaService: AiQuotaService,
    private val completionGateway: AiCompletionGateway,
    private val clock: Clock,
) {

    suspend fun complete(
        installationToken: String,
        request: AiCompletionRequestDto,
    ): AiCompletionResponseDto {
        validator.validate(request = request)
        val command = requestMapper.map(request = request)
        val completionRequest = requestFactory.create(
            command = command,
            now = clock.instant(),
        )

        val reservation = when (
            val result = quotaService.reserve(
                installationToken = installationToken,
                messageId = command.messageId,
                requestFingerprint = fingerprintFactory.conversation(command = command),
                executionFingerprint = fingerprintFactory.execution(command = command),
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
            return when (val result = completionGateway.complete(request = completionRequest)) {
                is AiProviderResult.Success -> {
                    succeeded = true
                    responseMapper.map(
                        completion = result.completion,
                        quota = reservation.quota,
                        quotaResetAt = reservation.resetAt,
                    )
                }

                is AiProviderResult.RateLimited -> throw RateLimitException(
                    retryAt = result.retryAfterSeconds?.let { seconds ->
                        clock.instant().plusSeconds(seconds).toEpochMilli()
                    },
                )

                AiProviderResult.Unauthorized,
                AiProviderResult.InsufficientBalance,
                AiProviderResult.InvalidRequest,
                AiProviderResult.Unavailable,
                -> throw ServerUnavailableException()
            }
        } finally {
            withContext(NonCancellable) {
                quotaService.finalize(
                    installationToken = installationToken,
                    messageId = command.messageId,
                    succeeded = succeeded,
                )
            }
        }
    }
}
