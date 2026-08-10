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

package ru.aleshin.studyassistant.core.ui.views

import androidx.compose.runtime.Composable
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.alternativeParsing
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.april_title as core_april_title
import ru.aleshin.studyassistant.core.ui.resources.august_title as core_august_title
import ru.aleshin.studyassistant.core.ui.resources.december_title as core_december_title
import ru.aleshin.studyassistant.core.ui.resources.february_title as core_february_title
import ru.aleshin.studyassistant.core.ui.resources.friday_short_title as core_friday_short_title
import ru.aleshin.studyassistant.core.ui.resources.friday_title as core_friday_title
import ru.aleshin.studyassistant.core.ui.resources.january_title as core_january_title
import ru.aleshin.studyassistant.core.ui.resources.july_title as core_july_title
import ru.aleshin.studyassistant.core.ui.resources.june_title as core_june_title
import ru.aleshin.studyassistant.core.ui.resources.march_title as core_march_title
import ru.aleshin.studyassistant.core.ui.resources.may_title as core_may_title
import ru.aleshin.studyassistant.core.ui.resources.monday_short_title as core_monday_short_title
import ru.aleshin.studyassistant.core.ui.resources.monday_title as core_monday_title
import ru.aleshin.studyassistant.core.ui.resources.november_title as core_november_title
import ru.aleshin.studyassistant.core.ui.resources.october_title as core_october_title
import ru.aleshin.studyassistant.core.ui.resources.saturday_short_title as core_saturday_short_title
import ru.aleshin.studyassistant.core.ui.resources.saturday_title as core_saturday_title
import ru.aleshin.studyassistant.core.ui.resources.september_title as core_september_title
import ru.aleshin.studyassistant.core.ui.resources.sunday_short_title as core_sunday_short_title
import ru.aleshin.studyassistant.core.ui.resources.sunday_title as core_sunday_title
import ru.aleshin.studyassistant.core.ui.resources.thursday_short_title as core_thursday_short_title
import ru.aleshin.studyassistant.core.ui.resources.thursday_title as core_thursday_title
import ru.aleshin.studyassistant.core.ui.resources.tuesday_short_title as core_tuesday_short_title
import ru.aleshin.studyassistant.core.ui.resources.tuesday_title as core_tuesday_title
import ru.aleshin.studyassistant.core.ui.resources.wednesday_short_title as core_wednesday_short_title
import ru.aleshin.studyassistant.core.ui.resources.wednesday_title as core_wednesday_title

/**
 * Example output: 31 january
 *
 * @author Stanislav Aleshin on 20.07.2024.
 */
@Composable
fun DateTimeComponents.Formats.dayMonthFormat(): DateTimeFormat<DateTimeComponents> {
    val localizedMonthNames = monthNames()
    return DateTimeComponents.Format {
        dayOfMonth()
        char(' ')
        monthName(localizedMonthNames)
    }
}

/**
 * Example output: 31.01
 */
fun DateTimeComponents.Formats.shortDayMonthFormat() = DateTimeComponents.Format {
    dayOfMonth()
    char('.')
    monthNumber()
}

/**
 * Example output: 31.01 - 14:10
 */
fun DateTimeComponents.Formats.shortDayMonthTimeFormat() = DateTimeComponents.Format {
    dayOfMonth()
    char('.')
    monthNumber()
    chars(" - ")
    hour()
    char(':')
    minute()
}

/**
 * Example output: 31.01.2024
 */
fun DateTimeComponents.Formats.dayMonthYearFormat() = DateTimeComponents.Format {
    dayOfMonth()
    char('.')
    monthNumber()
    char('.')
    year()
}

/**
 * Example output: Saturday, 31 january
 */
@Composable
fun DateTimeComponents.Formats.weekdayDayMonthFormat(): DateTimeFormat<DateTimeComponents> {
    val localizedDayOfWeekNames = dayOfWeekNames()
    val localizedMonthNames = monthNames()
    return DateTimeComponents.Format {
        dayOfWeek(localizedDayOfWeekNames)
        chars(", ")
        dayOfMonth()
        char(' ')
        monthName(localizedMonthNames)
    }
}

/**
 * Example output: Sat, 31 january
 */
@Composable
fun DateTimeComponents.Formats.shortWeekdayDayMonthFormat(): DateTimeFormat<DateTimeComponents> {
    val localizedDayOfWeekNames = dayOfWeekShortNames()
    val localizedMonthNames = monthNames()
    return DateTimeComponents.Format {
        dayOfWeek(localizedDayOfWeekNames)
        chars(", ")
        dayOfMonth(Padding.NONE)
        char(' ')
        monthName(localizedMonthNames)
    }
}

/**
 * Example output: 06:30
 */
fun DateTimeComponents.Formats.timeFormat() = DateTimeComponents.Format {
    hour()
    char(':')
    minute()
}

/**
 * Example output: 6:30
 */
fun DateTimeComponents.Formats.shortTimeFormat() = DateTimeComponents.Format {
    hour(Padding.NONE)
    char(':')
    minute()
}

/**
 * Example output: 2025-06-23T20:00:00
 * ISO 8601
 */
fun DateTimeComponents.Formats.iso8601() = DateTimeComponents.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    dayOfMonth()
    alternativeParsing(
        { char('t') }
    ) {
        char('T')
    }
    hour()
    char(':')
    minute()
    char(':')
    second()
}

@Composable
fun monthNames() = MonthNames(
    january = stringResource(CoreRes.string.core_january_title),
    february = stringResource(CoreRes.string.core_february_title),
    march = stringResource(CoreRes.string.core_march_title),
    april = stringResource(CoreRes.string.core_april_title),
    may = stringResource(CoreRes.string.core_may_title),
    june = stringResource(CoreRes.string.core_june_title),
    july = stringResource(CoreRes.string.core_july_title),
    august = stringResource(CoreRes.string.core_august_title),
    september = stringResource(CoreRes.string.core_september_title),
    october = stringResource(CoreRes.string.core_october_title),
    november = stringResource(CoreRes.string.core_november_title),
    december = stringResource(CoreRes.string.core_december_title),
)

@Composable
fun dayOfWeekNames() = DayOfWeekNames(
    monday = stringResource(CoreRes.string.core_monday_title),
    tuesday = stringResource(CoreRes.string.core_tuesday_title),
    wednesday = stringResource(CoreRes.string.core_wednesday_title),
    thursday = stringResource(CoreRes.string.core_thursday_title),
    friday = stringResource(CoreRes.string.core_friday_title),
    saturday = stringResource(CoreRes.string.core_saturday_title),
    sunday = stringResource(CoreRes.string.core_sunday_title),
)

@Composable
fun dayOfWeekShortNames() = DayOfWeekNames(
    monday = stringResource(CoreRes.string.core_monday_short_title),
    tuesday = stringResource(CoreRes.string.core_tuesday_short_title),
    wednesday = stringResource(CoreRes.string.core_wednesday_short_title),
    thursday = stringResource(CoreRes.string.core_thursday_short_title),
    friday = stringResource(CoreRes.string.core_friday_short_title),
    saturday = stringResource(CoreRes.string.core_saturday_short_title),
    sunday = stringResource(CoreRes.string.core_sunday_short_title),
)

const val TIME_SUFFIX = "T00:00:00"
