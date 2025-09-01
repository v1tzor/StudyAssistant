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
import ru.aleshin.studyassistant.core.database.models.shared.homeworks.SharedHomeworksDetailsEntity
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.sqldelight.shared.CurrentSharedHomeworksQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
interface SharedHomeworksLocalDataSource : LocalDataSource.FullSynced.SingleDocument<SharedHomeworksDetailsEntity> {

    class Base(
        private val sharedHomeworksQueries: CurrentSharedHomeworksQueries,
        private val coroutineManager: CoroutineManager,
    ) : SharedHomeworksLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.ioDispatcher

        override suspend fun addOrUpdateItem(item: SharedHomeworksDetailsEntity) {
            sharedHomeworksQueries.addOrUpdateHomeworks(item.mapToEntity()).await()
        }

        override suspend fun fetchItem(): Flow<SharedHomeworksDetailsEntity?> {
            val query = sharedHomeworksQueries.fetchHomeworks()
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun fetchMetadata(): MetadataModel? {
            val query = sharedHomeworksQueries.fetchEmptyHomeworks()
            val emptyItem = query.awaitAsOneOrNull()
            return if (emptyItem != null) {
                MetadataModel(emptyItem.document_id, emptyItem.updated_at)
            } else {
                null
            }
        }

        override suspend fun deleteItem() {
            sharedHomeworksQueries.deleteHomeworks().await()
        }
    }
}