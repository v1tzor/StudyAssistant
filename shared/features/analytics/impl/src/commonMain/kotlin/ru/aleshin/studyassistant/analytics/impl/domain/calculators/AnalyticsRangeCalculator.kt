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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsGranularity
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsPeriod
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsRangeSelection
import ru.aleshin.studyassistant.core.common.functional.TimeRange

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal interface AnalyticsRangeCalculator {

    fun createDefault(currentTime: Instant): AnalyticsRangeSelection
    fun selectPeriod(
        period: AnalyticsPeriod,
        anchor: Instant,
        currentTime: Instant,
    ): AnalyticsRangeSelection

    fun changePeriod(
        period: AnalyticsPeriod,
        selection: AnalyticsRangeSelection,
        currentTime: Instant,
    ): AnalyticsRangeSelection

    fun selectCustom(
        from: Instant,
        to: Instant,
        currentTime: Instant,
    ): AnalyticsRangeSelection

    fun shift(
        selection: AnalyticsRangeSelection,
        amount: Int,
        currentTime: Instant,
    ): AnalyticsRangeSelection

    class Base(
        private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ) : AnalyticsRangeCalculator {

        override fun createDefault(currentTime: Instant): AnalyticsRangeSelection {
            return selectPeriod(AnalyticsPeriod.MONTH, currentTime, currentTime)
        }

        override fun selectPeriod(
            period: AnalyticsPeriod,
            anchor: Instant,
            currentTime: Instant,
        ): AnalyticsRangeSelection {
            require(period != AnalyticsPeriod.CUSTOM)
            val anchorDate = anchor.toLocalDateTime(timeZone).date
            val rangeDates = when (period) {
                AnalyticsPeriod.WEEK -> {
                    val from = anchorDate.plus(-anchorDate.dayOfWeek.ordinal, DateTimeUnit.DAY)
                    from to from.plus(DAYS_IN_WEEK - 1, DateTimeUnit.DAY)
                }
                AnalyticsPeriod.MONTH -> {
                    val from = LocalDate(anchorDate.year, anchorDate.month, 1)
                    from to from.plus(1, DateTimeUnit.MONTH).plus(-1, DateTimeUnit.DAY)
                }
                AnalyticsPeriod.YEAR -> {
                    val from = LocalDate(anchorDate.year, 1, 1)
                    from to LocalDate(anchorDate.year, 12, 31)
                }
                AnalyticsPeriod.CUSTOM -> error("Custom period requires explicit boundaries")
            }
            val previousDates = when (period) {
                AnalyticsPeriod.WEEK -> rangeDates.first.plus(-DAYS_IN_WEEK, DateTimeUnit.DAY) to
                    rangeDates.second.plus(-DAYS_IN_WEEK, DateTimeUnit.DAY)
                AnalyticsPeriod.MONTH -> {
                    val from = rangeDates.first.plus(-1, DateTimeUnit.MONTH)
                    from to from.plus(1, DateTimeUnit.MONTH).plus(-1, DateTimeUnit.DAY)
                }
                AnalyticsPeriod.YEAR -> LocalDate(anchorDate.year - 1, 1, 1) to
                    LocalDate(anchorDate.year - 1, 12, 31)
                AnalyticsPeriod.CUSTOM -> error("Custom period requires explicit boundaries")
            }
            return createSelection(period, rangeDates, previousDates, currentTime)
        }

        override fun changePeriod(
            period: AnalyticsPeriod,
            selection: AnalyticsRangeSelection,
            currentTime: Instant,
        ): AnalyticsRangeSelection {
            val anchor = if (currentTime in selection.range.from..selection.range.to) {
                currentTime
            } else {
                val from = selection.range.from.toEpochMilliseconds()
                val to = selection.range.to.toEpochMilliseconds()
                Instant.fromEpochMilliseconds(from + (to - from) / 2L)
            }
            return selectPeriod(period, anchor, currentTime)
        }

        override fun selectCustom(
            from: Instant,
            to: Instant,
            currentTime: Instant,
        ): AnalyticsRangeSelection {
            val firstDate = from.toLocalDateTime(timeZone).date
            val secondDate = to.toLocalDateTime(timeZone).date
            val rangeDates = minOf(firstDate, secondDate) to maxOf(firstDate, secondDate)
            val days = rangeDates.first.daysUntil(rangeDates.second) + 1
            val previousTo = rangeDates.first.plus(-1, DateTimeUnit.DAY)
            val previousFrom = previousTo.plus(-(days - 1), DateTimeUnit.DAY)
            return createSelection(
                period = AnalyticsPeriod.CUSTOM,
                rangeDates = rangeDates,
                previousDates = previousFrom to previousTo,
                currentTime = currentTime,
            )
        }

        override fun shift(
            selection: AnalyticsRangeSelection,
            amount: Int,
            currentTime: Instant,
        ): AnalyticsRangeSelection {
            val fromDate = selection.range.from.toLocalDateTime(timeZone).date
            return when (selection.period) {
                AnalyticsPeriod.WEEK -> selectPeriod(
                    period = AnalyticsPeriod.WEEK,
                    anchor = fromDate.plus(amount * DAYS_IN_WEEK, DateTimeUnit.DAY).atStartOfDayIn(timeZone),
                    currentTime = currentTime,
                )
                AnalyticsPeriod.MONTH -> selectPeriod(
                    period = AnalyticsPeriod.MONTH,
                    anchor = fromDate.plus(amount, DateTimeUnit.MONTH).atStartOfDayIn(timeZone),
                    currentTime = currentTime,
                )
                AnalyticsPeriod.YEAR -> selectPeriod(
                    period = AnalyticsPeriod.YEAR,
                    anchor = fromDate.plus(amount, DateTimeUnit.YEAR).atStartOfDayIn(timeZone),
                    currentTime = currentTime,
                )
                AnalyticsPeriod.CUSTOM -> {
                    val toDate = selection.range.to.toLocalDateTime(timeZone).date
                    val days = fromDate.daysUntil(toDate) + 1
                    selectCustom(
                        from = fromDate.plus(amount * days, DateTimeUnit.DAY).atStartOfDayIn(timeZone),
                        to = toDate.plus(amount * days, DateTimeUnit.DAY).atStartOfDayIn(timeZone),
                        currentTime = currentTime,
                    )
                }
            }
        }

        private fun createSelection(
            period: AnalyticsPeriod,
            rangeDates: Pair<LocalDate, LocalDate>,
            previousDates: Pair<LocalDate, LocalDate>,
            currentTime: Instant,
        ): AnalyticsRangeSelection {
            val range = rangeDates.toTimeRange()
            val previousRange = previousDates.toTimeRange()
            val currentDate = currentTime.toLocalDateTime(timeZone).date
            val comparisonCutoff = when {
                currentDate < rangeDates.first -> Instant.fromEpochMilliseconds(
                    previousRange.from.toEpochMilliseconds() - 1L,
                )
                currentDate <= rangeDates.second -> Instant.fromEpochMilliseconds(
                    previousRange.from.toEpochMilliseconds() +
                        (currentTime.toEpochMilliseconds() - range.from.toEpochMilliseconds()),
                ).coerceAtMost(previousRange.to)
                else -> previousRange.to
            }
            val days = rangeDates.first.daysUntil(rangeDates.second) + 1
            val granularity = when {
                period == AnalyticsPeriod.YEAR -> AnalyticsGranularity.MONTH
                period != AnalyticsPeriod.CUSTOM -> AnalyticsGranularity.DAY
                days <= MAX_DAILY_CUSTOM_DAYS -> AnalyticsGranularity.DAY
                days <= MAX_WEEKLY_CUSTOM_DAYS -> AnalyticsGranularity.WEEK
                else -> AnalyticsGranularity.MONTH
            }
            return AnalyticsRangeSelection(
                period = period,
                range = range,
                previousRange = previousRange,
                granularity = granularity,
                comparisonCutoff = comparisonCutoff,
            )
        }

        private fun Pair<LocalDate, LocalDate>.toTimeRange(): TimeRange {
            return TimeRange(
                from = first.atStartOfDayIn(timeZone),
                to = second.endOfDay(),
            )
        }

        private fun LocalDate.endOfDay(): Instant {
            return Instant.fromEpochMilliseconds(
                plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).toEpochMilliseconds() - 1L,
            )
        }
    }
}

private const val DAYS_IN_WEEK = 7
private const val MAX_DAILY_CUSTOM_DAYS = 45
private const val MAX_WEEKLY_CUSTOM_DAYS = 180
