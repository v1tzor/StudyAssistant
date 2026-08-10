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
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.goals.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.goals.mapToLocalData
import ru.aleshin.studyassistant.core.database.datasource.goals.DailyGoalsLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalShort
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository

/**
 * @author Stanislav Aleshin on 19.04.2025.
 */
class DailyGoalsRepositoryImpl(
    private val localDataSource: DailyGoalsLocalDataSource,
) : DailyGoalsRepository {

    override suspend fun addOrUpdateGoal(goal: Goal): UID {
        val updatedGoal = goal.copy(uid = goal.uid.ifBlank { randomUUID() })
        localDataSource.addOrUpdateGoal(updatedGoal.mapToLocalData())
        return updatedGoal.uid
    }

    override suspend fun addDailyDailyGoals(dailyGoals: List<Goal>) {
        val updatedGoals = dailyGoals.map { goal ->
            goal.copy(uid = goal.uid.ifBlank { randomUUID() })
        }
        localDataSource.addOrUpdateGoals(updatedGoals.map { it.mapToLocalData() })
    }

    override suspend fun fetchGoalById(uid: UID): Flow<Goal?> {
        return localDataSource.fetchGoalDetailsById(uid).map { goal -> goal?.mapToDomain() }
    }

    override suspend fun fetchGoalByContentId(contentId: UID): Flow<Goal?> {
        return localDataSource.fetchGoalDetailsByContentId(contentId).map { goal -> goal?.mapToDomain() }
    }

    override suspend fun fetchDailyGoalsByTimeRange(timeRange: TimeRange): Flow<List<Goal>> {
        return localDataSource.fetchGoalsDetailsByTimeRange(
            from = timeRange.from.toEpochMilliseconds(),
            to = timeRange.to.toEpochMilliseconds(),
        ).map { goals -> goals.map { it.mapToDomain() } }
    }

    override suspend fun fetchShortDailyGoalsByTimeRange(timeRange: TimeRange): Flow<List<GoalShort>> {
        return localDataSource.fetchShortGoalsByTimeRange(
            from = timeRange.from.toEpochMilliseconds(),
            to = timeRange.to.toEpochMilliseconds(),
        ).map { goals -> goals.map { it.mapToDomain() } }
    }

    override suspend fun fetchShortActiveDailyGoals(): Flow<List<GoalShort>> {
        return localDataSource.fetchShortActiveDailyGoals().map { goals ->
            goals.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchOverdueDailyGoals(currentDate: Instant): Flow<List<Goal>> {
        return localDataSource.fetchOverdueGoalsDetails(currentDate.toEpochMilliseconds()).map { goals ->
            goals.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchDailyGoalsByDate(date: Instant): Flow<List<Goal>> {
        return localDataSource.fetchGoalDetailsByDate(date.toEpochMilliseconds()).map { goals ->
            goals.map { it.mapToDomain() }
        }
    }

    override suspend fun deleteGoal(uid: UID) {
        localDataSource.deleteGoalsByIds(listOf(uid))
    }
}
