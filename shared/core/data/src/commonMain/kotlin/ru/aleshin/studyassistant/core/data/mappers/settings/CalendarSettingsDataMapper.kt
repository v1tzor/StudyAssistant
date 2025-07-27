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

package ru.aleshin.studyassistant.core.data.mappers.settings

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.data.utils.sync.SingleSyncMapper
import ru.aleshin.studyassistant.core.database.models.settings.BaseCalendarSettingsEntity
import ru.aleshin.studyassistant.core.database.models.settings.HolidaysEntity
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.settings.CalendarSettings
import ru.aleshin.studyassistant.core.domain.entities.settings.Holidays
import ru.aleshin.studyassistant.core.domain.entities.settings.WeekScheduleViewType
import ru.aleshin.studyassistant.core.remote.models.settings.CalendarSettingsPojo
import ru.aleshin.studyassistant.core.remote.models.settings.HolidaysPojo

/**
 * @author Stanislav Aleshin on 01.05.2024.
 */

// Remote

fun CalendarSettings.mapToRemoteData() = CalendarSettingsPojo(
    numberOfWeek = numberOfWeek.name,
    weekScheduleViewType = weekScheduleViewType.name,
    holidays = holidays.map { it.mapToRemoteData().toJson() },
    updatedAt = updatedAt,
)

fun Holidays.mapToRemoteData() = HolidaysPojo(
    organizations = organizations,
    start = start.toEpochMilliseconds(),
    end = end.toEpochMilliseconds(),
)

fun CalendarSettingsPojo.mapToDomain() = CalendarSettings(
    numberOfWeek = NumberOfRepeatWeek.valueOf(numberOfWeek),
    weekScheduleViewType = WeekScheduleViewType.valueOf(weekScheduleViewType),
    holidays = holidays.map { it.fromJson<HolidaysPojo>().mapToDomain() },
    updatedAt = updatedAt,
)

fun HolidaysPojo.mapToDomain() = Holidays(
    organizations = organizations,
    start = start.mapEpochTimeToInstant(),
    end = end.mapEpochTimeToInstant(),
)

// Local

fun CalendarSettings.mapToLocalData() = BaseCalendarSettingsEntity(
    numberOfWeek = numberOfWeek.name,
    weekScheduleViewType = weekScheduleViewType.name,
    holidays = holidays.map { it.mapToLocalData().toJson() },
    updatedAt = updatedAt,
    isCacheData = 0L,
)

fun Holidays.mapToLocalData() = HolidaysEntity(
    organizations = organizations,
    start = start.toEpochMilliseconds(),
    end = end.toEpochMilliseconds(),
)

fun BaseCalendarSettingsEntity.mapToDomain() = CalendarSettings(
    numberOfWeek = NumberOfRepeatWeek.valueOf(numberOfWeek),
    weekScheduleViewType = WeekScheduleViewType.valueOf(weekScheduleViewType),
    holidays = holidays?.map { it.fromJson<HolidaysEntity>().mapToDomain() } ?: emptyList(),
    updatedAt = updatedAt,
)

fun HolidaysEntity.mapToDomain() = Holidays(
    organizations = organizations,
    start = start.mapEpochTimeToInstant(),
    end = end.mapEpochTimeToInstant(),
)

// Combined

fun BaseCalendarSettingsEntity.convertToRemote() = CalendarSettingsPojo(
    id = uid,
    numberOfWeek = numberOfWeek,
    weekScheduleViewType = weekScheduleViewType,
    holidays = holidays?.map { it.fromJson<HolidaysEntity>().mapToRemote().toJson() } ?: emptyList(),
    updatedAt = updatedAt,
)

fun CalendarSettingsPojo.convertToLocal() = BaseCalendarSettingsEntity(
    uid = id,
    numberOfWeek = numberOfWeek,
    weekScheduleViewType = weekScheduleViewType,
    holidays = holidays.map { it.fromJson<HolidaysPojo>().mapToLocal().toJson() },
    updatedAt = updatedAt,
    isCacheData = 1L,
)

fun HolidaysPojo.mapToLocal() = HolidaysEntity(
    organizations = organizations,
    start = start,
    end = end,
)

fun HolidaysEntity.mapToRemote() = HolidaysPojo(
    organizations = organizations,
    start = start,
    end = end,
)

class CalendarSettingsSyncMapper : SingleSyncMapper<BaseCalendarSettingsEntity, CalendarSettingsPojo>(
    localToRemote = { convertToRemote() },
    remoteToLocal = { convertToLocal() },
)