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

package ru.aleshin.studyassistant.core.data.mappers.schedules

import kotlinx.datetime.DayOfWeek
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.DateVersion
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.MediatedBaseSchedule
import ru.aleshin.studyassistant.core.remote.models.schedule.MediatedBaseSchedulePojo

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
fun MediatedBaseSchedule.mapToRemoteData() = MediatedBaseSchedulePojo(
    uid = uid,
    dateVersionFrom = dateVersion.from.toEpochMilliseconds(),
    dateVersionTo = dateVersion.to.toEpochMilliseconds(),
    weekDayOfWeek = dayOfWeek.name,
    week = week.name,
    classes = classes.map { it.mapToRemoteData() },
)

fun MediatedBaseSchedulePojo.mapToDomain() = MediatedBaseSchedule(
    uid = uid,
    dateVersion = DateVersion(
        from = dateVersionFrom.mapEpochTimeToInstant(),
        to = dateVersionTo.mapEpochTimeToInstant(),
    ),
    dayOfWeek = DayOfWeek.valueOf(weekDayOfWeek),
    week = NumberOfRepeatWeek.valueOf(week),
    classes = classes.map { it.mapToDomain() },
)
