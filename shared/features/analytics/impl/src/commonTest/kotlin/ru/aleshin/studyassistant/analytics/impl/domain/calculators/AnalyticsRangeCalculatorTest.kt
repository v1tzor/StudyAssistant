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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsGranularity
import ru.aleshin.studyassistant.core.domain.entities.settings.AnalyticsPeriod
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
class AnalyticsRangeCalculatorTest {

    private val calculator = AnalyticsRangeCalculator.Base(TimeZone.UTC)

    @Test
    fun defaultRangeIsCurrentCalendarMonth() {
        val currentTime = LocalDateTime(2026, 8, 10, 14, 35).toInstant(TimeZone.UTC)

        val selection = calculator.createDefault(currentTime)

        assertEquals(AnalyticsPeriod.MONTH, selection.period)
        assertEquals(LocalDate(2026, 8, 1), selection.range.from.date())
        assertEquals(LocalDate(2026, 8, 31), selection.range.to.date())
        assertEquals(LocalDate(2026, 7, 1), selection.previousRange.from.date())
        assertEquals(LocalDate(2026, 7, 31), selection.previousRange.to.date())
        assertEquals(LocalDate(2026, 7, 10), selection.comparisonCutoff.date())
        assertEquals(14, selection.comparisonCutoff.toLocalDateTime(TimeZone.UTC).hour)
        assertEquals(35, selection.comparisonCutoff.toLocalDateTime(TimeZone.UTC).minute)
    }

    @Test
    fun leapYearFebruaryKeepsCalendarBoundary() {
        val currentTime = LocalDate(2028, 2, 15).atStartOfDayIn(TimeZone.UTC)

        val selection = calculator.selectPeriod(AnalyticsPeriod.MONTH, currentTime, currentTime)

        assertEquals(LocalDate(2028, 2, 29), selection.range.to.date())
    }

    @Test
    fun periodChangeUsesCurrentDateInsideVisibleRange() {
        val currentTime = LocalDateTime(2026, 8, 10, 14, 35).toInstant(TimeZone.UTC)
        val month = calculator.createDefault(currentTime)

        val week = calculator.changePeriod(AnalyticsPeriod.WEEK, month, currentTime)

        assertEquals(LocalDate(2026, 8, 10), week.range.from.date())
        assertEquals(LocalDate(2026, 8, 16), week.range.to.date())
    }

    @Test
    fun periodChangeUsesVisibleRangeCenterOutsideCurrentPeriod() {
        val currentTime = LocalDateTime(2026, 8, 10, 14, 35).toInstant(TimeZone.UTC)
        val historicalMonth = calculator.selectPeriod(
            AnalyticsPeriod.MONTH,
            LocalDate(2026, 5, 1).atStartOfDayIn(TimeZone.UTC),
            currentTime,
        )

        val week = calculator.changePeriod(AnalyticsPeriod.WEEK, historicalMonth, currentTime)

        assertEquals(LocalDate(2026, 5, 11), week.range.from.date())
        assertEquals(LocalDate(2026, 5, 17), week.range.to.date())
    }

    @Test
    fun customRangeUsesInclusiveBoundariesAndEqualPreviousDuration() {
        val from = LocalDate(2026, 3, 10).atStartOfDayIn(TimeZone.UTC)
        val to = LocalDate(2026, 3, 14).atStartOfDayIn(TimeZone.UTC)

        val selection = calculator.selectCustom(from, to, to)

        assertEquals(LocalDate(2026, 3, 10), selection.range.from.date())
        assertEquals(LocalDate(2026, 3, 14), selection.range.to.date())
        assertEquals(LocalDate(2026, 3, 5), selection.previousRange.from.date())
        assertEquals(LocalDate(2026, 3, 9), selection.previousRange.to.date())
    }

    @Test
    fun customGranularityChangesAtDeclaredThresholds() {
        val from = LocalDate(2026, 1, 1)

        val daily = calculator.selectCustom(
            from.atStartOfDayIn(TimeZone.UTC),
            from.plus(44, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.UTC),
            from.atStartOfDayIn(TimeZone.UTC),
        )
        val weekly = calculator.selectCustom(
            from.atStartOfDayIn(TimeZone.UTC),
            from.plus(45, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.UTC),
            from.atStartOfDayIn(TimeZone.UTC),
        )
        val monthly = calculator.selectCustom(
            from.atStartOfDayIn(TimeZone.UTC),
            from.plus(180, DateTimeUnit.DAY).atStartOfDayIn(TimeZone.UTC),
            from.atStartOfDayIn(TimeZone.UTC),
        )

        assertEquals(AnalyticsGranularity.DAY, daily.granularity)
        assertEquals(AnalyticsGranularity.WEEK, weekly.granularity)
        assertEquals(AnalyticsGranularity.MONTH, monthly.granularity)
    }

    @Test
    fun dayBoundaryUsesCalendarDayAcrossDstTransition() {
        val newYork = TimeZone.of("America/New_York")
        val dstCalculator = AnalyticsRangeCalculator.Base(newYork)
        val date = LocalDate(2026, 3, 8)

        val selection = dstCalculator.selectCustom(
            date.atStartOfDayIn(newYork),
            date.atStartOfDayIn(newYork),
            date.atStartOfDayIn(newYork),
        )

        val duration = selection.range.to.toEpochMilliseconds() -
            selection.range.from.toEpochMilliseconds() + 1L
        assertEquals(23L * 60L * 60L * 1_000L, duration)
    }

    private fun kotlinx.datetime.Instant.date() = toLocalDateTime(TimeZone.UTC).date
}
