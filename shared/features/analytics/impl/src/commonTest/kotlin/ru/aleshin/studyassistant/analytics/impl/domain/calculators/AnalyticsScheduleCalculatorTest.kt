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

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.entities.schedules.DateVersion
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.domain.entities.settings.CalendarSettings
import ru.aleshin.studyassistant.core.domain.entities.settings.Holidays
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
class AnalyticsScheduleCalculatorTest {

    private val calculator = AnalyticsScheduleCalculator.Base(TimeZone.UTC)
    private val targetDate = LocalDate(2026, 8, 3).atStartOfDayIn(TimeZone.UTC)
    private val organization = OrganizationShort(uid = "org", shortName = "School", updatedAt = 0L)

    @Test
    fun emptyCustomDayCompletelyOverridesBaseSchedule() {
        val result = calculator.calculate(
            range = TimeRange(targetDate, targetDate),
            baseSchedules = listOf(baseSchedule("base", classModel("base-class"))),
            customSchedules = listOf(CustomSchedule("custom", targetDate, emptyList(), 0L)),
            calendarSettings = settings(),
        )

        assertTrue(result.getValue(targetDate).isEmpty())
    }

    @Test
    fun newestMatchingBaseVersionWins() {
        val older = baseSchedule(
            uid = "older",
            classModel = classModel("older-class"),
            versionFrom = LocalDate(2026, 1, 1).atStartOfDayIn(TimeZone.UTC),
        )
        val newer = baseSchedule(
            uid = "newer",
            classModel = classModel("newer-class"),
            versionFrom = LocalDate(2026, 7, 1).atStartOfDayIn(TimeZone.UTC),
        )

        val result = calculator.calculate(
            range = TimeRange(targetDate, targetDate),
            baseSchedules = listOf(older, newer),
            customSchedules = emptyList(),
            calendarSettings = settings(),
        )

        assertEquals("newer-class", result.getValue(targetDate).single().uid)
    }

    @Test
    fun holidayRemovesOnlyBaseClassesForLinkedOrganization() {
        val result = calculator.calculate(
            range = TimeRange(targetDate, targetDate),
            baseSchedules = listOf(baseSchedule("base", classModel("class"))),
            customSchedules = emptyList(),
            calendarSettings = settings(
                holidays = listOf(Holidays(listOf(organization.uid), targetDate, targetDate)),
            ),
        )

        assertTrue(result.getValue(targetDate).isEmpty())
    }

    private fun baseSchedule(
        uid: String,
        classModel: Class,
        versionFrom: kotlinx.datetime.Instant = LocalDate(2026, 1, 1).atStartOfDayIn(TimeZone.UTC),
    ) = BaseSchedule(
        uid = uid,
        dateVersion = DateVersion(
            from = versionFrom,
            to = LocalDate(2026, 12, 31).atStartOfDayIn(TimeZone.UTC),
        ),
        dayOfWeek = targetDate.let { LocalDate(2026, 8, 3).dayOfWeek },
        week = NumberOfRepeatWeek.ONE,
        classes = listOf(classModel),
    )

    private fun classModel(uid: String) = Class(
        uid = uid,
        scheduleId = "schedule",
        organization = organization,
        eventType = EventType.LECTURE,
        subject = null,
        teacher = null,
        office = "",
        location = null,
        timeRange = TimeRange(targetDate, targetDate),
    )

    private fun settings(holidays: List<Holidays> = emptyList()) = CalendarSettings(
        numberOfWeek = NumberOfRepeatWeek.ONE,
        holidays = holidays,
        updatedAt = 0L,
    )
}
