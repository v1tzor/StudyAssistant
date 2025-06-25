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

package ru.aleshin.studyassistant.chat.impl.presentation.mappers

import ru.aleshin.studyassistant.chat.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.schedules.BaseWeekScheduleUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.schedules.CustomScheduleUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.schedules.DateVersionUi
import ru.aleshin.studyassistant.core.domain.entities.schedules.DateVersion
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseWeekSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule

/**
 * @author Stanislav Aleshin on 30.05.2024.
 */
internal fun BaseWeekSchedule.mapToUi() = BaseWeekScheduleUi(
    from = from,
    to = to,
    numberOfWeek = numberOfWeek,
    weekDaySchedules = weekDaySchedules.mapValues { it.value.mapToUi() },
)

internal fun BaseSchedule.mapToUi(): BaseScheduleUi {
    val groupedClasses = classes.groupBy { it.organization.uid }
    return BaseScheduleUi(
        uid = uid,
        dateVersion = dateVersion.mapToUi(),
        dayOfWeek = dayOfWeek,
        week = week,
        classes = classes.map {
            val number = groupedClasses[it.organization.uid]?.indexOf(it)?.inc() ?: 0
            it.mapToUi(number)
        }
    )
}

internal fun CustomSchedule.mapToUi(): CustomScheduleUi {
    val groupedClasses = classes.groupBy { it.organization.uid }
    return CustomScheduleUi(
        uid = uid,
        date = date,
        classes = classes.map {
            val number = groupedClasses[it.organization.uid]?.indexOf(it)?.inc() ?: 0
            it.mapToUi(number)
        }
    )
}

internal fun DateVersion.mapToUi() = DateVersionUi(
    from = from,
    to = to,
)

internal fun BaseWeekScheduleUi.mapToDomain() = BaseWeekSchedule(
    from = from,
    to = to,
    numberOfWeek = numberOfWeek,
    weekDaySchedules = weekDaySchedules.mapValues { it.value.mapToDomain() },
)

internal fun BaseScheduleUi.mapToDomain() = BaseSchedule(
    uid = uid,
    dateVersion = dateVersion.mapToDomain(),
    dayOfWeek = dayOfWeek,
    week = week,
    classes = classes.map { it.mapToDomain() },
)

internal fun CustomScheduleUi.mapToDomain() = CustomSchedule(
    uid = uid,
    date = date,
    classes = classes.map { it.mapToDomain() },
)

internal fun DateVersionUi.mapToDomain() = DateVersion(
    from = from,
    to = to,
)