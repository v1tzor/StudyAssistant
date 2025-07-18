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
package ru.aleshin.studyassistant.core.common.extensions

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.minus
import kotlinx.datetime.offsetIn
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import ru.aleshin.studyassistant.core.common.functional.Constants.Date
import ru.aleshin.studyassistant.core.common.functional.Constants.Date.DAYS_IN_WEEK
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import kotlin.time.Duration

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
fun Instant.dateTime(timeZone: TimeZone = TimeZone.currentSystemDefault()) =
    toLocalDateTime(timeZone)

fun Instant.formatByTimeZone(
    format: DateTimeFormat<DateTimeComponents>,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val offset = offsetIn(timeZone)
    return format(format = format, offset = offset)
}

fun Instant.Companion.parseUsingOffset(
    input: CharSequence,
    format: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET
): Instant {
    return format.parse(input).toInstantUsingOffset()
}

fun Instant.shiftWeek(amount: Int, timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    return if (amount < 0) {
        this.minus(value = -amount, unit = DateTimeUnit.WEEK, timeZone = timeZone)
    } else {
        this.plus(value = amount, unit = DateTimeUnit.WEEK, timeZone = timeZone)
    }
}

fun Instant.shiftDay(amount: Int, timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    return if (amount < 0) {
        this.minus(value = -amount, unit = DateTimeUnit.DAY, timeZone = timeZone)
    } else {
        this.plus(value = amount, unit = DateTimeUnit.DAY, timeZone = timeZone)
    }
}

