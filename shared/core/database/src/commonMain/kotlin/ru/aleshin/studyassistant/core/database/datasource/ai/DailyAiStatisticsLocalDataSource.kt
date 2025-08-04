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

package ru.aleshin.studyassistant.core.database.datasource.ai

import app.cash.sqldelight.async.coroutines.awaitAsList
import kotlinx.coroutines.flow.Flow
import ru.aleshin.studyassistant.core.common.architecture.data.MetadataModel
import ru.aleshin.studyassistant.core.common.extensions.mapToListFlow
import ru.aleshin.studyassistant.core.common.extensions.mapToOneOrNullFlow
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.database.mappers.ai.mapToBase
import ru.aleshin.studyassistant.core.database.mappers.ai.mapToEntity
import ru.aleshin.studyassistant.core.database.models.ai.BaseDailyAiResponsesEntity
import ru.aleshin.studyassistant.core.database.utils.LocalDataSource
import ru.aleshin.studyassistant.sqldelight.ai.DailyAiResponsesQueries
import kotlin.coroutines.CoroutineContext

/**
 * @author Stanislav Aleshin on 01.08.2025.
 */
interface DailyAiStatisticsLocalDataSource : LocalDataSource.FullSynced.MultipleDocuments<BaseDailyAiResponsesEntity> {

    suspend fun fetchStatisticsByDate(date: Long): Flow<BaseDailyAiResponsesEntity?>

    class Base(
        private val dailyAiResponsesQuery: DailyAiResponsesQueries,
        private val coroutineManager: CoroutineManager,
    ) : DailyAiStatisticsLocalDataSource {

        private val coroutineContext: CoroutineContext
            get() = coroutineManager.backgroundDispatcher

        override suspend fun addOrUpdateItem(item: BaseDailyAiResponsesEntity) {
            dailyAiResponsesQuery.addOrUpdateStatistics(item.mapToEntity()).await()
        }

        override suspend fun addOrUpdateItems(items: List<BaseDailyAiResponsesEntity>) {
            items.forEach { item -> addOrUpdateItem(item) }
        }

        override suspend fun fetchStatisticsByDate(date: Long): Flow<BaseDailyAiResponsesEntity?> {
            val query = dailyAiResponsesQuery.fetchStatisticsByDate(date)
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun fetchItemById(id: String): Flow<BaseDailyAiResponsesEntity?> {
            val query = dailyAiResponsesQuery.fetchStatisticsById(id)
            return query.mapToOneOrNullFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun fetchItemsById(ids: List<String>): Flow<List<BaseDailyAiResponsesEntity>> {
            val query = dailyAiResponsesQuery.fetchStatisticsByIds(ids)
            return query.mapToListFlow(coroutineContext) { it.mapToBase() }
        }

        override suspend fun fetchAllMetadata(): List<MetadataModel> {
            val metadata = dailyAiResponsesQuery.fetchStatisticsMetadata().awaitAsList()
            return metadata.map { MetadataModel(it.id, it.updated_at) }
        }

        override suspend fun deleteItemsById(ids: List<String>) {
            dailyAiResponsesQuery.deleteStatisticsByIds(ids).await()
        }

        override suspend fun deleteAllItems() {
            dailyAiResponsesQuery.deleteAllStatistics().await()
        }
    }
}