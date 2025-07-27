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
import ru.aleshin.studyassistant.core.data.mappers.schedules.convertToLocal
import ru.aleshin.studyassistant.core.data.mappers.schedules.convertToRemote
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToRemoteData
import ru.aleshin.studyassistant.core.data.utils.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.utils.sync.RemoteResultSyncHandler
import ru.aleshin.studyassistant.core.database.datasource.schedules.CustomScheduleLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType
import ru.aleshin.studyassistant.core.domain.managers.sync.CustomScheduleSourceSyncManager.Companion.CUSTOM_SCHEDULE_SOURCE_KEY
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.remote.datasources.schedules.CustomScheduleRemoteDataSource

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
class CustomScheduleRepositoryImpl(
    private val remoteDataSource: CustomScheduleRemoteDataSource,
    private val localDataSource: CustomScheduleLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
    private val userSessionProvider: UserSessionProvider,
    private val resultSyncHandler: RemoteResultSyncHandler,
) : CustomScheduleRepository {

    override suspend fun addOrUpdateSchedule(schedule: CustomSchedule): UID {
        val currentUser = userSessionProvider.getCurrentUserId()
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        val upsertModel = schedule.copy(uid = schedule.uid.ifBlank { randomUUID() })

        if (isSubscriber) {
            localDataSource.sync().addOrUpdateItem(upsertModel.mapToLocalData())
            resultSyncHandler.executeOrAddToQueue(
                data = upsertModel.mapToRemoteData(userId = currentUser),
                type = OfflineChangeType.UPSERT,
                sourceKey = CUSTOM_SCHEDULE_SOURCE_KEY,
            ) {
                remoteDataSource.addOrUpdateItem(it)
            }
        } else {
            localDataSource.offline().addOrUpdateItem(upsertModel.mapToLocalData())
        }

        return upsertModel.uid
    }

    override suspend fun fetchScheduleById(uid: UID): Flow<CustomSchedule?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchScheduleDetailsById(uid).map { scheduleEntity ->
                    scheduleEntity?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchScheduleDetailsById(uid).map { scheduleEntity ->
                    scheduleEntity?.mapToDomain()
                }
            }
        }
    }

    override suspend fun fetchScheduleByDate(date: Instant): Flow<CustomSchedule?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchScheduleDetailsByDate(date).map { scheduleEntity ->
                    scheduleEntity?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchScheduleDetailsByDate(date).map { scheduleEntity ->
                    scheduleEntity?.mapToDomain()
                }
            }
        }
    }

    override suspend fun fetchSchedulesByTimeRange(timeRange: TimeRange): Flow<List<CustomSchedule>> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchSchedulesDetailsByTimeRange(timeRange.from, timeRange.to).map { schedules ->
                    schedules.map { scheduleEntity -> scheduleEntity.mapToDomain() }
                }
            } else {
                localDataSource.offline().fetchSchedulesDetailsByTimeRange(timeRange.from, timeRange.to).map { schedules ->
                    schedules.map { scheduleEntity -> scheduleEntity.mapToDomain() }
                }
            }
        }
    }

    override suspend fun fetchClassById(uid: UID, scheduleId: UID): Flow<Class?> {
        return subscriptionChecker.getSubscriberStatusFlow().flatMapLatest { isSubscriber ->
            if (isSubscriber) {
                localDataSource.sync().fetchClassById(uid, scheduleId).map { classEntity ->
                    classEntity?.mapToDomain()
                }
            } else {
                localDataSource.offline().fetchClassById(uid, scheduleId).map { classEntity ->
                    classEntity?.mapToDomain()
                }
            }
        }
    }

    override suspend fun deleteScheduleById(scheduleId: UID) {
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        return if (isSubscriber) {
            localDataSource.sync().deleteItemsById(listOf(scheduleId))
            resultSyncHandler.executeOrAddToQueue(
                documentId = scheduleId,
                type = OfflineChangeType.DELETE,
                sourceKey = CUSTOM_SCHEDULE_SOURCE_KEY,
            ) {
                remoteDataSource.deleteItemById(scheduleId)
            }
        } else {
            localDataSource.offline().deleteItemsById(listOf(scheduleId))
        }
    }

    override suspend fun deleteSchedulesByTimeRange(timeRange: TimeRange) {
        val isSubscriber = subscriptionChecker.getSubscriberStatus()

        return if (isSubscriber) {
            val deletableItems = localDataSource.sync().fetchSchedulesByTimeRangeEmpty(timeRange.from, timeRange.to)

            localDataSource.sync().deleteSchedulesByTimeRange(timeRange.from, timeRange.to)
            resultSyncHandler.executeOrAddToQueue(
                documentIds = deletableItems.map { it.id },
                type = OfflineChangeType.DELETE,
                sourceKey = CUSTOM_SCHEDULE_SOURCE_KEY,
            ) {
                remoteDataSource.deleteItemsByIds(deletableItems.map { it.id })
            }
        } else {
            localDataSource.offline().deleteSchedulesByTimeRange(timeRange.from, timeRange.to)
        }
    }

    override suspend fun transferData(direction: DataTransferDirection) {
        val currentUser = userSessionProvider.getCurrentUserId()
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allSchedulesFlow = remoteDataSource.fetchAllItems(currentUser)
                val schedules = allSchedulesFlow.first().map { it.convertToLocal() }
                localDataSource.offline().deleteAllItems()
                localDataSource.offline().addOrUpdateItems(schedules)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allSchedules = localDataSource.offline().fetchAllSchedules().first()
                val schedulesRemote = allSchedules.map { it.convertToRemote(currentUser) }

                remoteDataSource.deleteAllItems(currentUser)
                remoteDataSource.addOrUpdateItems(schedulesRemote)

                localDataSource.sync().deleteAllItems()
                localDataSource.sync().addOrUpdateItems(allSchedules)
            }
        }
    }
}