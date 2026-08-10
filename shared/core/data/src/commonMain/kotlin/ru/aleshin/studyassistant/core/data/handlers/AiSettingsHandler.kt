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
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceType
import ru.aleshin.studyassistant.core.domain.entities.ai.AiSettings
import ru.aleshin.studyassistant.sqldelight.ai.AiSettingsEntity

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal interface AiSettingsHandler {

    fun fetchSettings(): Flow<AiSettings>
    suspend fun updateServiceType(serviceType: AiServiceType)
    suspend fun savePersonalKey(apiKey: String)
    suspend fun deletePersonalKey()
    suspend fun fetchPersonalKey(): String?
    suspend fun updateSharedQuota(remaining: Int, resetAt: Instant?)

    class Base(
        private val localDataSource: AiPreferencesLocalDataSource,
        private val dateManager: DateManager,
    ) : AiSettingsHandler {

        override fun fetchSettings(): Flow<AiSettings> = localDataSource.fetchSettings().map { entity ->
            val currentTime = dateManager.fetchCurrentInstant()
            val storedResetAt = entity.shared_quota_reset_at?.let(Instant::fromEpochMilliseconds)
            val quotaExpired = storedResetAt != null && storedResetAt <= currentTime
            AiSettings(
                serviceType = AiServiceType.valueOf(entity.service_type),
                hasPersonalKey = localDataSource.fetchPersonalKey() != null,
                sharedQuotaRemaining = if (quotaExpired) {
                    AiSettings.SHARED_DAILY_QUOTA
                } else {
                    entity.shared_quota_remaining.toInt()
                },
                sharedQuotaResetAt = storedResetAt.takeUnless { quotaExpired },
            )
        }

        override suspend fun updateServiceType(serviceType: AiServiceType) {
            check(serviceType != AiServiceType.PERSONAL || localDataSource.fetchPersonalKey() != null) {
                "Personal AI key is not configured"
            }
            localDataSource.updateSettings(currentSettings().copy(service_type = serviceType.name))
        }

        override suspend fun savePersonalKey(apiKey: String) {
            require(apiKey.isNotBlank()) { "AI key must not be blank" }
            localDataSource.savePersonalKey(apiKey.trim())
            localDataSource.updateSettings(currentSettings().copy(has_personal_key = 1L))
        }

        override suspend fun deletePersonalKey() {
            localDataSource.deletePersonalKey()
            localDataSource.updateSettings(
                currentSettings().copy(
                    service_type = AiServiceType.SHARED.name,
                    has_personal_key = 0L,
                )
            )
        }

        override suspend fun fetchPersonalKey(): String? = localDataSource.fetchPersonalKey()

        override suspend fun updateSharedQuota(remaining: Int, resetAt: Instant?) {
            localDataSource.updateSettings(
                currentSettings().copy(
                    shared_quota_remaining = remaining.coerceIn(
                        minimumValue = 0,
                        maximumValue = AiSettings.SHARED_DAILY_QUOTA,
                    ).toLong(),
                    shared_quota_reset_at = resetAt?.toEpochMilliseconds(),
                )
            )
        }

        private suspend fun currentSettings(): AiSettingsEntity {
            return localDataSource.fetchSettings().first()
        }
    }
}
