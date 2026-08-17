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

package ru.aleshin.studyassistant.analytics.impl.domain.common

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import ru.aleshin.studyassistant.analytics.impl.domain.calculators.AnalyticsRangeCalculator
import ru.aleshin.studyassistant.core.domain.entities.settings.AnalyticsPeriod
import ru.aleshin.studyassistant.core.domain.entities.settings.AnalyticsSettings
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
class AnalyticsSettingsSelectionTest {

    private val calculator = AnalyticsRangeCalculator.Base(TimeZone.UTC)
    private val currentTime = Instant.parse("2026-03-15T12:00:00Z")

    @Test
    fun defaultSettingsUseCurrentMonth() {
        val selection = AnalyticsSettings().toRangeSelection(calculator, currentTime)

        assertEquals(AnalyticsPeriod.MONTH, selection.period)
        val expected = calculator.selectPeriod(AnalyticsPeriod.MONTH, currentTime, currentTime)
        assertEquals(expected.range, selection.range)
    }

    @Test
    fun savedWeekUsesCurrentWeek() {
        val selection = AnalyticsSettings(period = AnalyticsPeriod.WEEK)
            .toRangeSelection(calculator, currentTime)

        assertEquals(AnalyticsPeriod.WEEK, selection.period)
        val expected = calculator.selectPeriod(AnalyticsPeriod.WEEK, currentTime, currentTime)
        assertEquals(expected.range, selection.range)
    }

    @Test
    fun savedCustomRangeKeepsBoundaries() {
        val from = Instant.parse("2026-01-01T00:00:00Z")
        val to = Instant.parse("2026-01-10T00:00:00Z")
        val selection = AnalyticsSettings(
            period = AnalyticsPeriod.CUSTOM,
            customFrom = from.toEpochMilliseconds(),
            customTo = to.toEpochMilliseconds(),
        ).toRangeSelection(calculator, currentTime)

        val expected = calculator.selectCustom(from, to, currentTime)
        assertEquals(AnalyticsPeriod.CUSTOM, selection.period)
        assertEquals(expected.range, selection.range)
    }

    @Test
    fun brokenCustomFallsBackToMonth() {
        val selection = AnalyticsSettings(
            period = AnalyticsPeriod.CUSTOM,
            customFrom = 20L,
            customTo = 10L,
        ).toRangeSelection(calculator, currentTime)

        assertEquals(AnalyticsPeriod.MONTH, selection.period)
    }
}
