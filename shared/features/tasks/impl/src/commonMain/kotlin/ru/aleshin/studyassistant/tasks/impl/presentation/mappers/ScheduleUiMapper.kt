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

package ru.aleshin.studyassistant.tasks.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.schedules.DateVersion
import ru.aleshin.studyassistant.core.domain.entities.schedules.Schedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.CustomScheduleUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.DateVersionUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.ScheduleUi

/**
 * @author Stanislav Aleshin on 29.06.2024.
 */
internal fun BaseSchedule.mapToUi() = BaseScheduleUi(
    uid = uid,
    dateVersion = dateVersion.mapToUi(),
    dayOfWeek = dayOfWeek,
    week = week,
    classes = classes.map { it.mapToUi() },
    updatedAt = updatedAt,
)

internal fun CustomSchedule.mapToUi() = CustomScheduleUi(
    uid = uid,
    date = date,
    classes = classes.map { it.mapToUi() },
    updatedAt = updatedAt,
)

internal fun Schedule.mapToUi() = when (this) {
    is Schedule.Base -> ScheduleUi.Base(data?.mapToUi())
    is Schedule.Custom -> ScheduleUi.Custom(data?.mapToUi())
}

internal fun DateVersion.mapToUi() = DateVersionUi(
    from = from,
    to = to,
)