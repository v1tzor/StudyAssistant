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

package ru.aleshin.studyassistant.core.domain.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalShort

/**
 * @author Stanislav Aleshin on 18.04.2025.
 */
interface DailyGoalsRepository {
    suspend fun addOrUpdateGoal(goal: Goal, targetUser: UID): UID
    suspend fun addDailyDailyGoals(dailyGoals: List<Goal>, targetUser: UID)
    suspend fun fetchGoalById(uid: UID, targetUser: UID): Flow<Goal?>
    suspend fun fetchGoalByContentId(contentId: UID, targetUser: UID): Flow<Goal?>
    suspend fun fetchDailyGoalsByTimeRange(timeRange: TimeRange, targetUser: UID): Flow<List<Goal>>
    suspend fun fetchShortDailyGoalsByTimeRange(timeRange: TimeRange, targetUser: UID): Flow<List<GoalShort>>
    suspend fun fetchShortActiveDailyGoals(targetUser: UID): Flow<List<GoalShort>>
    suspend fun fetchOverdueDailyGoals(currentDate: Instant, targetUser: UID): Flow<List<Goal>>
    suspend fun fetchDailyGoalsByDate(date: Instant, targetUser: UID): Flow<List<Goal>>
    suspend fun deleteGoal(uid: UID, targetUser: UID)
    suspend fun deleteAllDailyGoals(targetUser: UID)
    suspend fun transferData(direction: DataTransferDirection, targetUser: UID)
}