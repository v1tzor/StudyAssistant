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

package ru.aleshin.studyassistant.core.database.datasource.settings

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.sqldelight.settings.AnalyticsQueries
import ru.aleshin.studyassistant.sqldelight.settings.AnalyticsSettingsEntity
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
interface AnalyticsSettingsLocalDataSource {

    fun fetchSettings(): Flow<AnalyticsSettingsEntity>

    suspend fun updateSettings(settings: AnalyticsSettingsEntity)

    suspend fun resetSettings()

    class Base(
        private val analyticsQueries: AnalyticsQueries,
        private val coroutineManager: CoroutineManager,
    ) : AnalyticsSettingsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.ioDispatcher

        override fun fetchSettings(): Flow<AnalyticsSettingsEntity> {
            return analyticsQueries.fetchSettings().asFlow().mapToOne(coroutineContext)
        }

        override suspend fun updateSettings(settings: AnalyticsSettingsEntity) {
            analyticsQueries.updateSettings(settings)
        }

        override suspend fun resetSettings() {
            analyticsQueries.resetSettings()
        }
    }
}
