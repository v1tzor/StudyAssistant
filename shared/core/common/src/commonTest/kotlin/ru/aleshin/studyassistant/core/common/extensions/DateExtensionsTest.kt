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

package ru.aleshin.studyassistant.core.common.extensions

import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Instant

/**
 * @author Stanislav Aleshin on 18.08.2026.
 */
class DateExtensionsTest {

    @Test
    fun equalsDay_sameCalendarDay_isTrue() {
        val first = LocalDate(2026, 8, 18)
        val second = LocalDate(2026, 8, 18)

        assertTrue(first.equalsDay(second))
    }

    @Test
    fun equalsDay_sameDayOfYearDifferentYears_isFalse() {
        val first = LocalDate(2025, 1, 1)
        val second = LocalDate(2026, 1, 1)

        assertFalse(first.equalsDay(second))
    }

    @Test
    fun endThisDay_coversLastMillisecondOfLocalDay() {
        val zone = TimeZone.of("UTC")
        val noon = LocalDateTime(2026, 8, 18, 12, 0).toInstant(zone)
        val endOfDay = noon.endThisDay(zone)
        val nextMidnight = LocalDateTime(2026, 8, 19, 0, 0).toInstant(zone)

        assertTrue(endOfDay < nextMidnight)
        assertEquals(999_999_999, endOfDay.dateTime(zone).nanosecond)
        assertEquals(23, endOfDay.dateTime(zone).hour)
        assertEquals(59, endOfDay.dateTime(zone).minute)
        assertEquals(59, endOfDay.dateTime(zone).second)
    }

    @Test
    fun utcEpochDateToLocalStartOfDay_keepsSelectedCalendarDayInUtcMinusZone() {
        val selectedUtcMidnight = Instant.parse("2026-08-18T00:00:00Z")
        val newYork = TimeZone.of("America/New_York")

        val localStart = selectedUtcMidnight.toEpochMilliseconds().utcEpochDateToLocalStartOfDay(newYork)

        assertEquals(LocalDate(2026, 8, 18), localStart.dateTime(newYork).date)
        assertEquals(0, localStart.dateTime(newYork).hour)
        assertEquals(0, localStart.dateTime(newYork).minute)
    }
}
