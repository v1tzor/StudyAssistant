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
import ru.aleshin.studyassistant.core.common.payments.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToRemoteData
import ru.aleshin.studyassistant.core.database.datasource.schedules.CustomScheduleLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.remote.datasources.schedules.CustomScheduleRemoteDataSource

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
class CustomScheduleRepositoryImpl(
    private val remoteDataSource: CustomScheduleRemoteDataSource,
    private val localDataSource: CustomScheduleLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : CustomScheduleRepository {

    override suspend fun addOrUpdateSchedule(
        schedule: CustomSchedule,
        targetUser: UID
    ): UID {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateSchedule(schedule.mapToRemoteData(), targetUser)
        } else {
            localDataSource.addOrUpdateSchedule(schedule.mapToLocalData())
        }
    }

    override suspend fun fetchScheduleById(
        uid: UID,
        targetUser: UID
    ): Flow<CustomSchedule?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchScheduleById(uid, targetUser).map { schedulePojo ->
                schedulePojo?.mapToDomain()
            }
        } else {
            localDataSource.fetchScheduleById(uid).map { scheduleEntity ->
                scheduleEntity?.mapToDomain()
            }
        }
    }

    override suspend fun fetchScheduleByDate(
        date: Instant,
        targetUser: UID
    ): Flow<CustomSchedule?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchScheduleByDate(date, targetUser).map { schedulePojo ->
                schedulePojo?.mapToDomain()
            }
        } else {
            localDataSource.fetchScheduleByDate(date).map { scheduleEntity ->
                scheduleEntity?.mapToDomain()
            }
        }
    }

    override suspend fun fetchSchedulesByTimeRange(
        timeRange: TimeRange,
        targetUser: UID
    ): Flow<List<CustomSchedule>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchSchedulesByTimeRange(timeRange.from, timeRange.to, targetUser).map { schedules ->
                schedules.map { schedulePojo -> schedulePojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchSchedulesByTimeRange(timeRange.from, timeRange.to).map { schedules ->
                schedules.map { scheduleEntity -> scheduleEntity.mapToDomain() }
            }
        }
    }

    override suspend fun fetchClassById(uid: UID, scheduleId: UID, targetUser: UID): Flow<Class?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchClassById(uid, scheduleId, targetUser).map { classPojo -> classPojo?.mapToDomain() }
        } else {
            localDataSource.fetchClassById(uid, scheduleId).map { classEntity -> classEntity?.mapToDomain() }
        }
    }

    override suspend fun deleteScheduleById(scheduleId: UID, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        if (isSubscriber) {
            remoteDataSource.deleteScheduleById(scheduleId, targetUser)
        } else {
            localDataSource.deleteScheduleById(scheduleId)
        }
    }

    override suspend fun deleteSchedulesByTimeRange(timeRange: TimeRange, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        if (isSubscriber) {
            remoteDataSource.deleteSchedulesByTimeRange(timeRange.from, timeRange.to, targetUser)
        } else {
            localDataSource.deleteSchedulesByTimeRange(timeRange.from, timeRange.to)
        }
    }

    override suspend fun transferData(direction: DataTransferDirection, targetUser: UID) {
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allSchedules = remoteDataSource.fetchSchedulesByTimeRange(
                    from = DISTANT_PAST,
                    to = DISTANT_FUTURE,
                    targetUser = targetUser
                ).let { schedulesFlow ->
                    return@let schedulesFlow.first().map { it.mapToDomain().mapToLocalData() }
                }
                localDataSource.deleteSchedulesByTimeRange(DISTANT_PAST, DISTANT_FUTURE)
                localDataSource.addOrUpdateSchedulesGroup(allSchedules)
                remoteDataSource.deleteSchedulesByTimeRange(DISTANT_PAST, DISTANT_FUTURE, targetUser)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allSchedules = localDataSource.fetchSchedulesByTimeRange(
                    from = DISTANT_PAST,
                    to = DISTANT_FUTURE,
                ).let { schedulesFlow ->
                    return@let schedulesFlow.first().map { it.mapToDomain().mapToRemoteData() }
                }
                remoteDataSource.deleteSchedulesByTimeRange(DISTANT_PAST, DISTANT_FUTURE, targetUser)
                remoteDataSource.addOrUpdateSchedulesGroup(allSchedules, targetUser)
            }
        }
    }
}