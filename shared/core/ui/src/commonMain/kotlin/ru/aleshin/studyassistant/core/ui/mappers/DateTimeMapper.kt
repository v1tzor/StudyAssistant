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
package ru.aleshin.studyassistant.core.ui.mappers

import androidx.compose.runtime.Composable
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.toMinutesAndHoursSuffixString
import ru.aleshin.studyassistant.core.common.extensions.toMinutesOrHoursSuffixString
import ru.aleshin.studyassistant.core.common.extensions.toString
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.timeFormat
import kotlin.time.Duration

/**
 * @author Stanislav Aleshin on 13.04.2024.
 */
@Composable
fun Duration.toLanguageString(showAbsoluteValue: Boolean = true): String {
    val daySuffix = StudyAssistantRes.strings.daySuffix
    val minuteSuffix = StudyAssistantRes.strings.minuteSuffix
    val hourSuffix = StudyAssistantRes.strings.hourSuffix

    return this.toString(daySuffix, minuteSuffix, hourSuffix, showAbsoluteValue)
}

@Composable
fun Long.toMinutesOrHoursTitle(): String {
    val minuteSuffix = StudyAssistantRes.strings.minuteSuffix
    val hoursSuffix = StudyAssistantRes.strings.hourSuffix

    return this.toMinutesOrHoursSuffixString(minuteSuffix, hoursSuffix)
}

@Composable
fun Long.toMinutesAndHoursTitle(): String {
    val minuteSuffix = StudyAssistantRes.strings.minuteSuffix
    val hoursSuffix = StudyAssistantRes.strings.hourSuffix

    return this.toMinutesAndHoursSuffixString(minuteSuffix, hoursSuffix)
}

@Composable
fun TimeRange.format(
    fromDateTimeFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Formats.timeFormat(),
    joinChars: String = " - ",
    toDateTimeFormat: DateTimeFormat<DateTimeComponents> = DateTimeComponents.Formats.timeFormat(),
): String {
    return buildString {
        append(from.formatByTimeZone(fromDateTimeFormat))
        append(joinChars)
        append(to.formatByTimeZone(toDateTimeFormat))
    }
}