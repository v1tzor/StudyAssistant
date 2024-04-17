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
 * imitations under the License.
 */
package extensions

import com.ionspin.kotlin.bignum.decimal.BigDecimal
import functional.Constants
import functional.TimeRange
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
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
    amount: Int,
    timeZone: TimeZone = TimeZone.currentSystemDefault()
): Instant {
    return if (amount < 0) {
        this.minus(value = -amount, unit = DateTimeUnit.MILLISECOND, timeZone = timeZone)
    } else {
        this.plus(value = amount, unit = DateTimeUnit.MILLISECOND, timeZone = timeZone)
    }
}

fun Instant.isCurrentDay(date: Instant): Boolean {
    val timeZone = TimeZone.currentSystemDefault()
    val currentDate = this.toLocalDateTime(timeZone).dayOfYear
    val compareDate = date.toLocalDateTime(timeZone).dayOfYear

    return currentDate == compareDate
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

fun Instant.setHoursAndMinutes(hour: Int, minute: Int): Instant {
    val timeZone = TimeZone.currentSystemDefault()
    val dateTime = this.toLocalDateTime(timeZone)
    return LocalDateTime(
        date = dateTime.date,
        time = LocalTime(
            hour = hour,
            minute = minute,
            second = dateTime.second,
            nanosecond = dateTime.nanosecond
        )
    ).toInstant(timeZone)
}

fun epochDuration(start: Instant, end: Instant): Long {
    return end.toEpochMilliseconds() - start.toEpochMilliseconds()
}

fun Instant.isNotZeroDifference(end: Instant): Boolean {
    return epochDuration(this, end) > 0L
}

fun epochDuration(timeRange: TimeRange): Long {
    return timeRange.to.toEpochMilliseconds() - timeRange.from.toEpochMilliseconds()
}

fun durationOrZero(start: Instant?, end: Instant?) = if (start != null && end != null) {
    epochDuration(start, end)
} else {
    Constants.Date.EMPTY_DURATION
}

fun Long?.mapEpochTimeToInstantOrDefault(default: Instant): Instant {
    return if (this == null) default else Instant.fromEpochMilliseconds(this)
}

fun Long.mapEpochTimeToInstant(): Instant {
    return Instant.fromEpochMilliseconds(this)
}

fun Long.toSeconds(): Long {
    return this / Constants.Date.MILLIS_IN_SECONDS
}

fun Long.toMinutes(): Long {
    return toSeconds() / Constants.Date.SECONDS_IN_MINUTE
}

fun Long.toMinutesInHours(): Long {
    val hours = toHorses()
    val minutes = toMinutes()
    return minutes - hours * Constants.Date.MINUTES_IN_HOUR
}

fun Long.toHorses(): Long {
    return toMinutes() / Constants.Date.MINUTES_IN_HOUR
}

fun Int.minutesToMillis(): Long {
    return this * Constants.Date.MILLIS_IN_MINUTE
}

fun Int.hoursToMillis(): Long {
    return this * Constants.Date.MILLIS_IN_HOUR
}

//fun Long.toMinutesOrHoursString(minutesSymbol: String, hoursSymbol: String): String {
//    val minutes = this.toMinutes()
//    val hours = this.toHorses()
//
//    return if (minutes == 0L) {
//        Constants.Date.minutesFormat.format("1", minutesSymbol)
//    } else if (minutes in 1L..59L) {
//        Constants.Date.minutesFormat.format(minutes.toString(), minutesSymbol)
//    } else if (minutes > 59L && (minutes % 60L) != 0L) {
//        Constants.Date.hoursAndMinutesFormat.format(
//            hours.toString(),
//            hoursSymbol,
//            toMinutesInHours().toString(),
//            minutesSymbol,
//        )
//    } else {
//        Constants.Date.hoursFormat.format(hours.toString(), hoursSymbol)
//    }
//}
//
//fun Long.toMinutesAndHoursString(minutesSymbol: String, hoursSymbol: String): String {
//    val minutes = this.toMinutes()
//    val hours = this.toHorses()
//
//    return Constants.Date.hoursAndMinutesFormat.format(
//        hours.toString(),
//        hoursSymbol,
//        (minutes - hours * Constants.Date.MINUTES_IN_HOUR).toString(),
//        minutesSymbol,
//    )
//}
//
//fun Date.setZeroSecond(): Date {
//    val calendar = Calendar.getInstance().apply {
//        time = this@setZeroSecond
//        set(Calendar.SECOND, 0)
//    }
//
//    return calendar.time
//}
//
//fun TimeRange.isIncludeTime(time: Date?): Boolean {
//    if (time == null) return false
//    return time >= this.from && time <= this.to
//}
//
//fun Date.toMonthAndDayTitle(): String {
//    val calendar = Calendar.getInstance().apply { time = this@toMonthAndDayTitle }
//    val month = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.MONTH) + 1)
//    val day = String.format(Locale.getDefault(), "%02d", calendar.get(Calendar.DAY_OF_MONTH))
//    return "$day.$month"
//}
//
//fun TimeRange.toDaysTitle(): String {
//    val calendar = Calendar.getInstance()
//    val dayStart = calendar.apply { time = from }.get(Calendar.DAY_OF_MONTH)
//    val dayEnd = calendar.apply { time = to }.get(Calendar.DAY_OF_MONTH)
//    return "$dayStart-$dayEnd"
//}
//
//fun TimeRange.toMonthTitle(): String {
//    val calendar = Calendar.getInstance()
//    val monthStart = calendar.apply { time = from }.get(Calendar.MONTH) + 1
//    val monthEnd = calendar.apply { time = to }.get(Calendar.MONTH) + 1
//    return "$monthStart-$monthEnd"
//}

fun countWeeksByDays(days: Int): Int {
    return BigDecimal.fromDouble(days.toDouble() / Constants.Date.DAYS_IN_WEEK).ceil().intValue()
}

fun countMonthByDays(days: Int): Int {
    return BigDecimal.fromDouble(days.toDouble() / Constants.Date.DAYS_IN_MONTH).ceil().intValue()
}
