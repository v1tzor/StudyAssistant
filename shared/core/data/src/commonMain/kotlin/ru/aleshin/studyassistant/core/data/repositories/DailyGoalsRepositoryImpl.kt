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

@file:OptIn(ExperimentalCoroutinesApi::class)

package ru.aleshin.studyassistant.core.data.repositories

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.api.auth.UserSessionProvider
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.goals.convertToLocal
import ru.aleshin.studyassistant.core.data.mappers.goals.convertToRemote
import ru.aleshin.studyassistant.core.data.mappers.goals.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.goals.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.goals.mapToRemoteData
import ru.aleshin.studyassistant.core.data.utils.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.utils.sync.RemoteResultSyncHandler
import ru.aleshin.studyassistant.core.database.datasource.goals.DailyGoalsLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalShort
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType
import ru.aleshin.studyassistant.core.domain.managers.sync.DailyGoalsSourceSyncManager.Companion.GOAL_SOURCE_KEY
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.remote.datasources.goals.DailyGoalsRemoteDataSource

/**
 * @author Stanislav Aleshin on 19.04.2025.
 */
class DailyGoalsRepositoryImpl(
    private val remoteDataSource: DailyGoalsRemoteDataSource,
    private val localDataSource: DailyGoalsLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
    private val userSessionProvider: UserSessionProvider,
    private val resultSyncHandler: RemoteResultSyncHandler,
) : DailyGoalsRepository {

    override suspend fun addOrUpdateGoal(goal: Goal): UID {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        val upsertModel = goal.copy(uid = goal.uid.ifBlank { randomUUID() })

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItem(upsertModel.mapToLocalData())
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModel.mapToRemoteData(userId = currentUser),
                type = OfflineChangeType.UPSERT,
                sourceKey = GOAL_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItem(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItem(upsertModel.mapToLocalData())
        }

        return upsertModel.uid
    }

    override suspend fun addDailyDailyGoals(dailyGoals: List<Goal>) {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        val upsertModels = dailyGoals.map { goal -> goal.copy(uid = goal.uid.ifBlank { randomUUID() }) }

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItems(upsertModels.map { it.mapToLocalData() })
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModels.map { it.mapToRemoteData(currentUser) },
                type = OfflineChangeType.UPSERT,
                sourceKey = GOAL_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItems(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItems(upsertModels.map { it.mapToLocalData() })
        }
    }

    override suspend fun fetchGoalById(uid: UID): Flow<Goal?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchGoalDetailsById(uid).map { goal ->
                    goal?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchGoalDetailsById(uid).map { goal ->
                    goal?.mapToDomain()
                }
            }
        }
    }

    override suspend fun fetchGoalByContentId(contentId: UID): Flow<Goal?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchGoalDetailsByContentId(contentId).map { goal ->
                    goal?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchGoalDetailsByContentId(contentId).map { goal ->
                    goal?.mapToDomain()
                }
            }
        }
    }

    override suspend fun fetchDailyGoalsByTimeRange(timeRange: TimeRange): Flow<List<Goal>> {
        val from = timeRange.from.toEpochMilliseconds()
        val to = timeRange.to.toEpochMilliseconds()

        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchGoalsDetailsByTimeRange(from, to).map { goals ->
                    goals.map { goal -> goal.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchGoalsDetailsByTimeRange(from, to).map { goals ->
                    goals.map { goal -> goal.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchShortDailyGoalsByTimeRange(timeRange: TimeRange): Flow<List<GoalShort>> {
        val from = timeRange.from.toEpochMilliseconds()
        val to = timeRange.to.toEpochMilliseconds()

        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchShortGoalsByTimeRange(from, to).map { goals ->
                    goals.map { it.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchShortGoalsByTimeRange(from, to).map { goals ->
                    goals.map { it.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchShortActiveDailyGoals(): Flow<List<GoalShort>> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchShortActiveDailyGoals().map { goals ->
                    goals.map { it.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchShortActiveDailyGoals().map { goals ->
                    goals.map { it.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchOverdueDailyGoals(currentDate: Instant): Flow<List<Goal>> {
        val date = currentDate.toEpochMilliseconds()

        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchOverdueGoalsDetails(date).map { goals ->
                    goals.map { it.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchOverdueGoalsDetails(date).map { goals ->
                    goals.map { it.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchDailyGoalsByDate(date: Instant): Flow<List<Goal>> {
        val targetDate = date.toEpochMilliseconds()

        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchGoalDetailsByDate(targetDate).map { goals ->
                    goals.map { it.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchGoalDetailsByDate(targetDate).map { goals ->
                    goals.map { it.mapToDomain() }
                }
            }
        }
    }

    override suspend fun deleteGoal(uid: UID) {
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        return if (isSubscriber) {
            localDataSource.sync().deleteItemsById(listOf(uid))
            resultSyncHandler.executeOrAddToQueue(
                documentId = uid,
                type = OfflineChangeType.DELETE,
                sourceKey = GOAL_SOURCE_KEY,
            ) {
                remoteDataSource.deleteItemById(uid)
            }
        } else {
            localDataSource.offline().deleteItemsById(listOf(uid))
        }
    }

    override suspend fun transferData(direction: DataTransferDirection) {
        val currentUser = userSessionProvider.getCurrentUserId()
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allGoalsFlow = remoteDataSource.fetchAllItems(currentUser)
                val goals = allGoalsFlow.first().map { it.convertToLocal() }
                localDataSource.offline().deleteAllItems()
                localDataSource.offline().addOrUpdateItems(goals)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allGoals = localDataSource.offline().fetchAllGoals().first()
                val goalsRemote = allGoals.map { it.convertToRemote(currentUser) }

                remoteDataSource.deleteAllItems(currentUser)
                remoteDataSource.addOrUpdateItems(goalsRemote)

                localDataSource.sync().deleteAllItems()
                localDataSource.sync().addOrUpdateItems(allGoals)
            }
        }
    }
}