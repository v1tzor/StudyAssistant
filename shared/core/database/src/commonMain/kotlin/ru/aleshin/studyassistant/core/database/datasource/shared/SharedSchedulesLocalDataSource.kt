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

package ru.aleshin.studyassistant.core.database.datasource.shared

import app.cash.sqldelight.async.coroutines.awaitAsOneOrNull
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.shared.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.shared.mapToEntity
import ru.aleshin.studyassistant.core.database.models.shared.schedules.SharedSchedulesShortDetailsEntity
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.sqldelight.shared.CurrentSharedSchedulesQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
interface SharedSchedulesLocalDataSource : LocalDataSource.FullSynced.SingleDocument<SharedSchedulesShortDetailsEntity> {

    class Base(
        private val sharedSchedulesQueries: CurrentSharedSchedulesQueries,
        private val coroutineManager: CoroutineManager,
    ) : SharedSchedulesLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.ioDispatcher

        override suspend fun addOrUpdateItem(item: SharedSchedulesShortDetailsEntity) {
            sharedSchedulesQueries.addOrUpdateSchedules(item.mapToEntity()).await()
        }

        override suspend fun fetchItem(): Flow<SharedSchedulesShortDetailsEntity?> {
            val query = sharedSchedulesQueries.fetchSchedules()
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun fetchMetadata(): MetadataModel? {
            val query = sharedSchedulesQueries.fetchEmptySchedules()
            val emptyItem = query.awaitAsOneOrNull()
            return if (emptyItem != null) {
                MetadataModel(emptyItem.document_id, emptyItem.updated_at)
            } else {
                null
            }
        }

        override suspend fun deleteItem() {
            sharedSchedulesQueries.deleteSchedule().await()
        }
    }
}