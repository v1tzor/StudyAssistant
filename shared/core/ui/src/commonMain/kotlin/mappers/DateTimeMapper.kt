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
package mappers

import androidx.compose.runtime.Composable
import extensions.toMinutesAndHoursString
import extensions.toMinutesOrHoursString
import functional.TimeRange
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 13.04.2024.
 */
@Composable
fun Long.toMinutesOrHoursTitle(): String {
    val minutesSymbols = StudyAssistantRes.strings.minutesSuffix
    val hoursSymbols = StudyAssistantRes.strings.hoursSuffix

    return this.toMinutesOrHoursString(minutesSymbols, hoursSymbols).text
}

@Composable
fun Long.toMinutesAndHoursTitle(): String {
    val minutesSymbols = StudyAssistantRes.strings.minutesSuffix
    val hoursSymbols = StudyAssistantRes.strings.hoursSuffix

    return this.toMinutesAndHoursString(minutesSymbols, hoursSymbols).text
}

@Composable
fun TimeRange.format(
    fromDateTimeFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format {
        hour(Padding.ZERO)
        char(':')
        minute(Padding.ZERO)
    },
    joinSymbol: String = " - ",
    toDateTimeFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Format {
        hour(Padding.ZERO)
        char(':')
        minute(Padding.ZERO)
    },
): String {
    return buildString {
        append(from.format(fromDateTimeFormat))
        append(joinSymbol)
        append(to.format(toDateTimeFormat))
    }
}

