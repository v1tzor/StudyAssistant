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

import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.settings.CalendarSettings
import ru.aleshin.studyassistant.core.remote.models.settings.CalendarSettingsPojo
import ru.aleshin.studyassistant.sqldelight.settings.CalendarSettingsEntity

/**
 * @author Stanislav Aleshin on 01.05.2024.
 */
fun CalendarSettingsPojo.mapToDomain() = CalendarSettings(
    numberOfWeek = NumberOfRepeatWeek.valueOf(numberOfWeek),
)

fun CalendarSettingsEntity.mapToDomain() = CalendarSettings(
    numberOfWeek = NumberOfRepeatWeek.valueOf(number_of_week),
)

fun CalendarSettings.mapToRemoteData() = CalendarSettingsPojo(
    numberOfWeek = numberOfWeek.name,
)

fun CalendarSettings.mapToLocalData() = CalendarSettingsEntity(
    id = 1L,
    number_of_week = numberOfWeek.name,
)