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
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.dialog.TimePickerDialog
import ru.aleshin.studyassistant.core.ui.views.shortWeekdayDayMonthFormat
import ru.aleshin.studyassistant.core.ui.views.timeFormat
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

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
                format = DateTimeComponents.Formats.shortWeekdayDayMonthFormat(StudyAssistantRes.strings)
            ),
            infoIcon = painterResource(StudyAssistantRes.icons.calendarToday),
            label = EditorThemeRes.strings.todoDeadlineFieldLabel,
            placeholder = EditorThemeRes.strings.todoDeadlineFieldPlaceholder,
            trailingIcon = {
                Icon(
                    painter = painterResource(StudyAssistantRes.icons.selectDate),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        )

        ClickableInfoTextField(
            onClick = { timePickerState = true },
            enabled = !isLoading,
            value = deadline?.formatByTimeZone(DateTimeComponents.Formats.timeFormat()),
            infoIcon = painterResource(StudyAssistantRes.icons.timeOutline),
            label = EditorThemeRes.strings.todoTimeFieldLabel,
            placeholder = EditorThemeRes.strings.todoTimeFieldPlaceholder,
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
        initialSelectedDateMillis = deadline?.toEpochMilliseconds()
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
                    val targetDeadline = selectedDate.mapEpochTimeToInstant().run {
                        if (deadline != null) {
                            setHoursAndMinutes(deadline)
                        } else {
                            setHoursAndMinutes(0, 0)
                        }
                    }
                    onSelectedDate.invoke(targetDeadline)
                },
                content = { Text(text = StudyAssistantRes.strings.selectConfirmTitle) }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = StudyAssistantRes.strings.cancelTitle)
            }
        },
    ) {
        DatePicker(
            state = datePickerState,
            title = {
                Text(
                    modifier = Modifier.padding(start = 24.dp, top = 24.dp),
                    text = StudyAssistantRes.strings.datePickerDialogHeader,
                )
            },
            headline = {
                Text(
                    modifier = Modifier.padding(start = 24.dp),
                    text = EditorThemeRes.strings.todoDeadlineDatePickerHeadline,
                )
            },
        )
    }
}