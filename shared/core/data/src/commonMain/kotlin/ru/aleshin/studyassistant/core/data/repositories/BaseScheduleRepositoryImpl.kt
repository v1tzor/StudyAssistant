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
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.schedules.mapToLocalData
import ru.aleshin.studyassistant.core.database.datasource.schedules.BaseScheduleLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
class BaseScheduleRepositoryImpl(
    private val localDataSource: BaseScheduleLocalDataSource,
) : BaseScheduleRepository {

    override suspend fun addOrUpdateSchedule(schedule: BaseSchedule): UID {
        val updatedSchedule = schedule.copy(uid = schedule.uid.ifBlank { randomUUID() })
        localDataSource.addOrUpdateSchedule(updatedSchedule.mapToLocalData())
        return updatedSchedule.uid
    }

    override suspend fun addOrUpdateSchedulesGroup(schedules: List<BaseSchedule>) {
        val updatedSchedules = schedules.map { schedule ->
            schedule.copy(uid = schedule.uid.ifBlank { randomUUID() })
        }
        localDataSource.addOrUpdateSchedules(updatedSchedules.map { it.mapToLocalData() })
    }

    override suspend fun fetchScheduleById(uid: UID): Flow<BaseSchedule?> {
        return localDataSource.fetchScheduleDetailsById(uid).map { schedule -> schedule?.mapToDomain() }
    }

    override suspend fun fetchScheduleByDate(
        date: Instant,
        numberOfWeek: NumberOfRepeatWeek,
    ): Flow<BaseSchedule?> {
        return localDataSource.fetchScheduleDetailsByDate(date, numberOfWeek).map { schedule ->
            schedule?.mapToDomain()
        }
    }

    override suspend fun fetchSchedulesByVersion(
        version: TimeRange,
        numberOfWeek: NumberOfRepeatWeek?,
    ): Flow<List<BaseSchedule>> {
        return localDataSource.fetchSchedulesByVersion(
            from = version.from,
            to = version.to,
            numberOfWeek = numberOfWeek,
        ).map { schedules -> schedules.map { it.mapToDomain() } }
    }

    override suspend fun fetchClassById(uid: UID, scheduleId: UID): Flow<Class?> {
        return localDataSource.fetchClassById(uid, scheduleId).map { classEntity ->
            classEntity?.mapToDomain()
        }
    }

    override suspend fun deleteSchedulesByTimeRange(timeRange: TimeRange) {
        localDataSource.deleteSchedulesByTimeRange(timeRange.from, timeRange.to)
    }
}
