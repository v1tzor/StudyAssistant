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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.settings.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.settings.mapToEntity
import ru.aleshin.studyassistant.core.database.models.settings.BaseCalendarSettingsEntity
import ru.aleshin.studyassistant.sqldelight.settings.CalendarQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 24.04.2024.
 */
interface CalendarSettingsLocalDataSource {

    suspend fun addOrUpdateSettings(item: BaseCalendarSettingsEntity)
    suspend fun fetchSettings(): Flow<BaseCalendarSettingsEntity?>

    class Base(
        private val calendarQueries: CalendarQueries,
        private val coroutineManager: CoroutineManager,
    ) : CalendarSettingsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.ioDispatcher

        override suspend fun addOrUpdateSettings(item: BaseCalendarSettingsEntity) {
            val updatedModel = item.mapToEntity(id = LOCAL_SETTINGS_ID)
            calendarQueries.addOrUpdateSettings(updatedModel)
        }

        override suspend fun fetchSettings(): Flow<BaseCalendarSettingsEntity?> {
            val query = calendarQueries.fetchSettings()
            return query.mapToOneOrNullFlow(coroutineContext) {
                it.mapToBase()
            }.map {
                it ?: BaseCalendarSettingsEntity.default()
            }
        }

        private companion object {
            const val LOCAL_SETTINGS_ID = 1L
        }
    }
}
