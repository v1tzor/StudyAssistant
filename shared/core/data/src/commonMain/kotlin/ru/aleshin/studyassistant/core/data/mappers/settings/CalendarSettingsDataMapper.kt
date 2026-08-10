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

package ru.aleshin.studyassistant.core.data.mappers.settings

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.database.models.settings.BaseCalendarSettingsEntity
import ru.aleshin.studyassistant.core.database.models.settings.HolidaysEntity
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.settings.CalendarSettings
import ru.aleshin.studyassistant.core.domain.entities.settings.WeekScheduleViewType

/**
 * @author Stanislav Aleshin on 01.05.2024.
 */
fun CalendarSettings.mapToLocalData() = BaseCalendarSettingsEntity(
    numberOfWeek = numberOfWeek.name,
    weekScheduleViewType = weekScheduleViewType.name,
    holidays = holidays.map { it.mapToLocalData().toJson() },
    updatedAt = updatedAt,
)

fun BaseCalendarSettingsEntity.mapToDomain() = CalendarSettings(
    numberOfWeek = NumberOfRepeatWeek.valueOf(numberOfWeek),
    weekScheduleViewType = WeekScheduleViewType.valueOf(weekScheduleViewType),
    holidays = holidays?.map { it.fromJson<HolidaysEntity>().mapToDomain() } ?: emptyList(),
    updatedAt = updatedAt,
)
