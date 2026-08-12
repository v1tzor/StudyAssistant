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

package ru.aleshin.studyassistant.core.data.datasources

import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.database.datasource.ai.AiSettingsLocalDataSource
import ru.aleshin.studyassistant.sqldelight.ai.AiSettingsEntity

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
interface AiPreferencesLocalDataSource {

    fun fetchSettings(): Flow<AiSettingsEntity>
    suspend fun updateSettings(settings: AiSettingsEntity)

    class Base(
        private val settingsDataSource: AiSettingsLocalDataSource,
    ) : AiPreferencesLocalDataSource {

        override fun fetchSettings(): Flow<AiSettingsEntity> = settingsDataSource.fetchSettings()

        override suspend fun updateSettings(settings: AiSettingsEntity) {
            settingsDataSource.updateSettings(settings)
        }
    }
}
