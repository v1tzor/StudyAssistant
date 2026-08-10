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
package ru.aleshin.studyassistant.core.ui.mappers

import androidx.compose.runtime.Composable
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.DateTimeFormat
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.toMinutesAndHoursSuffixString
import ru.aleshin.studyassistant.core.common.extensions.toMinutesOrHoursSuffixString
import ru.aleshin.studyassistant.core.common.extensions.toString
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.ui.views.timeFormat
import kotlin.time.Duration
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.day_suffix as core_day_suffix
import ru.aleshin.studyassistant.core.ui.resources.hour_suffix as core_hour_suffix
import ru.aleshin.studyassistant.core.ui.resources.minute_suffix as core_minute_suffix

/**
 * @author Stanislav Aleshin on 13.04.2024.
 */
@Composable
fun Duration.toLanguageString(showAbsoluteValue: Boolean = true): String {
    val daySuffix = stringResource(CoreRes.string.core_day_suffix)
    val minuteSuffix = stringResource(CoreRes.string.core_minute_suffix)
    val hourSuffix = stringResource(CoreRes.string.core_hour_suffix)
    return toString(daySuffix, minuteSuffix, hourSuffix, showAbsoluteValue)
}

@Composable
fun Long.toMinutesOrHoursTitle(): String {
    val minuteSuffix = stringResource(CoreRes.string.core_minute_suffix)
    val hoursSuffix = stringResource(CoreRes.string.core_hour_suffix)
    return toMinutesOrHoursSuffixString(minuteSuffix, hoursSuffix)
}

@Composable
fun Long.toMinutesAndHoursTitle(): String {
    val minuteSuffix = stringResource(CoreRes.string.core_minute_suffix)
    val hoursSuffix = stringResource(CoreRes.string.core_hour_suffix)
    return toMinutesAndHoursSuffixString(minuteSuffix, hoursSuffix)
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
