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

package ru.aleshin.studyassistant.core.domain.entities.schedules.base

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.dateTimeByWeek
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.Constants.Date.DAYS_IN_WEEK
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
fun List<BaseSchedule>.associateWithDates(
    timeRange: TimeRange,
    maxNumberOfWeek: NumberOfRepeatWeek,
): Map<Instant, BaseSchedule> = buildMap {
    this@associateWithDates.forEach { schedule ->
        val firstDate = schedule.dayOfWeek.dateTimeByWeek(schedule.dateVersion.from)
        val firstWeek = firstDate.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)
        val targetDate = firstDate.shiftWeek(
            schedule.week.isoRepeatWeekNumber - firstWeek.isoRepeatWeekNumber,
        )
        val scheduleEnd = minOf(timeRange.to, schedule.dateVersion.to)
        val untilEnd = targetDate.daysUntil(scheduleEnd, TimeZone.currentSystemDefault())
        if (untilEnd >= 0) {
            val repeats = untilEnd / (maxNumberOfWeek.isoRepeatWeekNumber * DAYS_IN_WEEK)
            for (index in 0..repeats) {
                val date = targetDate.shiftWeek(index * maxNumberOfWeek.isoRepeatWeekNumber)
                if (timeRange.containsDate(date)) put(date.startThisDay(), schedule)
            }
        }
    }
}
