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
package ru.aleshin.studyassistant.core.ui.views.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.functional.TimeFormat
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.TimeFormatSelector

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
@Composable
fun TimePickerDialog(
    modifier: Modifier = Modifier,
    initTime: Instant?,
    timeRestriction: LocalTime? = null,
    header: String = StudyAssistantRes.strings.timePickerDialogHeader,
    showCurrentTimeSelector: Boolean = true,
    onDismiss: () -> Unit,
    onConfirmTime: (Instant) -> Unit,
) {
    BaseTimePickerDialog(
        modifier = modifier,
        initTime = initTime?.dateTime()?.time,
        timeRestriction = timeRestriction,
        header = header,
        showCurrentTimeSelector = showCurrentTimeSelector,
        onDismiss = onDismiss,
        onConfirmTime = { time ->
            val dateTime = time.atDate(Clock.System.now().dateTime().date)
            onConfirmTime.invoke(dateTime.toInstant(TimeZone.currentSystemDefault()))
        }
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BaseTimePickerDialog(
    modifier: Modifier = Modifier,
    initTime: LocalTime?,
    timeRestriction: LocalTime? = null,
    header: String = StudyAssistantRes.strings.timePickerDialogHeader,
    showCurrentTimeSelector: Boolean = true,
    onDismiss: () -> Unit,
    onConfirmTime: (LocalTime) -> Unit,
) {
    val is24Format = true // TODO: Real get 24 format

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.height(235.dp).width(if (is24Format) 290.dp else 350.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.End,
            ) {
                val currentDateTime = Clock.System.now().dateTime()
                var hour by rememberSaveable { mutableStateOf(initTime?.hour) }
                var minute by rememberSaveable { mutableStateOf(initTime?.minute) }
                val localTime = remember(hour, minute) { LocalTime(hour ?: 0, minute ?: 0) }
                var format by remember {
                    mutableStateOf(if (hour != null && hour!! > 11) TimeFormat.PM else TimeFormat.AM)
                }
                TimePickerHeader(title = header)
                TimePickerHourMinuteSelector(
                    modifier = Modifier.weight(1f),
                    hours = hour,
                    minutes = minute,
                    format = format,
                    is24Format = is24Format,
                    onHoursChanges = { value -> hour = value },
                    onMinutesChanges = { value -> minute = value },
                    onChangeFormat = {
                        hour = null
                        format = it
                    },
                )
                TimePickerActions(
                    enabled = if (timeRestriction != null) {
                        localTime <= timeRestriction
                    } else {
                        true
                    },
                    showCurrentTimeSelector = showCurrentTimeSelector,
                    onDismiss = onDismiss,
                    onCurrentTimeChoose = {
                        hour = currentDateTime.hour + if (currentDateTime.minute == 59) 1 else 0
                        minute = if (currentDateTime.minute != 59) {
                            currentDateTime.minute + 1
                        } else {
                            0
                        }
                        if (!is24Format && (hour!! > 12 || hour == 0)) format = TimeFormat.PM
                    },
                    onConfirm = {
                        val time = LocalTime(
                            hour = hour ?: 0,
                            minute = minute ?: 0,
                            second = 0,
                            nanosecond = 0,
                        )
                        onConfirmTime.invoke(time)
                    },
                )
            }
        }
    }
}

@Composable
internal fun TimePickerHeader(
    modifier: Modifier = Modifier,
    title: String,
) = Box(
    modifier = modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp).fillMaxWidth(),
) {
    Text(
        text = title,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelMedium,
    )
}

@Composable
internal fun TimePickerHourMinuteSelector(
    modifier: Modifier = Modifier,
    hours: Int?,
    minutes: Int?,
    is24Format: Boolean,
    format: TimeFormat,
    onHoursChanges: (Int?) -> Unit,
    onMinutesChanges: (Int?) -> Unit,
    onChangeFormat: (TimeFormat) -> Unit,
) = Row(
    modifier = modifier.padding(horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    val requester = remember { FocusRequester() }
    OutlinedTextField(
        modifier = Modifier.weight(1f),
        value = if (is24Format) {
            hours?.toString() ?: ""
        } else {
            when {
                hours == 0 && format == TimeFormat.AM -> "12"
                hours == 0 && format == TimeFormat.PM -> "12"
                format == TimeFormat.PM && hours != 12 -> hours?.minus(12)?.toString() ?: ""
                else -> hours?.toString() ?: ""
            }
        },
        onValueChange = {
            val time = it.toIntOrNull()
            if (time != null && is24Format && time in 0..23) {
                onHoursChanges(time)
            } else if (time != null && !is24Format && time in 1..12) {
                val formatTime = when (format) {
                    TimeFormat.PM -> if (time != 12) time + 12 else 12
                    TimeFormat.AM -> if (time != 12) time else 0
                }
                onHoursChanges(formatTime)
            } else if (it.isBlank()) {
                onHoursChanges(null)
            }
        },
        textStyle = MaterialTheme.typography.displayMedium.copy(textAlign = TextAlign.Center),
        shape = MaterialTheme.shapes.small,
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )
    Text(
        modifier = Modifier.width(24.dp),
        text = StudyAssistantRes.strings.separator,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.displayLarge,
        color = MaterialTheme.colorScheme.onSurface,
    )
    OutlinedTextField(
        modifier = Modifier.weight(1f).focusRequester(requester),
        value = minutes?.toString() ?: "",
        onValueChange = {
            val time = it.toIntOrNull()
            if (time != null && time in 0..59) {
                onMinutesChanges(time)
            } else if (it.isBlank()) {
                onMinutesChanges(null)
            }
        },
        textStyle = MaterialTheme.typography.displayMedium.copy(textAlign = TextAlign.Center),
        shape = MaterialTheme.shapes.small,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )
    TimeFormatSelector(
        modifier = Modifier.size(height = 80.dp, width = 52.dp).offset(x = 12.dp),
        isVisible = !is24Format,
        format = format,
        onChangeFormat = onChangeFormat,
    )
}

@Composable
internal fun TimePickerActions(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showCurrentTimeSelector: Boolean = false,
    onCurrentTimeChoose: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) = Row(
    modifier = modifier.fillMaxWidth().padding(bottom = 20.dp, start = 16.dp, end = 24.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    if (showCurrentTimeSelector && onCurrentTimeChoose != null) {
        IconButton(onClick = onCurrentTimeChoose) {
            Icon(
                painter = painterResource(StudyAssistantRes.icons.timeOutline),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
    Spacer(modifier = Modifier.weight(1f))
    TextButton(onClick = onDismiss) {
        Text(
            text = StudyAssistantRes.strings.cancelTitle,
            maxLines = 1,
        )
    }
    TextButton(enabled = enabled, onClick = onConfirm) {
        Text(
            text = StudyAssistantRes.strings.selectConfirmTitle,
            maxLines = 1,
        )
    }
}