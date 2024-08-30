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
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.payments.SubscriptionChecker
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToRemoteData
import ru.aleshin.studyassistant.core.database.datasource.schedules.BaseScheduleLocalDataSource
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.remote.datasources.schedules.BaseScheduleRemoteDataSource

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
class BaseScheduleRepositoryImpl(
    private val remoteDataSource: BaseScheduleRemoteDataSource,
    private val localDataSource: BaseScheduleLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : BaseScheduleRepository {

    override suspend fun addOrUpdateSchedule(schedule: BaseSchedule, targetUser: UID): UID {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateSchedule(schedule.mapToRemoteData(), targetUser)
        } else {
            localDataSource.addOrUpdateSchedule(schedule.mapToLocalData())
        }
    }

    override suspend fun addOrUpdateSchedulesGroup(schedules: List<BaseSchedule>, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateSchedulesGroup(schedules.map { it.mapToRemoteData() }, targetUser)
        } else {
            localDataSource.addOrUpdateSchedulesGroup(schedules.map { it.mapToLocalData() })
        }
    }

    override suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<BaseSchedule?> {
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
        numberOfWeek: NumberOfRepeatWeek,
        targetUser: UID
    ): Flow<BaseSchedule?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchScheduleByDate(date, numberOfWeek, targetUser).map { schedulePojo ->
                schedulePojo?.mapToDomain()
            }
        } else {
            localDataSource.fetchScheduleByDate(date, numberOfWeek).map { scheduleEntity ->
                scheduleEntity?.mapToDomain()
            }
        }
    }

    override suspend fun fetchSchedulesByVersion(
        version: TimeRange,
        numberOfWeek: NumberOfRepeatWeek?,
        targetUser: UID
    ): Flow<List<BaseSchedule>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.fetchSchedulesByVersion(
                version.from,
                version.to,
                numberOfWeek,
                targetUser
            ).map { schedules ->
                schedules.map { baseSchedulePojo -> baseSchedulePojo.mapToDomain() }
            }
        } else {
            localDataSource.fetchSchedulesByVersion(version.from, version.to, numberOfWeek).map { schedules ->
                schedules.map { baseScheduleEntity -> baseScheduleEntity.mapToDomain() }
            }
        }
    }

    override suspend fun fetchSchedulesByTimeRange(
        timeRange: TimeRange,
        maxNumberOfWeek: NumberOfRepeatWeek,
        targetUser: UID
    ): Flow<Map<Instant, BaseSchedule?>> {
        val scheduleListFlow = fetchSchedulesByVersion(timeRange, null, targetUser)
        return scheduleListFlow.map { schedules ->
            return@map buildMap<Instant, BaseSchedule?> {
                timeRange.periodDates().forEach { targetDate ->
                    val targetWeekDay = targetDate.dateTime().dayOfWeek
                    val targetWeek = targetDate.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)
                    val schedulesByDate = schedules.find { schedule ->
                        val versionFilter = schedule.dateVersion.containsDate(targetDate)
                        val dateFilter = schedule.week == targetWeek && schedule.dayOfWeek == targetWeekDay
                        return@find versionFilter && dateFilter
                    }
                    put(targetDate, schedulesByDate)
                }
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

    override suspend fun deleteSchedulesByTimeRange(timeRange: TimeRange, targetUser: UID) {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.deleteSchedulesByTimeRange(timeRange.from, timeRange.to, targetUser)
        } else {
            localDataSource.deleteSchedulesByTimeRange(timeRange.from, timeRange.to)
        }
    }

    override suspend fun transferData(direction: DataTransferDirection, targetUser: UID) {
        when (direction) {
            DataTransferDirection.REMOTE_TO_LOCAL -> {
                val allSchedules = remoteDataSource.fetchSchedulesByVersion(
                    from = DISTANT_PAST,
                    to = DISTANT_FUTURE,
                    numberOfWeek = null,
                    targetUser = targetUser
                ).let { schedulesFlow ->
                    return@let schedulesFlow.first().map { it.mapToDomain().mapToLocalData() }
                }
                localDataSource.deleteSchedulesByTimeRange(DISTANT_PAST, DISTANT_FUTURE)
                localDataSource.addOrUpdateSchedulesGroup(allSchedules)
                remoteDataSource.deleteSchedulesByTimeRange(DISTANT_PAST, DISTANT_FUTURE, targetUser)
            }
            DataTransferDirection.LOCAL_TO_REMOTE -> {
                val allSchedules = localDataSource.fetchSchedulesByVersion(
                    from = DISTANT_PAST,
                    to = DISTANT_FUTURE,
                    numberOfWeek = null,
                ).let { schedulesFlow ->
                    return@let schedulesFlow.first().map { it.mapToDomain().mapToRemoteData() }
                }
                remoteDataSource.deleteSchedulesByTimeRange(DISTANT_PAST, DISTANT_FUTURE, targetUser)
                remoteDataSource.addOrUpdateSchedulesGroup(allSchedules, targetUser)
            }
        }
    }
}