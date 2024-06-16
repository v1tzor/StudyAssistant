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

package ru.aleshin.studyassistant.schedule.impl.presentation.mappers

import entities.schedules.BaseScheduleDetails
import entities.schedules.CustomScheduleDetails
import entities.schedules.DateVersion
import entities.schedules.ScheduleDetails
import ru.aleshin.studyassistant.schedule.impl.domain.entities.WeekScheduleDetails
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.BaseScheduleDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.CustomScheduleDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.DateVersionUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.ScheduleDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.WeekScheduleDetailsUi

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal fun BaseScheduleDetails.mapToUi(): BaseScheduleDetailsUi {
    val groupedClasses = classes.groupBy { it.organization.uid }
    return BaseScheduleDetailsUi(
        uid = uid,
        dateVersion = dateVersion.mapToUi(),
        dayOfWeek = dayOfWeek,
        week = week,
        classes = classes.map {
            val number = groupedClasses[it.organization.uid]?.indexOf(it)?.inc() ?: 0
            it.mapToUi(number)
        },
    )
}

internal fun CustomScheduleDetails.mapToUi(): CustomScheduleDetailsUi {
    val groupedClasses = classes.groupBy { it.organization.uid }
    return CustomScheduleDetailsUi(
        uid = uid,
        date = date,
        classes = classes.map {
            val number = groupedClasses[it.organization.uid]?.indexOf(it)?.inc() ?: 0
            it.mapToUi(number)
        },
    )
}

internal fun ScheduleDetails.mapToUi() = when (this) {
    is ScheduleDetails.Base -> ScheduleDetailsUi.Base(data?.mapToUi())
    is ScheduleDetails.Custom -> ScheduleDetailsUi.Custom(data?.mapToUi())
}

internal fun WeekScheduleDetails.mapToUi() = WeekScheduleDetailsUi(
    from = from,
    to = to,
    numberOfWeek = numberOfWeek,
    weekDaySchedules = weekDaySchedules.mapValues { it.value?.mapToUi() },
)

internal fun DateVersion.mapToUi() = DateVersionUi(
    from = from,
    to = to,
)