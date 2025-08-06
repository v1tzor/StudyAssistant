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

package ru.aleshin.studyassistant.core.database.mappers.settings

import ru.aleshin.studyassistant.core.database.models.settings.BaseCalendarSettingsEntity
import ru.aleshin.studyassistant.sqldelight.settings.CalendarSettingsEntity

/**
 * @author Stanislav Aleshin on 25.07.2025.
 */
fun CalendarSettingsEntity.mapToBase() = BaseCalendarSettingsEntity(
    uid = document_id.toString(),
    numberOfWeek = number_of_week,
    weekScheduleViewType = week_schedule_view_type,
    holidays = holidays,
    updatedAt = updated_at,
    isCacheData = is_cache_data,
)

fun BaseCalendarSettingsEntity.mapToEntity(id: Long) = CalendarSettingsEntity(
    id = id,
    document_id = uid,
    number_of_week = numberOfWeek,
    week_schedule_view_type = weekScheduleViewType,
    holidays = holidays,
    updated_at = updatedAt,
    is_cache_data = isCacheData,
)