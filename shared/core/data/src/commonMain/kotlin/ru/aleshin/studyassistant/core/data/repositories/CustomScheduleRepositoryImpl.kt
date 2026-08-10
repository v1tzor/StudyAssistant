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
import ru.aleshin.studyassistant.core.database.datasource.schedules.CustomScheduleLocalDataSource
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
class CustomScheduleRepositoryImpl(
    private val localDataSource: CustomScheduleLocalDataSource,
) : CustomScheduleRepository {

    override suspend fun addOrUpdateSchedule(schedule: CustomSchedule): UID {
        val updatedSchedule = schedule.copy(uid = schedule.uid.ifBlank { randomUUID() })
        localDataSource.addOrUpdateSchedule(updatedSchedule.mapToLocalData())
        return updatedSchedule.uid
    }

    override suspend fun fetchScheduleById(uid: UID): Flow<CustomSchedule?> {
        return localDataSource.fetchScheduleDetailsById(uid).map { schedule -> schedule?.mapToDomain() }
    }

    override suspend fun fetchScheduleByDate(date: Instant): Flow<CustomSchedule?> {
        return localDataSource.fetchScheduleDetailsByDate(date).map { schedule -> schedule?.mapToDomain() }
    }

    override suspend fun fetchSchedulesByTimeRange(timeRange: TimeRange): Flow<List<CustomSchedule>> {
        return localDataSource.fetchSchedulesDetailsByTimeRange(timeRange.from, timeRange.to).map { schedules ->
            schedules.map { it.mapToDomain() }
        }
    }

    override suspend fun fetchClassById(uid: UID, scheduleId: UID): Flow<Class?> {
        return localDataSource.fetchClassById(uid, scheduleId).map { classEntity ->
            classEntity?.mapToDomain()
        }
    }

    override suspend fun deleteScheduleById(scheduleId: UID) {
        localDataSource.deleteSchedulesByIds(listOf(scheduleId))
    }

    override suspend fun deleteSchedulesByTimeRange(timeRange: TimeRange) {
        localDataSource.deleteSchedulesByTimeRange(timeRange.from, timeRange.to)
    }
}
