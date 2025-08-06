/*
 * Copyright 2024 Stanislav Aleshin
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

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.datasource.settings.CalendarSettingsLocalDataSource.OfflineStorage
import ru.aleshin.studyassistant.core.database.datasource.settings.CalendarSettingsLocalDataSource.SyncStorage
import ru.aleshin.studyassistant.core.database.mappers.settings.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.settings.mapToEntity
import ru.aleshin.studyassistant.core.database.models.settings.BaseCalendarSettingsEntity
import ru.aleshin.studyassistant.core.database.utils.CombinedLocalDataSource
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.core.database.utils.LocalSingleDocumentCommands
import ru.aleshin.studyassistant.sqldelight.settings.CalendarQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 24.04.2024.
 */
interface CalendarSettingsLocalDataSource : CombinedLocalDataSource<BaseCalendarSettingsEntity, OfflineStorage, SyncStorage> {

    interface Commands : LocalSingleDocumentCommands<BaseCalendarSettingsEntity> {

        abstract class Abstract(
            isCacheSource: Boolean,
            private val calendarQueries: CalendarQueries,
            private val coroutineManager: CoroutineManager,
        ) : Commands {

            private val coroutineContext: CoroutineContext
                get() = coroutineManager.backgroundDispatcher

            private val isCacheData = if (isCacheSource) 1L else 0L

            override suspend fun addOrUpdateItem(item: BaseCalendarSettingsEntity) {
                val updatedModel = item.mapToEntity(id = isCacheData + 1L).copy(is_cache_data = isCacheData)
                calendarQueries.addOrUpdateSettings(updatedModel).await()
            }

            override suspend fun fetchItem(): Flow<BaseCalendarSettingsEntity?> {
                val query = calendarQueries.fetchSettings(isCacheData)
                return query.mapToOneOrNullFlow(coroutineContext) {
                    it.mapToBase()
                }.map {
                    it ?: BaseCalendarSettingsEntity.default().copy(isCacheData = isCacheData)
                }
            }

            override suspend fun fetchMetadata(): MetadataModel? {
                val emptyModel = calendarQueries.fetchEmptySettings().awaitAsOneOrNull()
                return if (emptyModel != null) {
                    MetadataModel(emptyModel.document_id ?: "", emptyModel.updated_at)
                } else {
                    null
                }
            }

            override suspend fun deleteItem() {
                calendarQueries.deleteSettings(isCacheData).await()
            }
        }
    }

    interface OfflineStorage : LocalDataSource.OnlyOffline, Commands {

        class Base(
            calendarQueries: CalendarQueries,
            coroutineManager: CoroutineManager,
        ) : OfflineStorage, Commands.Abstract(
            isCacheSource = false,
            calendarQueries = calendarQueries,
            coroutineManager = coroutineManager,
        )
    }

    interface SyncStorage : LocalDataSource.FullSynced.SingleDocument<BaseCalendarSettingsEntity>, Commands {

        class Base(
            calendarQueries: CalendarQueries,
            coroutineManager: CoroutineManager,
        ) : SyncStorage, Commands.Abstract(
            isCacheSource = true,
            calendarQueries = calendarQueries,
            coroutineManager = coroutineManager,
        )
    }

    class Base(
        private val offlineStorage: OfflineStorage,
        private val syncStorage: SyncStorage
    ) : CalendarSettingsLocalDataSource {
        override fun offline() = offlineStorage
        override fun sync() = syncStorage
    }
}