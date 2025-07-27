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

package ru.aleshin.studyassistant.core.domain.repositories

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface BaseScheduleRepository {
    suspend fun addOrUpdateSchedule(schedule: BaseSchedule): UID
    suspend fun addOrUpdateSchedulesGroup(schedules: List<BaseSchedule>)
    suspend fun fetchScheduleById(uid: UID): Flow<BaseSchedule?>
    suspend fun fetchScheduleByDate(date: Instant, numberOfWeek: NumberOfRepeatWeek): Flow<BaseSchedule?>
    suspend fun fetchSchedulesByVersion(version: TimeRange, numberOfWeek: NumberOfRepeatWeek?): Flow<List<BaseSchedule>>
    suspend fun fetchSchedulesByTimeRange(timeRange: TimeRange, maxNumberOfWeek: NumberOfRepeatWeek): Flow<Map<Instant, BaseSchedule?>>
    suspend fun fetchClassById(uid: UID, scheduleId: UID): Flow<Class?>
    suspend fun deleteSchedulesByTimeRange(timeRange: TimeRange)
    suspend fun transferData(direction: DataTransferDirection)
}