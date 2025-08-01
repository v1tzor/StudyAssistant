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

package ru.aleshin.studyassistant.settings.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.settings.CalendarSettings
import ru.aleshin.studyassistant.core.domain.entities.settings.Holidays
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.CalendarSettingsUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.HolidaysUi

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
internal fun CalendarSettings.mapToUi() = CalendarSettingsUi(
    numberOfWeek = numberOfWeek,
    weekScheduleViewType = weekScheduleViewType,
    holidays = holidays.map { it.mapToUi() },
    updatedAt = updatedAt,
)

internal fun Holidays.mapToUi() = HolidaysUi(
    organizations = organizations,
    start = start,
    end = end,
)

internal fun CalendarSettingsUi.mapToDomain() = CalendarSettings(
    numberOfWeek = numberOfWeek,
    weekScheduleViewType = weekScheduleViewType,
    holidays = holidays.map { it.mapToDomain() },
    updatedAt = updatedAt,
)

internal fun HolidaysUi.mapToDomain() = Holidays(
    organizations = organizations,
    start = start,
    end = end,
)