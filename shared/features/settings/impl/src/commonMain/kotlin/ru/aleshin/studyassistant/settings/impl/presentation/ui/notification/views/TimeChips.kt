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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.views

import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import ru.aleshin.studyassistant.core.ui.mappers.toLanguageString
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseTimePickerDialog
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * @author Stanislav Aleshin on 25.08.2024.
 */
@Composable
internal fun BeforeTimeChip(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedTime: Long,
    onTimeChange: (Long) -> Unit,
) {
    var timePickerState by remember { mutableStateOf(false) }

    AssistChip(
        onClick = { timePickerState = true },
        label = {
            Text(
                text = buildString {
                    append(SettingsThemeRes.strings.beforeTimePrefix)
                    append(selectedTime.toDuration(DurationUnit.MILLISECONDS).toLanguageString())
                },
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        enabled = enabled,
        modifier = modifier,
        trailingIcon = {
            ExpandedIcon(isExpanded = timePickerState)
        },
    )

    if (timePickerState) {
        BaseTimePickerDialog(
            initTime = LocalTime.fromMillisecondOfDay(selectedTime.toInt()),
            timeRestriction = LocalTime(hour = 2, minute = 0),
            onDismiss = { timePickerState = false },
            onConfirmTime = { selectedStartTime ->
                onTimeChange(selectedStartTime.toMillisecondOfDay().toLong())
                timePickerState = false
            },
        )
    }
}

@Composable
internal fun ReminderTimeChip(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedTime: Long,
    onTimeChange: (Long) -> Unit,
) {
    var datePickerState by remember { mutableStateOf(false) }
    val time = LocalTime.fromMillisecondOfDay(selectedTime.toInt())
    val timeFormat = LocalTime.Format {
        hour()
        char(':')
        minute()
    }

    AssistChip(
        onClick = { datePickerState = true },
        label = {
            Text(
                text = buildString {
                    append(SettingsThemeRes.strings.reminderTimePrefix)
                    append(time.format(timeFormat))
                },
                color = MaterialTheme.colorScheme.onSurface,
            )
        },
        enabled = enabled,
        modifier = modifier,
        trailingIcon = {
            ExpandedIcon(isExpanded = datePickerState)
        },
    )

    if (datePickerState) {
        BaseTimePickerDialog(
            initTime = LocalTime.fromMillisecondOfDay(selectedTime.toInt()),
            onDismiss = { datePickerState = false },
            onConfirmTime = { selectedStartTime ->
                onTimeChange(selectedStartTime.toMillisecondOfDay().toLong())
                datePickerState = false
            },
        )
    }
}