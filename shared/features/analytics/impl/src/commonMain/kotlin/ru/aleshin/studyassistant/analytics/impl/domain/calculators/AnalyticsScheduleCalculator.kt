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

package ru.aleshin.studyassistant.analytics.impl.domain.calculators

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.entities.settings.CalendarSettings

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal interface AnalyticsScheduleCalculator {

    fun calculate(
        range: TimeRange,
        baseSchedules: List<BaseSchedule>,
        customSchedules: List<CustomSchedule>,
        calendarSettings: CalendarSettings,
    ): Map<Instant, List<Class>>

    class Base(
        private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ) : AnalyticsScheduleCalculator {

        override fun calculate(
            range: TimeRange,
            baseSchedules: List<BaseSchedule>,
            customSchedules: List<CustomSchedule>,
            calendarSettings: CalendarSettings,
        ): Map<Instant, List<Class>> {
            val customSchedulesByDate = customSchedules.associateBy {
                it.date.startThisDay(timeZone)
            }
            val startDate = range.from.toLocalDateTime(timeZone).date
            val endDate = range.to.toLocalDateTime(timeZone).date
            return buildMap {
                var date = startDate
                while (date <= endDate) {
                    val dateInstant = date.atStartOfDayIn(timeZone)
                    val customSchedule = customSchedulesByDate[dateInstant]
                    val classes = if (customSchedule != null) {
                        customSchedule.classes
                    } else {
                        val numberOfWeek = date.numberOfRepeatWeek(calendarSettings.numberOfWeek)
                        val baseSchedule = baseSchedules
                            .asSequence()
                            .filter { schedule -> schedule.dayOfWeek == date.dayOfWeek }
                            .filter { schedule -> schedule.week == numberOfWeek }
                            .filter { schedule -> schedule.dateVersion.containsDate(dateInstant) }
                            .maxByOrNull { schedule -> schedule.dateVersion.from }
                        baseSchedule?.classes.orEmpty().filter { classModel ->
                            calendarSettings.holidays.none { holiday ->
                                val isDateInHoliday = dateInstant in holiday.start..holiday.end
                                isDateInHoliday && classModel.organization.uid in holiday.organizations
                            }
                        }
                    }
                    put(dateInstant, classes.sortedBy { it.timeRange.from })
                    date = date.plus(1, DateTimeUnit.DAY)
                }
            }
        }
    }
}
