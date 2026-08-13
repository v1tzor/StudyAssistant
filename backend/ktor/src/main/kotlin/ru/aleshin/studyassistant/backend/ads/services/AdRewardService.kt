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

package ru.aleshin.studyassistant.backend.ads.services

import ru.aleshin.studyassistant.backend.ads.api.dto.AdRewardChallengeRequestDto
import ru.aleshin.studyassistant.backend.ads.api.dto.AdRewardChallengeResponseDto
import ru.aleshin.studyassistant.backend.ads.api.dto.AdRewardCompletionResponseDto
import ru.aleshin.studyassistant.backend.ads.domain.model.AdRewardPurpose
import ru.aleshin.studyassistant.backend.ads.domain.repository.AdRewardRepository
import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import ru.aleshin.studyassistant.backend.common.api.RewardUnavailableException
import ru.aleshin.studyassistant.backend.security.InstallationHasher
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.time.Clock
import java.util.UUID

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
class AdRewardService(
    private val repository: AdRewardRepository,
    private val installationHasher: InstallationHasher,
    private val clock: Clock,
) {

    suspend fun createChallenge(
        installationToken: String,
        request: AdRewardChallengeRequestDto,
    ): AdRewardChallengeResponseDto {
        val purpose = AdRewardPurpose.entries.firstOrNull { it.value == request.purpose }
            ?: throw InvalidRequestException()
        val subjectHash = when (purpose) {
            AdRewardPurpose.AI_QUOTA_RESET -> null
            AdRewardPurpose.SCHEDULE_IMPORT -> hashSubject(request.subject)
        }
        val challenge = repository.createChallenge(
            installationHash = installationHasher.hash(installationToken = installationToken),
            purpose = purpose,
            subjectHash = subjectHash,
            now = clock.instant(),
        ) ?: throw RewardUnavailableException()

        return AdRewardChallengeResponseDto(
            id = challenge.id.toString(),
            purpose = challenge.purpose.value,
            expiresAt = challenge.expiresAt.toEpochMilli(),
        )
    }

    suspend fun completeChallenge(
        installationToken: String,
        challengeId: String,
    ): AdRewardCompletionResponseDto {
        val id = runCatching { UUID.fromString(challengeId) }
            .getOrElse { throw InvalidRequestException() }
        val completion = repository.completeChallenge(
            installationHash = installationHasher.hash(installationToken = installationToken),
            challengeId = id,
            now = clock.instant(),
        ) ?: throw RewardUnavailableException()

        return AdRewardCompletionResponseDto(
            purpose = completion.purpose.value,
            quotaRemaining = completion.quota?.remaining,
            quotaLimit = completion.quota?.limit,
            rewardedResetsRemaining = completion.quota?.rewardedResetsRemaining,
            quotaResetAt = completion.quotaResetAt?.toEpochMilli(),
        )
    }

    fun hashSubject(subject: String?): ByteArray {
        if (subject.isNullOrBlank() || subject.length > MAX_SUBJECT_LENGTH) {
            throw InvalidRequestException()
        }
        return MessageDigest
            .getInstance(HASH_ALGORITHM)
            .digest(subject.toByteArray(StandardCharsets.UTF_8))
    }

    private companion object {
        const val MAX_SUBJECT_LENGTH = 256
        const val HASH_ALGORITHM = "SHA-256"
    }
}
