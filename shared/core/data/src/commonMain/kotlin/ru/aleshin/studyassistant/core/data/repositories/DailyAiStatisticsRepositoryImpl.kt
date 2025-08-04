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

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToLocal
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToRemote
import ru.aleshin.studyassistant.core.data.utils.sync.RemoteResultSyncHandler
import ru.aleshin.studyassistant.core.database.datasource.ai.DailyAiStatisticsLocalDataSource
import ru.aleshin.studyassistant.core.database.models.ai.BaseDailyAiResponsesEntity
import ru.aleshin.studyassistant.core.domain.entities.ai.DailyAiResponses
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType
import ru.aleshin.studyassistant.core.domain.managers.sync.DailyAiStatisticsSourceSyncManager.Companion.DAILY_AI_STATISTICS_SOURCE_KEY
import ru.aleshin.studyassistant.core.domain.repositories.DailyAiStatisticsRepository
import ru.aleshin.studyassistant.core.remote.datasources.ai.DailyAiStatisticsRemoteDataSource

/**
 * @author Stanislav Aleshin on 01.08.2025.
 */
class DailyAiStatisticsRepositoryImpl(
    private val localDataSource: DailyAiStatisticsLocalDataSource,
    private val remoteDataSource: DailyAiStatisticsRemoteDataSource,
    private val userSessionProvider: UserSessionProvider,
    private val resultSyncHandler: RemoteResultSyncHandler,
    private val dateManager: DateManager,
) : DailyAiStatisticsRepository {

    override suspend fun addOrUpdateStatistics(statistics: DailyAiResponses) {
        val currentUser = userSessionProvider.getCurrentUserId()

        val upsertModel = statistics.copy(id = statistics.id.ifBlank { randomUUID() })

        localDataSource.addOrUpdateItem(upsertModel.mapToLocal())
        resultSyncHandler.executeOrAddToQueue(
            data = upsertModel.mapToRemote(userId = currentUser),
            type = OfflineChangeType.UPSERT,
            sourceKey = DAILY_AI_STATISTICS_SOURCE_KEY,
        ) {
            remoteDataSource.addOrUpdateItem(it)
        }
    }

    override suspend fun incrementResponseByDate(date: Instant): DailyAiResponses {
        val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()

        val statistics = localDataSource.fetchStatisticsByDate(date.toEpochMilliseconds()).firstOrNull()
        val updatedStatistics = statistics?.let {
            it.copy(totalResponses = it.totalResponses + 1L, updatedAt = updatedAt)
        } ?: BaseDailyAiResponsesEntity(
            uid = randomUUID(),
            totalResponses = 1L,
            date = date.toEpochMilliseconds(),
            updatedAt = updatedAt,
        )
        addOrUpdateStatistics(updatedStatistics.mapToDomain())

        return updatedStatistics.mapToDomain()
    }

    override suspend fun fetchStatisticsByDate(date: Instant): Flow<DailyAiResponses?> {
        return localDataSource.fetchStatisticsByDate(date.toEpochMilliseconds()).map { statistics ->
            statistics?.mapToDomain()
        }
    }

    override suspend fun deleteStatisticsById(id: String) {
        localDataSource.deleteItemsById(listOf(id))
        resultSyncHandler.executeOrAddToQueue(
            documentId = id,
            type = OfflineChangeType.DELETE,
            sourceKey = DAILY_AI_STATISTICS_SOURCE_KEY,
        ) {
            remoteDataSource.deleteItemById(id)
        }
    }
}