fun Instant.shiftMinutes(
    amount: Int,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Instant {
    return if (amount < 0) {
        this.minus(value = -amount, unit = DateTimeUnit.MINUTE, timeZone = timeZone)
    } else {
        this.plus(value = amount, unit = DateTimeUnit.MINUTE, timeZone = timeZone)
    }
}

fun Instant.shiftMillis(
    amount: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Instant {
    return if (amount < 0) {
        this.minus(value = -amount, unit = DateTimeUnit.MILLISECOND, timeZone = timeZone)
    } else {
        this.plus(value = amount, unit = DateTimeUnit.MILLISECOND, timeZone = timeZone)
    }
}

fun TimeRange.shiftWeek(
    value: Int,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): TimeRange {
    return copy(
        from = from.shiftDay(value * DAYS_IN_WEEK, timeZone),
        to = to.shiftDay(value * DAYS_IN_WEEK, timeZone),
    )
}

fun LocalDate.equalsDay(date: LocalDate?): Boolean {
    return dayOfYear == date?.dayOfYear
}

fun Instant.equalsDay(
    date: LocalDate?,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean {
    return toLocalDateTime(timeZone).date.equalsDay(date)
}

fun Instant.equalsDay(
    date: Instant?,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean {
    return equalsDay(date?.toLocalDateTime(timeZone)?.date)
}

fun Instant.isNextDay(
    date: Instant?,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean {
    return this.shiftDay(1).equalsDay(date, timeZone)
}

fun Instant.isCurrentWeek(
    date: Instant?,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Boolean {
    val weekTimeRange = dateTime().weekTimeRange()
    return date?.let { weekTimeRange.containsDate(it) } ?: false
}

fun Instant.compareByHoursAndMinutes(compareDate: Instant): Boolean {
    val timeZone = TimeZone.currentSystemDefault()
    val firstDateTime = this.toLocalDateTime(timeZone)
    val secondDateTime = compareDate.toLocalDateTime(timeZone)
    val hoursEquals = firstDateTime.hour == secondDateTime.hour
    val minutesEquals = firstDateTime.minute == secondDateTime.minute

    return hoursEquals && minutesEquals
}

fun Instant.startThisDay(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    val datetime = this.toLocalDateTime(timeZone)
    return datetime.setStartDay().toInstant(timeZone)
}

fun Instant.endThisDay(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    val datetime = this.toLocalDateTime(timeZone)
    return datetime.setEndDay().toInstant(timeZone)
}

fun LocalDateTime.setStartDay() = LocalDateTime(
    date = date,
    time = LocalTime(0, 0, 0, 0)
)

fun LocalDateTime.setEndDay() = LocalDateTime(
    date = date,
    time = LocalTime(23, 59, 59, 59)
)

fun Instant.setHoursAndMinutes(
    instance: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Instant {
    val dateTime = instance.toLocalDateTime(timeZone)
    val hour = dateTime.hour
    val minute = dateTime.minute
    return setHoursAndMinutes(hour, minute)
}

fun Instant.setHoursAndMinutes(time: LocalTime): Instant {
    val hour = time.hour
    val minute = time.minute
    return setHoursAndMinutes(hour, minute)
}

fun Instant.setHoursAndMinutes(
    hour: Int,
    minute: Int,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Instant {
    return toLocalDateTime(timeZone).setHoursAndMinutes(hour, minute).toInstant(timeZone)
}

fun LocalDateTime.setHoursAndMinutes(hour: Int, minute: Int): LocalDateTime {
    return LocalDateTime(
        date = date,
        time = LocalTime(
            hour = hour,
            minute = minute,
            second = 0,
            nanosecond = 0
        )
    )
}

fun epochDateTimeDuration(start: Instant, end: Instant): Long {
    return end.toEpochMilliseconds() - start.toEpochMilliseconds()
}

fun epochTimeDuration(start: Instant, end: Instant): Long {
    return end.dateTime().time.epochTimeDuration() - start.dateTime().time.epochTimeDuration()
}

fun LocalDateTime.dayEpochDuration(): Long {
    return time.epochTimeDuration()
}

fun LocalTime.epochTimeDuration(): Long {
    return hour.hoursToMillis() + minute.minutesToMillis()
}

fun Instant.isNotZeroDifference(end: Instant): Boolean {
    return epochDateTimeDuration(this, end) > 0L
}

fun epochDateTimeDuration(timeRange: TimeRange): Long {
    return epochDateTimeDuration(timeRange.from, timeRange.to)
}

fun epochTimeDuration(timeRange: TimeRange): Long {
    return epochTimeDuration(timeRange.from, timeRange.to)
}

fun dateTimeDurationOrZero(start: Instant?, end: Instant?) = if (start != null && end != null) {
    epochDateTimeDuration(start, end)
} else {
    Date.EMPTY_DURATION
}

fun Long?.mapEpochTimeToInstantOrDefault(default: Instant): Instant {
    return if (this == null) default else Instant.fromEpochMilliseconds(this)
}

fun Long.mapEpochTimeToInstant(): Instant {
    return Instant.fromEpochMilliseconds(this)
}

fun Long.toSeconds(): Long {
    return this / Date.MILLIS_IN_SECONDS
}

fun Long.toMinutes(): Long {
    return toSeconds() / Date.SECONDS_IN_MINUTE
}

fun Long.toMinutesInHours(): Long {
    val hours = toHorses()
    val minutes = toMinutes()
    return minutes - hours * Date.MINUTES_IN_HOUR
}

fun Long.toHorses(): Long {
    return toMinutes() / Date.MINUTES_IN_HOUR
}

fun Int.minutesToMillis(): Long {
    return this * Date.MILLIS_IN_MINUTE
}

fun Int.hoursToMillis(): Long {
    return this * Date.MILLIS_IN_HOUR
}

fun Duration.toString(
    daySuffix: String,
    minuteSuffix: String,
    hourSuffix: String,
    showAbsoluteValue: Boolean = true,
) = buildString {
    val isNegative = isNegative()
    if (isNegative && !showAbsoluteValue) append('-')
    absoluteValue.toComponents { days, hours, minutes, seconds, _ ->
        val hasDays = days != 0L
        val hasHours = hours != 0
        val hasMinutes = minutes != 0
        val hasSeconds = seconds != 0
        var components = 0
        if (hasDays) {
            append(days).append(daySuffix)
            components++
        }
        if (hasHours) {
            if (components > 0) append(' ')
            append(hours).append(hourSuffix)
        }
        if (!hasDays && !hasHours && hasMinutes) {
            append(minutes).append(minuteSuffix)
        } else if (!hasDays && !hasHours && !hasMinutes && hasSeconds) {
            append("< 0").append(minuteSuffix)
        } else if (!hasDays && !hasHours && !hasMinutes && !hasSeconds) {
            append(0).append(minuteSuffix)
        }
    }
}

fun Long.toShortTimeString(): String {
    val hours = this.toHorses()
    val minutes = this.toMinutesInHours()
    val seconds = this.toSeconds() - this.toMinutes() * Date.SECONDS_IN_MINUTE

    return buildString {
        append(hours.toString().padStart(2, '0'))
        append(':')
        append(minutes.toString().padStart(2, '0'))
        append(':')
        append(seconds.toString().padStart(2, '0'))
    }
}

fun Long.toMinutesOrHoursSuffixString(minutesSuffix: String, hoursSuffix: String): String {
    val minutes = this.toMinutes()
    val hours = this.toHorses()

    return if (minutes == 0L) {
        buildString { append("1", minutesSuffix) }
    } else if (minutes in 1L..59L) {
        buildString { append(minutes.toString(), minutesSuffix) }
    } else if (minutes > 59L && (minutes % 60L) != 0L) {
        buildString {
            append(hours.toString(), hoursSuffix)
            append(' ')
            append(toMinutesInHours().toString(), minutesSuffix)
        }
    } else {
        buildString { append(hours.toString(), hoursSuffix) }
    }
}

fun Long.toMinutesAndHoursSuffixString(minutesSuffix: String, hoursSuffix: String): String {
    val minutes = this.toMinutes()
    val hours = this.toHorses()

    return buildString {
        append(hours.toString(), hoursSuffix)
        append(' ')
        append((minutes - hours * Date.MINUTES_IN_HOUR).toString(), minutesSuffix)
    }
}

fun Long.toMinutesAndHoursString(): String {
    val minutes = this.toMinutes()
    val hours = this.toHorses()

    return buildString {
        if (hours < 10) append("0")
        append(hours.toString())
        append(':')
        if (minutes < 10) append("0")
        append((minutes - hours * Date.MINUTES_IN_HOUR).toString())
    }
}

fun countWeeksByDays(days: Int): Int {
    return BigDecimal.fromDouble(days.toDouble() / DAYS_IN_WEEK).ceil().intValue()
}

fun countMonthByDays(days: Int): Int {
    return BigDecimal.fromDouble(days.toDouble() / Date.DAYS_IN_MONTH).ceil().intValue()
}

fun Instant.startOfWeek(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    val dateTime = toLocalDateTime(timeZone)
    return shiftDay(-dateTime.dayOfWeek.ordinal, timeZone).startThisDay(timeZone)
}

fun Instant.endOfWeek(timeZone: TimeZone = TimeZone.currentSystemDefault()): Instant {
    val dateTime = toLocalDateTime(timeZone)
    return shiftDay(DayOfWeek.entries.lastIndex - dateTime.dayOfWeek.ordinal, timeZone).endThisDay()
}

fun Instant.dateOfWeekDay(
    dayOfWeek: DayOfWeek,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Instant {
    return startOfWeek(timeZone).shiftDay(dayOfWeek.ordinal, timeZone)
}

fun LocalDateTime.weekTimeRange(timeZone: TimeZone = TimeZone.currentSystemDefault()): TimeRange {
    val instant = toInstant(timeZone)
    return TimeRange(from = instant.startOfWeek(timeZone), to = instant.endOfWeek(timeZone))
}

fun DayOfWeek.dateTimeByWeek(
    mondayDate: Instant,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Instant {
    return mondayDate.shiftDay(ordinal, timeZone)
}

fun LocalDate.isoWeekNumber(): Int {
    if (firstWeekInYearStart(year + 1) < this) return 1

    val currentYearStart = firstWeekInYearStart(year)
    val start = if (this < currentYearStart) firstWeekInYearStart(year - 1) else currentYearStart

    val currentCalendarWeek = start.until(this, DateTimeUnit.WEEK) + 1

    return currentCalendarWeek
}

private fun firstWeekInYearStart(year: Int): LocalDate {
    val jan1st = LocalDate(year, 1, 1)
    val previousMonday = jan1st.minus(jan1st.dayOfWeek.ordinal, DateTimeUnit.DAY)

    return if (jan1st.dayOfWeek <= DayOfWeek.THURSDAY) {
        previousMonday
    } else {
        previousMonday.plus(1, DateTimeUnit.WEEK)
    }
}

fun DateTimePeriod.millis() = minutes.minutesToMillis()

fun LocalTime.untilInMillis(time: LocalTime): Long {
    val value = time.toMillisecondOfDay() - this.toMillisecondOfDay()
    return value.toLong()
}