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
import entities.schedules.BaseSchedule
import entities.settings.NumberOfWeek
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DayOfWeek
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

    override suspend fun fetchScheduleByDate(
        week: NumberOfWeek,
        weekDayOfWeek: DayOfWeek,
        targetUser: UID
    ): Flow<BaseSchedule?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()

        val scheduleFlow = if (isSubscriber) {
            remoteDataSource.fetchScheduleByDate(week.name, weekDayOfWeek.name, targetUser)
        } else {
            localDataSource.fetchScheduleByDate(week.name, weekDayOfWeek.name)
        }

        return scheduleFlow.map { scheduleData ->
            scheduleData?.mapToDomain()
        }
    }

    override suspend fun fetchSchedulesByTimeRange(
        timeRange: TimeRange,
        targetUser: UID
    ): Flow<List<BaseSchedule>> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val timeStart = timeRange.from.toEpochMilliseconds()
        val timeEnd = timeRange.to.toEpochMilliseconds()

        val scheduleListFlow = if (isSubscriber) {
            remoteDataSource.fetchSchedulesByTimeRange(timeStart.toInt(), timeEnd.toInt(), targetUser)
        } else {
            localDataSource.fetchSchedulesByTimeRange(timeStart, timeEnd)
        }

        return scheduleListFlow.map { scheduleList ->
            scheduleList.map { it.mapToDomain() }
        }
    }

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
}
