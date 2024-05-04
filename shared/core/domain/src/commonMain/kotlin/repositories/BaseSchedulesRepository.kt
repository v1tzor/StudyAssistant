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

import entities.schedules.BaseSchedule
import entities.settings.NumberOfWeek
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.DayOfWeek

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface BaseSchedulesRepository {
    suspend fun fetchScheduleByDate(week: NumberOfWeek, weekDayOfWeek: DayOfWeek, targetUser: UID): Flow<BaseSchedule?>
    suspend fun fetchSchedulesByTimeRange(timeRange: TimeRange, targetUser: UID): Flow<List<BaseSchedule>>
    suspend fun addOrUpdateSchedule(schedule: BaseSchedule, targetUser: UID): UID
}