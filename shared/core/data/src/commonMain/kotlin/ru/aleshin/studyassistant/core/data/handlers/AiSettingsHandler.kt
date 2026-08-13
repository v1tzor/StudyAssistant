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

package ru.aleshin.studyassistant.core.data.handlers

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.data.datasources.AiPreferencesLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.ai.AiSettings
import ru.aleshin.studyassistant.sqldelight.ai.AiSettingsEntity

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal interface AiSettingsHandler {

    fun fetchSettings(): Flow<AiSettings>
    suspend fun updateQuota(
        remaining: Int,
        limit: Int,
        rewardedResetsRemaining: Int,
        resetAt: Instant?,
    )

    class Base(
        private val localDataSource: AiPreferencesLocalDataSource,
        private val dateManager: DateManager,
    ) : AiSettingsHandler {

        override fun fetchSettings(): Flow<AiSettings> = localDataSource.fetchSettings().map { entity ->
            val currentTime = dateManager.fetchCurrentInstant()
            val storedResetAt = entity.quota_reset_at?.let(Instant::fromEpochMilliseconds)
            val quotaExpired = storedResetAt != null && storedResetAt <= currentTime
            AiSettings(
                quotaRemaining = if (quotaExpired) {
                    AiSettings.DAILY_QUOTA
                } else {
                    entity.quota_remaining.toInt()
                },
                quotaLimit = if (quotaExpired) {
                    AiSettings.DAILY_QUOTA
                } else {
                    entity.quota_limit.toInt()
                },
                rewardedResetsRemaining = if (quotaExpired) {
                    AiSettings.MAX_REWARDED_RESETS
                } else {
                    entity.rewarded_resets_remaining.toInt()
                },
                quotaResetAt = storedResetAt.takeUnless { quotaExpired },
            )
        }

        override suspend fun updateQuota(
            remaining: Int,
            limit: Int,
            rewardedResetsRemaining: Int,
            resetAt: Instant?,
        ) {
            localDataSource.updateSettings(
                currentSettings().copy(
                    quota_remaining = remaining.coerceIn(
                        minimumValue = 0,
                        maximumValue = AiSettings.MAX_DAILY_QUOTA,
                    ).toLong(),
                    quota_limit = limit.coerceIn(
                        minimumValue = AiSettings.DAILY_QUOTA,
                        maximumValue = AiSettings.MAX_DAILY_QUOTA,
                    ).toLong(),
                    rewarded_resets_remaining = rewardedResetsRemaining.coerceIn(
                        minimumValue = 0,
                        maximumValue = AiSettings.MAX_REWARDED_RESETS,
                    ).toLong(),
                    quota_reset_at = resetAt?.toEpochMilliseconds(),
                )
            )
        }

        private suspend fun currentSettings(): AiSettingsEntity {
            return localDataSource.fetchSettings().first()
        }
    }
}
