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

import ru.aleshin.studyassistant.backend.ai.domain.repository.AiQuotaRepository
import ru.aleshin.studyassistant.backend.ai.domain.result.AiQuotaReservationResult
import ru.aleshin.studyassistant.backend.security.InstallationHasher
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Clock
import java.util.UUID

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
class AiQuotaService(
    private val repository: AiQuotaRepository,
    private val installationHasher: InstallationHasher,
    private val clock: Clock,
) {

    suspend fun reserve(
        installationToken: String,
        messageId: UUID,
        requestFingerprint: String,
        executionFingerprint: String = requestFingerprint,
    ): AiQuotaReservationResult {
        val installationHash = installationHasher.hash(installationToken = installationToken)
        val requestHash = hash(fingerprint = requestFingerprint)
        val executionHash = hash(fingerprint = executionFingerprint)

        return repository.reserve(
            installationHash = installationHash,
            messageId = messageId,
            requestHash = requestHash,
            executionHash = executionHash,
            now = clock.instant(),
        )
    }

    suspend fun finalize(
        installationToken: String,
        messageId: UUID,
        succeeded: Boolean,
        reservationGeneration: Int,
    ) {
        val installationHash = installationHasher.hash(installationToken = installationToken)

        repository.finalize(
            installationHash = installationHash,
            messageId = messageId,
            succeeded = succeeded,
            reservationGeneration = reservationGeneration,
            now = clock.instant(),
        )
    }

    suspend fun saveResponse(
        installationToken: String,
        messageId: UUID,
        executionFingerprint: String,
        responsePayload: ByteArray,
        responseNonce: ByteArray,
    ) {
        val installationHash = installationHasher.hash(installationToken = installationToken)

        repository.saveResponse(
            installationHash = installationHash,
            messageId = messageId,
            executionHash = hash(fingerprint = executionFingerprint),
            responsePayload = responsePayload,
            responseNonce = responseNonce,
        )
    }

    private fun hash(fingerprint: String): ByteArray {
        return MessageDigest
            .getInstance(HASH_ALGORITHM)
            .digest(fingerprint.toByteArray(StandardCharsets.UTF_8))
    }

    private companion object {

        const val HASH_ALGORITHM = "SHA-256"
    }
}
