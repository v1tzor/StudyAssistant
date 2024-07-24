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

package ru.aleshin.studyassistant.core.ui.views

import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings
import ru.aleshin.studyassistant.core.ui.theme.tokens.dayOfWeekNames
import ru.aleshin.studyassistant.core.ui.theme.tokens.dayOfWeekShortNames
import ru.aleshin.studyassistant.core.ui.theme.tokens.monthNames

/**
 * Example output: 31 january
 *
 * @author Stanislav Aleshin on 20.07.2024.
 */
fun DateTimeComponents.Formats.dayMonthFormat(
    strings: StudyAssistantStrings,
) = DateTimeComponents.Format {
    dayOfMonth()
    char(' ')
    monthName(strings.monthNames())
}

/**
 * Example output: 31.01
 *
 * @author Stanislav Aleshin on 20.07.2024.
 */
fun DateTimeComponents.Formats.shortDayMonthFormat() = DateTimeComponents.Format {
    dayOfMonth()
    char('.')
    monthNumber()
}

/**
 * Example output: 31.01.2024
 *
 * @author Stanislav Aleshin on 20.07.2024.
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
 *
 * @author Stanislav Aleshin on 20.07.2024.
 */
fun DateTimeComponents.Formats.weekdayDayMonthFormat(
    strings: StudyAssistantStrings,
) = DateTimeComponents.Format {
    dayOfWeek(strings.dayOfWeekNames())
    chars(", ")
    dayOfMonth()
    char(' ')
    monthName(strings.monthNames())
}

/**
 * Example output: Sat, 31 january
 *
 * @author Stanislav Aleshin on 20.07.2024.
 */
fun DateTimeComponents.Formats.shortWeekdayDayMonthFormat(
    strings: StudyAssistantStrings,
) = DateTimeComponents.Format {
    dayOfWeek(strings.dayOfWeekShortNames())
    chars(", ")
    dayOfMonth(Padding.NONE)
    char(' ')
    monthName(strings.monthNames())
}

/**
 * Example output: 06:30
 *
 * @author Stanislav Aleshin on 20.07.2024.
 */
fun DateTimeComponents.Formats.timeFormat() = DateTimeComponents.Format {
    hour()
    char(':')
    minute()
}

/**
 * Example output: 6:30
 *
 * @author Stanislav Aleshin on 20.07.2024.
 */
fun DateTimeComponents.Formats.shortTimeFormat() = DateTimeComponents.Format {
    hour(Padding.NONE)
    char(':')
    minute()
}