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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.toUtcEpochDateMillis
import ru.aleshin.studyassistant.core.common.extensions.utcEpochDateToLocalStartOfDay
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.dialog.TimePickerDialog
import ru.aleshin.studyassistant.core.ui.views.shortWeekdayDayMonthFormat
import ru.aleshin.studyassistant.core.ui.views.timeFormat
import ru.aleshin.studyassistant.editor.impl.resources.Res
import ru.aleshin.studyassistant.editor.impl.resources.todo_deadline_date_picker_headline
import ru.aleshin.studyassistant.editor.impl.resources.todo_deadline_field_label
import ru.aleshin.studyassistant.editor.impl.resources.todo_deadline_field_placeholder
import ru.aleshin.studyassistant.editor.impl.resources.todo_time_field_label
import ru.aleshin.studyassistant.editor.impl.resources.todo_time_field_placeholder
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.cancel_title as core_cancel_title
import ru.aleshin.studyassistant.core.ui.resources.date_picker_dialog_header as core_date_picker_dialog_header
import ru.aleshin.studyassistant.core.ui.resources.ic_calendar_today as core_ic_calendar_today
import ru.aleshin.studyassistant.core.ui.resources.ic_clock_outline as core_ic_clock_outline
import ru.aleshin.studyassistant.core.ui.resources.ic_select_date as core_ic_select_date
import ru.aleshin.studyassistant.core.ui.resources.select_confirm_title as core_select_confirm_title

/**
 * @author Stanislav Aleshin on 26.07.2024.
 */
@Composable
internal fun TodoDeadlineInfoFields(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    deadline: Instant?,
    onChangeDeadline: (Instant?) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        var datePickerState by remember { mutableStateOf(false) }
        var timePickerState by remember { mutableStateOf(false) }

        ClickableInfoTextField(
            onClick = { datePickerState = true },
            enabled = !isLoading,
            value = deadline?.formatByTimeZone(
                format = DateTimeComponents.Formats.shortWeekdayDayMonthFormat()
            ),
            infoIcon = painterResource(CoreRes.drawable.core_ic_calendar_today),
            label = stringResource(Res.string.todo_deadline_field_label),
            placeholder = stringResource(Res.string.todo_deadline_field_placeholder),
            trailingIcon = {
                Icon(
                    painter = painterResource(CoreRes.drawable.core_ic_select_date),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        )

        ClickableInfoTextField(
            onClick = { timePickerState = true },
            enabled = !isLoading,
            value = deadline?.formatByTimeZone(DateTimeComponents.Formats.timeFormat()),
            infoIcon = painterResource(CoreRes.drawable.core_ic_clock_outline),
            label = stringResource(Res.string.todo_time_field_label),
            placeholder = stringResource(Res.string.todo_time_field_placeholder),
            trailingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Default.ArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        )

        if (timePickerState) {
            TimePickerDialog(
                initTime = deadline,
                onDismiss = { timePickerState = false },
                onConfirmTime = { time ->
                    onChangeDeadline(time)
                    timePickerState = false
                },
            )
        }

        if (datePickerState) {
            TodoDatePicker(
                deadline = deadline,
                onDismiss = { datePickerState = false },
                onSelectedDate = { date ->
                    onChangeDeadline(date)
                    datePickerState = false
                }
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun TodoDatePicker(
    modifier: Modifier = Modifier,
    deadline: Instant?,
    onDismiss: () -> Unit,
    onSelectedDate: (Instant?) -> Unit,
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = deadline?.toUtcEpochDateMillis()
    )
    val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = confirmEnabled,
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis ?: return@TextButton
                    val targetDeadline = selectedDate.utcEpochDateToLocalStartOfDay().run {
                        if (deadline != null) {
                            setHoursAndMinutes(deadline)
                        } else {
                            setHoursAndMinutes(0, 0)
                        }
                    }
                    onSelectedDate.invoke(targetDeadline)
                },
                content = { Text(text = stringResource(CoreRes.string.core_select_confirm_title)) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(CoreRes.string.core_cancel_title))
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp),
                    text = stringResource(CoreRes.string.core_date_picker_dialog_header),
                )
            },
            headline = {
                Text(
                    modifier = Modifier.padding(start = 24.dp),
                    text = stringResource(Res.string.todo_deadline_date_picker_headline),
                )
            },
        )
    }
}