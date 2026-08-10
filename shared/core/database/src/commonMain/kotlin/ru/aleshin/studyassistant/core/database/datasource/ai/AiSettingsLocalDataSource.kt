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

package ru.aleshin.studyassistant.core.database.datasource.ai

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.sqldelight.ai.AiSettingsEntity
import ru.aleshin.studyassistant.sqldelight.ai.AiSettingsQueries

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
interface AiSettingsLocalDataSource {
    fun fetchSettings(): Flow<AiSettingsEntity>
    suspend fun updateSettings(settings: AiSettingsEntity)

    class Base(
        private val queries: AiSettingsQueries,
        private val coroutineManager: CoroutineManager,
    ) : AiSettingsLocalDataSource {
        override fun fetchSettings(): Flow<AiSettingsEntity> {
            return queries.fetchSettings().asFlow().mapToOne(coroutineManager.ioDispatcher)
        }

        override suspend fun updateSettings(settings: AiSettingsEntity) {
            queries.updateSettings(settings).await()
        }
    }
}
