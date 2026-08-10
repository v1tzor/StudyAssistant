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
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.ai.mapToLocal
import ru.aleshin.studyassistant.core.database.datasource.ai.DailyAiStatisticsLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.ai.DailyAiResponses
import ru.aleshin.studyassistant.core.domain.repositories.DailyAiStatisticsRepository

/**
 * @author Stanislav Aleshin on 01.08.2025.
 */
class DailyAiStatisticsRepositoryImpl(
    private val localDataSource: DailyAiStatisticsLocalDataSource,
) : DailyAiStatisticsRepository {

    override suspend fun addOrUpdateStatistics(statistics: DailyAiResponses) {
        val upsertModel = statistics.copy(id = statistics.id.ifBlank { randomUUID() })
        localDataSource.addOrUpdateStatistics(upsertModel.mapToLocal())
    }

    override suspend fun fetchStatisticsByDate(date: Instant): Flow<DailyAiResponses?> {
        return localDataSource.fetchStatisticsByDate(date.toEpochMilliseconds()).map { statistics ->
            statistics?.mapToDomain()
        }
    }

    override suspend fun deleteStatisticsById(id: String) {
        localDataSource.deleteStatisticsById(id)
    }
}
