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

package ru.aleshin.studyassistant.backend.sharing.services

import kotlinx.serialization.json.jsonObject
import ru.aleshin.studyassistant.backend.common.api.ClaimedShareException
import ru.aleshin.studyassistant.backend.common.api.ConsumedShareException
import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import ru.aleshin.studyassistant.backend.common.api.InvalidShareException
import ru.aleshin.studyassistant.backend.common.api.RateLimitException
import ru.aleshin.studyassistant.backend.common.api.ServerUnavailableException
import ru.aleshin.studyassistant.backend.plugins.BackendJson
import ru.aleshin.studyassistant.backend.security.ClaimTokenService
import ru.aleshin.studyassistant.backend.security.InstallationHasher
import ru.aleshin.studyassistant.backend.security.PayloadCipher
import ru.aleshin.studyassistant.backend.security.PayloadPurpose
import ru.aleshin.studyassistant.backend.security.ShareCode
import ru.aleshin.studyassistant.backend.security.ShareCodeGenerator
import ru.aleshin.studyassistant.backend.security.ShareCodeHasher
import ru.aleshin.studyassistant.backend.sharing.SharingConfig
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.ClaimScheduleShareRequest
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.ClaimScheduleShareResponse
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.ConfirmScheduleShareRequest
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.CreateScheduleShareRequest
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.CreateScheduleShareResponse
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.ReleaseScheduleShareRequest
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.ScheduleClaimDto
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.ScheduleShareLinkDto
import ru.aleshin.studyassistant.backend.sharing.api.dto.schedule.SuccessResponse
import ru.aleshin.studyassistant.backend.sharing.api.validation.SharePayloadValidator
import ru.aleshin.studyassistant.backend.sharing.domain.model.StoredScheduleShare
import ru.aleshin.studyassistant.backend.sharing.domain.repository.ScheduleSharingRepository
import ru.aleshin.studyassistant.backend.sharing.domain.result.ClaimActionStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.ClaimScheduleShareStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.CodeLookupStorageResult
import ru.aleshin.studyassistant.backend.sharing.domain.result.CreateScheduleShareStorageResult
import java.time.Clock
import java.util.UUID

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
class ScheduleSharingService(
    private val repository: ScheduleSharingRepository,
    private val installationHasher: InstallationHasher,
    private val shareCodeGenerator: ShareCodeGenerator,
    private val shareCodeHasher: ShareCodeHasher,
    private val claimTokenService: ClaimTokenService,
    private val payloadCipher: PayloadCipher,
    private val payloadValidator: SharePayloadValidator,
    private val config: SharingConfig,
    private val clock: Clock,
) {

    suspend fun create(
        installationToken: String,
        request: CreateScheduleShareRequest,
    ): CreateScheduleShareResponse {
        val payload = payloadValidator.validateSchedule(share = request.share)

        val creatorHash = installationHasher.hash(
            installationToken = installationToken,
        )

        val encrypted = payloadCipher.encrypt(
            plaintext = payload.bytes,
            purpose = PayloadPurpose.SCHEDULE_SHARE,
        )

        val createdAt = clock.instant()

        val expiresAt = createdAt.plus(config.scheduleLifetime)

        repeat(CODE_GENERATION_ATTEMPTS) {
            val code = shareCodeGenerator.generate()

            val share = StoredScheduleShare(
                id = UUID.randomUUID(),
                codeHash = shareCodeHasher.hash(code = code),
                creatorHash = creatorHash,
                itemCount = payload.itemCount,
                payload = encrypted.ciphertext,
                payloadNonce = encrypted.nonce,
                createdAt = createdAt,
                expiresAt = expiresAt,
                claimHash = null,
                claimedUntil = null,
                consumedAt = null,
            )

            when (val result = repository.tryCreate(share = share)) {
                CreateScheduleShareStorageResult.Created -> {
                    return CreateScheduleShareResponse(
                        link = ScheduleShareLinkDto(
                            code = code.formatted(),
                            createdAt = createdAt.toEpochMilli(),
                            expiresAt = expiresAt.toEpochMilli(),
                        ),
                    )
                }
                CreateScheduleShareStorageResult.CodeConflict -> {
                    // Generate another code.
                }
                is CreateScheduleShareStorageResult.Limited -> {
                    throw RateLimitException(retryAt = result.retryAt?.toEpochMilli())
                }
            }
        }

        throw ServerUnavailableException()
    }

    suspend fun claim(
        installationToken: String,
        request: ClaimScheduleShareRequest,
    ): ClaimScheduleShareResponse {
        val now = clock.instant()

        val installationHash = installationHasher.hash(
            installationToken = installationToken,
        )

        when (val lookup = repository.recordCodeLookup(installationHash = installationHash, now = now)) {
            is CodeLookupStorageResult.Allowed -> Unit
            is CodeLookupStorageResult.Limited -> {
                throw RateLimitException(retryAt = lookup.retryAt?.toEpochMilli())
            }
        }

        val code = try {
            ShareCode.parse(raw = request.code)
        } catch (_: IllegalArgumentException) {
            throw InvalidRequestException()
        }

        val claimToken = claimTokenService.generate()

        val claimHash = claimTokenService.hash(token = claimToken)

        val claimedUntil = now.plus(
            config.scheduleClaimLifetime,
        )

        val storedShare = when (
            val result = repository.claim(
                codeHash = shareCodeHasher.hash(code = code),
                claimHash = claimHash,
                now = now,
                claimedUntil = claimedUntil,
            )
        ) {
            is ClaimScheduleShareStorageResult.Claimed -> {
                result.share
            }
            ClaimScheduleShareStorageResult.NotFound -> {
                throw InvalidShareException()
            }
            ClaimScheduleShareStorageResult.Consumed -> {
                throw ConsumedShareException()
            }
            is ClaimScheduleShareStorageResult.Busy -> {
                throw ClaimedShareException()
            }
        }

        val share = try {
            val plaintext = payloadCipher.decrypt(
                ciphertext = storedShare.payload,
                nonce = storedShare.payloadNonce,
                purpose = PayloadPurpose.SCHEDULE_SHARE,
            )

            BackendJson.parseToJsonElement(plaintext.decodeToString()).jsonObject
        } catch (_: Exception) {
            /*
             * Payload retrieval failed after claim.
             * Release the lease like the old backend did.
             */
            runCatching {
                repository.release(
                    claimHash = claimHash,
                    now = clock.instant(),
                )
            }

            throw ServerUnavailableException()
        }

        return ClaimScheduleShareResponse(
            claim = ScheduleClaimDto(
                claimToken = claimToken,
                share = share,
            ),
        )
    }

    suspend fun confirm(
        request: ConfirmScheduleShareRequest,
    ): SuccessResponse {
        val claimHash = claimTokenHash(
            token = request.claimToken,
        )

        return when (repository.confirm(claimHash = claimHash, now = clock.instant())) {
            ClaimActionStorageResult.Success -> {
                SuccessResponse(
                    success = true,
                )
            }
            ClaimActionStorageResult.InvalidClaim -> {
                throw ClaimedShareException()
            }
            ClaimActionStorageResult.Consumed -> {
                /*
                 * Currently confirm itself never returns this:
                 * repeated confirm is idempotent.
                 */
                throw ConsumedShareException()
            }
        }
    }

    suspend fun release(
        request: ReleaseScheduleShareRequest,
    ): SuccessResponse {
        val claimHash = claimTokenHash(
            token = request.claimToken,
        )

        return when (repository.release(claimHash = claimHash, now = clock.instant())) {
            ClaimActionStorageResult.Success -> SuccessResponse(success = true)
            ClaimActionStorageResult.InvalidClaim -> throw ClaimedShareException()
            ClaimActionStorageResult.Consumed -> throw ConsumedShareException()
        }
    }

    private fun claimTokenHash(
        token: String,
    ): ByteArray {
        if (token.isBlank() || token.length > MAX_CLAIM_TOKEN_LENGTH) {
            throw ClaimedShareException()
        }

        return try {
            claimTokenService.hash(token = token)
        } catch (_: IllegalArgumentException) {
            throw ClaimedShareException()
        }
    }

    private companion object {

        const val CODE_GENERATION_ATTEMPTS = 5

        const val MAX_CLAIM_TOKEN_LENGTH = 256
    }
}
