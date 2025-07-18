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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.Instant.Companion.DISTANT_FUTURE
import kotlinx.datetime.Instant.Companion.DISTANT_PAST
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.goals.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.goals.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.goals.mapToRemoteData
import ru.aleshin.studyassistant.core.database.datasource.goals.DailyGoalsLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalShort
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.remote.datasources.billing.SubscriptionChecker
import ru.aleshin.studyassistant.core.remote.datasources.goals.DailyGoalsRemoteDataSource

/**
 * @author Stanislav Aleshin on 19.04.2025.
 */
class DailyGoalsRepositoryImpl(
    private val remoteDataSource: DailyGoalsRemoteDataSource,
    private val localDataSource: DailyGoalsLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : DailyGoalsRepository {

    override suspend fun addOrUpdateGoal(goal: Goal, targetUser: UID): UID {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateGoal(goal.mapToRemoteData(targetUser), targetUser)
        } else {
            localDataSource.addOrUpdateGoal(goal.mapToLocalData())
        }
    }

    override suspend fun addDailyDailyGoals(dailyGoals: List<Goal>, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        if (isSubscriber) {
            remoteDataSource.addDailyDailyGoals(dailyGoals.map { it.mapToRemoteData(targetUser) }, targetUser)
        } else {
            localDataSource.addDailyDailyGoals(dailyGoals.map { it.mapToLocalData() })
        }
    }

    override suspend fun fetchGoalById(uid: UID, targetUser: UID): Flow<Goal?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchGoalById(uid, targetUser).map { goal -> goal?.mapToDomain() }
        } else {
            localDataSource.fetchGoalById(uid).map { goal -> goal?.mapToDomain() }
        }
    }

    override suspend fun fetchGoalByContentId(contentId: UID, targetUser: UID): Flow<Goal?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchGoalByContentId(contentId, targetUser).map { goal -> goal?.mapToDomain() }
        } else {
            localDataSource.fetchGoalByContentId(contentId).map { goal -> goal?.mapToDomain() }
        }
    }

    override suspend fun fetchDailyGoalsByTimeRange(
        timeRange: TimeRange,
        targetUser: UID
    ): Flow<List<Goal>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val from = timeRange.from.toEpochMilliseconds()
        val to = timeRange.to.toEpochMilliseconds()

        return if (isSubscriber) {
            remoteDataSource.fetchDailyGoalsByTimeRange(from, to, targetUser).map { goals ->
                goals.map { it.mapToDomain() }
            }
        } else {
            localDataSource.fetchDailyGoalsByTimeRange(from, to).map { goals ->
                goals.map { it.mapToDomain() }
            }
        }
    }

    override suspend fun fetchShortDailyGoalsByTimeRange(
        timeRange: TimeRange,
        targetUser: UID
    ): Flow<List<GoalShort>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val from = timeRange.from.toEpochMilliseconds()
        val to = timeRange.to.toEpochMilliseconds()

        return if (isSubscriber) {
            remoteDataSource.fetchShortDailyGoalsByTimeRange(from, to, targetUser).map { goals ->
                goals.map { it.mapToDomain() }
            }
        } else {
            localDataSource.fetchShortDailyGoalsByTimeRange(from, to).map { goals ->
                goals.map { it.mapToDomain() }
            }
        }
    }

    override suspend fun fetchShortActiveDailyGoals(targetUser: UID): Flow<List<GoalShort>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchShortActiveDailyGoals(targetUser).map { goals ->
                goals.map { it.mapToDomain() }
            }
        } else {
            localDataSource.fetchShortActiveDailyGoals().map { goals ->
                goals.map { it.mapToDomain() }
            }
        }
    }

    override suspend fun fetchOverdueDailyGoals(
        currentDate: Instant,
        targetUser: UID
    ): Flow<List<Goal>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val date = currentDate.toEpochMilliseconds()

        return if (isSubscriber) {
            remoteDataSource.fetchOverdueDailyGoals(date, targetUser).map { goals ->
                goals.map { it.mapToDomain() }
            }
        } else {
            localDataSource.fetchOverdueDailyGoals(date).map { goals ->
                goals.map { it.mapToDomain() }
            }
        }
    }

    override suspend fun fetchDailyGoalsByDate(date: Instant, targetUser: UID): Flow<List<Goal>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val targetDate = date.toEpochMilliseconds()

        return if (isSubscriber) {
            remoteDataSource.fetchDailyGoalsByDate(targetDate, targetUser).map { goals ->
                goals.map { it.mapToDomain() }
            }
        } else {
            localDataSource.fetchDailyGoalsByDate(targetDate).map { goals ->
                goals.map { it.mapToDomain() }
            }
        }
    }

    override suspend fun deleteGoal(uid: UID, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        if (isSubscriber) {
            remoteDataSource.deleteGoal(uid, targetUser)
        } else {
            localDataSource.deleteGoal(uid)
        }
    }

    override suspend fun deleteAllDailyGoals(targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        if (isSubscriber) {
            remoteDataSource.deleteAllDailyGoals(targetUser)
        } else {
            localDataSource.deleteAllDailyGoals()
        }
    }

    override suspend fun transferData(direction: DataTransferDirection, targetUser: UID) {
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allGoals = remoteDataSource.fetchDailyGoalsByTimeRange(
                    from = DISTANT_PAST.toEpochMilliseconds(),
                    to = DISTANT_FUTURE.toEpochMilliseconds(),
                    targetUser = targetUser,
                ).let { goalsFlow ->
                    return@let goalsFlow.first().map { it.mapToDomain().mapToLocalData() }
                }
                localDataSource.deleteAllDailyGoals()
                localDataSource.addDailyDailyGoals(allGoals)
                remoteDataSource.deleteAllDailyGoals(targetUser)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allGoals = localDataSource.fetchDailyGoalsByTimeRange(
                    from = DISTANT_PAST.toEpochMilliseconds(),
                    to = DISTANT_FUTURE.toEpochMilliseconds(),
                ).let { goalsFlow ->
                    return@let goalsFlow.first().map { it.mapToDomain().mapToRemoteData(targetUser) }
                }
                remoteDataSource.deleteAllDailyGoals(targetUser)
                remoteDataSource.addDailyDailyGoals(allGoals, targetUser)
            }
        }
    }
}