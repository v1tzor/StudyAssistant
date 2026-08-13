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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.data.handlers.AiSettingsHandler
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardChallenge
import ru.aleshin.studyassistant.core.domain.entities.ads.AdRewardPurpose
import ru.aleshin.studyassistant.core.domain.managers.InstallationIdProvider
import ru.aleshin.studyassistant.core.domain.repositories.AdRewardRepository
import ru.aleshin.studyassistant.core.remote.datasources.ads.AdRewardRemoteDataSource
import ru.aleshin.studyassistant.core.remote.models.ads.AdRewardChallengeRequestPojo

/**
 * @author Stanislav Aleshin on 13.08.2026.
 */
internal class AdRewardRepositoryImpl(
    private val remoteDataSource: AdRewardRemoteDataSource,
    private val installationIdProvider: InstallationIdProvider,
    private val settingsHandler: AiSettingsHandler,
) : AdRewardRepository {

    override suspend fun createChallenge(
        purpose: AdRewardPurpose,
        subject: String?,
    ): AdRewardChallenge {
        val response = remoteDataSource.createChallenge(
            request = AdRewardChallengeRequestPojo(
                purpose = purpose.value,
                subject = subject,
            ),
            installationToken = installationIdProvider.fetchInstallationId(),
        )
        return AdRewardChallenge(
            id = response.id,
            purpose = AdRewardPurpose.entries.first { it.value == response.purpose },
            expiresAt = Instant.fromEpochMilliseconds(response.expiresAt),
        )
    }

    override suspend fun completeChallenge(challengeId: String) {
        val response = remoteDataSource.completeChallenge(
            challengeId = challengeId,
            installationToken = installationIdProvider.fetchInstallationId(),
        )
        val quotaRemaining = response.quotaRemaining
        val quotaLimit = response.quotaLimit
        val rewardedResetsRemaining = response.rewardedResetsRemaining
        if (quotaRemaining != null &&
            quotaLimit != null &&
            rewardedResetsRemaining != null
        ) {
            settingsHandler.updateQuota(
                remaining = quotaRemaining,
                limit = quotaLimit,
                rewardedResetsRemaining = rewardedResetsRemaining,
                resetAt = response.quotaResetAt?.let(Instant::fromEpochMilliseconds),
            )
        }
    }
}
