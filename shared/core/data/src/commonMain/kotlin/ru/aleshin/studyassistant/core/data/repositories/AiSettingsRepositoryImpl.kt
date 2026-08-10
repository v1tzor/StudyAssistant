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

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.data.handlers.AiSettingsHandler
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceType
import ru.aleshin.studyassistant.core.domain.entities.ai.AiSettings
import ru.aleshin.studyassistant.core.domain.repositories.AiSettingsRepository

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal class AiSettingsRepositoryImpl(
    private val settingsHandler: AiSettingsHandler,
) : AiSettingsRepository {

    override fun fetchSettings(): Flow<AiSettings> = settingsHandler.fetchSettings()

    override suspend fun updateServiceType(serviceType: AiServiceType) {
        settingsHandler.updateServiceType(serviceType)
    }

    override suspend fun savePersonalKey(apiKey: String) {
        settingsHandler.savePersonalKey(apiKey)
    }

    override suspend fun deletePersonalKey() {
        settingsHandler.deletePersonalKey()
    }

    override suspend fun fetchPersonalKey(): String? = settingsHandler.fetchPersonalKey()

    override suspend fun updateSharedQuota(remaining: Int, resetAt: Instant?) {
        settingsHandler.updateSharedQuota(remaining, resetAt)
    }
}
