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

package ru.aleshin.studyassistant.editor.impl.presentation.mappers

import entities.schedules.BaseSchedule
import entities.schedules.DateVersion
import ru.aleshin.studyassistant.editor.impl.domain.entities.BaseWeekSchedule
import ru.aleshin.studyassistant.editor.impl.presentation.models.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.BaseWeekScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.DateVersionUi

/**
 * @author Stanislav Aleshin on 30.05.2024.
 */
internal fun BaseWeekSchedule.mapToUi() = BaseWeekScheduleUi(
    from = from,
    to = to,
    numberOfWeek = numberOfWeek,
    weekDaySchedules = weekDaySchedules.mapValues { it.value?.mapToUi() },
)

internal fun BaseSchedule.mapToUi() = BaseScheduleUi(
    uid = uid,
    dateVersion = dateVersion.mapToUi(),
    dayOfWeek = dayOfWeek,
    week = week,
    classes = classes.map { it.mapToUi() }
)

internal fun DateVersion.mapToUi() = DateVersionUi(
    from = from,
    to = to,
)

internal fun BaseWeekScheduleUi.mapToDomain() = BaseWeekSchedule(
    from = from,
    to = to,
    numberOfWeek = numberOfWeek,
    weekDaySchedules = weekDaySchedules.mapValues { it.value?.mapToDomain() },
)

internal fun BaseScheduleUi.mapToDomain() = BaseSchedule(
    uid = uid,
    dateVersion = dateVersion.mapToDomain(),
    dayOfWeek = dayOfWeek,
    week = week,
    classes = classes.map { it.mapToDomain() },
)

internal fun DateVersionUi.mapToDomain() = DateVersion(
    from = from,
    to = to,
)
