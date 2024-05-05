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

import database.schedules.CustomScheduleLocalDataSource
import entities.schedules.CustomSchedule
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import mappers.schedules.mapToData
import mappers.schedules.mapToDomain
import payments.SubscriptionChecker
import remote.schedules.CustomScheduleRemoteDataSource

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
class CustomScheduleRepositoryImpl(
    private val remoteDataSource: CustomScheduleRemoteDataSource,
    private val localDataSource: CustomScheduleLocalDataSource,
    private val subscriptionChecker: SubscriptionChecker,
) : CustomScheduleRepository {

    override suspend fun fetchScheduleByDate(
        date: Instant,
        targetUser: UID
    ): Flow<CustomSchedule?> {
        val isSubscriber = subscriptionChecker.checkSubscriptionActivity()
        val dateMillis = date.toEpochMilliseconds()

        val scheduleFlow = if (isSubscriber) {
            remoteDataSource.fetchScheduleByDate(dateMillis.toInt(), targetUser)
        } else {
            localDataSource.fetchScheduleByDate(dateMillis)
        }

        return scheduleFlow.map { scheduleData ->
            scheduleData?.mapToDomain()
        }
    }

    override suspend fun fetchSchedulesByTimeRange(
        timeRange: TimeRange,
        targetUser: UID
    ): Flow<List<CustomSchedule>> {
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
        schedule: CustomSchedule,
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
