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

package repositories

import database.schedules.BaseScheduleLocalDataSource
import entities.classes.Class
import entities.common.NumberOfRepeatWeek
import entities.common.numberOfRepeatWeek
import entities.schedules.base.BaseSchedule
import extensions.dateTime
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import mappers.schedules.mapToData
import mappers.schedules.mapToDomain
import payments.SubscriptionChecker
import remote.schedules.BaseScheduleRemoteDataSource

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
class BaseScheduleRepositoryImpl(
    private val remoteDataSource: BaseScheduleRemoteDataSource,
    private val localDataSource: BaseScheduleLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : BaseScheduleRepository {

    override suspend fun addOrUpdateSchedule(
        schedule: BaseSchedule,
        targetUser: UID
    ): UID {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        return if (isSubscriber) {
            remoteDataSource.addOrUpdateSchedule(schedule.mapToData(), targetUser)
        } else {
            localDataSource.addOrUpdateSchedule(schedule.mapToData())
        }
    }

    override suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<BaseSchedule?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val scheduleFlow = if (isSubscriber) {
            remoteDataSource.fetchScheduleById(uid, targetUser)
        } else {
            localDataSource.fetchScheduleById(uid)
        }

        return scheduleFlow.map { scheduleData ->
            scheduleData?.mapToDomain()
        }
    }

    override suspend fun fetchScheduleByDate(
        date: Instant,
        numberOfWeek: NumberOfRepeatWeek,
        targetUser: UID
    ): Flow<BaseSchedule?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val scheduleFlow = if (isSubscriber) {
            remoteDataSource.fetchScheduleByDate(date, numberOfWeek, targetUser)
        } else {
            localDataSource.fetchScheduleByDate(date, numberOfWeek)
        }

        return scheduleFlow.map { scheduleData ->
            scheduleData?.mapToDomain()
        }
    }

    override suspend fun fetchSchedulesByVersion(
        version: TimeRange,
        numberOfWeek: NumberOfRepeatWeek?,
        targetUser: UID
    ): Flow<List<BaseSchedule>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val scheduleListFlow = if (isSubscriber) {
            remoteDataSource.fetchSchedulesByVersion(version.from, version.to, numberOfWeek, targetUser)
        } else {
            localDataSource.fetchSchedulesByVersion(version.from, version.to, numberOfWeek)
        }

        return scheduleListFlow.map { scheduleList ->
            scheduleList.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchSchedulesByTimeRange(
        timeRange: TimeRange,
        maxNumberOfWeek: NumberOfRepeatWeek,
        targetUser: UID
    ): Flow<Map<Instant, BaseSchedule?>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val scheduleListFlow = if (isSubscriber) {
            remoteDataSource.fetchSchedulesByVersion(timeRange.from, timeRange.to, null, targetUser)
        } else {
            localDataSource.fetchSchedulesByVersion(timeRange.from, timeRange.to, null)
        }

        return scheduleListFlow.map { scheduleList ->
            val schedules = scheduleList.map { it.mapToDomain() }
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

        val classFlow = if (isSubscriber) {
            remoteDataSource.fetchClassById(uid, scheduleId, targetUser)
        } else {
            localDataSource.fetchClassById(uid, scheduleId)
        }

        return classFlow.map { classData ->
            classData?.mapToDomain()
        }
    }
}