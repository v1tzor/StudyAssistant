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

import entities.classes.Class
import entities.schedules.custom.CustomSchedule
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
interface CustomScheduleRepository {
    suspend fun addOrUpdateSchedule(schedule: CustomSchedule, targetUser: UID): UID
    suspend fun fetchScheduleById(uid: UID, targetUser: UID): Flow<CustomSchedule?>
    suspend fun fetchScheduleByDate(date: Instant, targetUser: UID): Flow<CustomSchedule?>
    suspend fun fetchSchedulesByTimeRange(timeRange: TimeRange, targetUser: UID): Flow<List<CustomSchedule>>
    suspend fun fetchClassById(uid: UID, scheduleId: UID, targetUser: UID): Flow<Class?>
}