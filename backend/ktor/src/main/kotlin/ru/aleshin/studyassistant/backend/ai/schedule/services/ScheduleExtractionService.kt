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
import kotlinx.serialization.json.Json
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
import ru.aleshin.studyassistant.backend.plugins.BackendJson
import ru.aleshin.studyassistant.backend.security.PayloadCipher
import ru.aleshin.studyassistant.backend.security.PayloadPurpose
import java.nio.charset.StandardCharsets.UTF_8
import java.security.MessageDigest
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
    private val payloadCipher: PayloadCipher,
    private val json: Json = BackendJson,
) {

    suspend fun extract(
        installationToken: String,
        request: ScheduleExtractionRequestDto,
    ): ScheduleExtractionResponseDto {
        validator.validate(request = request)
        val command = requestMapper.map(request = request)
        val requestFingerprint = command.request.fingerprint()

        val reservation = when (
            val result = quotaService.reserve(
                installationToken = installationToken,
                messageId = command.requestId,
                requestFingerprint = requestFingerprint,
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
            is AiQuotaReservationResult.IdempotencyReplay -> {
                val payload = result.responsePayload
                val nonce = result.responseNonce
                if (payload == null || nonce == null) throw RateLimitException(
                    retryAt = clock.instant().plusSeconds(5).toEpochMilli(),
                )
                val cachedResponse = runCatching {
                    val response = payloadCipher.decrypt(
                        ciphertext = payload,
                        nonce = nonce,
                        purpose = PayloadPurpose.AI_RESPONSE_CACHE,
                    ).toString(UTF_8)
                    json.decodeFromString<ScheduleExtractionResponseDto>(response)
                }.getOrElse {
                    throw ServerUnavailableException()
                }
                return result.quota?.let { quota ->
                    cachedResponse.copy(
                        quotaRemaining = quota.remaining,
                        quotaLimit = quota.limit,
                        rewardedResetsRemaining = quota.rewardedResetsRemaining,
                        quotaResetAt = result.resetAt?.toEpochMilli() ?: cachedResponse.quotaResetAt,
                    )
                } ?: cachedResponse
            }
        }

        var succeeded = false

        try {
            return when (val result = extractionGateway.extract(request = command.request)) {
                is ScheduleProviderResult.Success -> {
                    val response = responseMapper.map(
                        draft = result.draft,
                        quota = reservation.quota,
                        quotaResetAt = reservation.resetAt,
                    )
                    val encrypted = payloadCipher.encrypt(
                        plaintext = json.encodeToString(response).toByteArray(UTF_8),
                        purpose = PayloadPurpose.AI_RESPONSE_CACHE,
                    )
                    quotaService.saveResponse(
                        installationToken = installationToken,
                        messageId = command.requestId,
                        executionFingerprint = requestFingerprint,
                        responsePayload = encrypted.ciphertext,
                        responseNonce = encrypted.nonce,
                    )
                    succeeded = true
                    response
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
            append(todayDate)
            append(FINGERPRINT_SEPARATOR)
            append(note.orEmpty())
            append(FINGERPRINT_SEPARATOR)
            append(sha256Hex(imageBytes))
        }
    }

    private fun sha256Hex(bytes: ByteArray): String {
        return MessageDigest
            .getInstance(HASH_ALGORITHM)
            .digest(bytes)
            .joinToString(separator = "") { value -> "%02x".format(value) }
    }

    private companion object {

        const val SCHEDULE_FINGERPRINT_PREFIX = "schedule\u0000"
        const val FINGERPRINT_SEPARATOR = '\u0000'
        const val HASH_ALGORITHM = "SHA-256"
    }
}
