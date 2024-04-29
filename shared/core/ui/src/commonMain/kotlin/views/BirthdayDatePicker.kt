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

package views

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import extensions.mapEpochTimeToInstant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import theme.StudyAssistantRes
import theme.tokens.monthNames

/**
 * @author Stanislav Aleshin on 27.04.2024
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BirthdayDatePicker(
    modifier: Modifier = Modifier,
    isOpenDialog: Boolean,
    label: String,
    onDismiss: () -> Unit,
    onSelectedDate: (String) -> Unit,
) {
    if (isOpenDialog) {
        val datePickerState = rememberDatePickerState()
        val confirmEnabled by remember {
            derivedStateOf { datePickerState.selectedDateMillis != null }
        }
        DatePickerDialog(
            modifier = modifier,
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    enabled = confirmEnabled,
                    onClick = {
                        val selectedDate = datePickerState.selectedDateMillis ?: return@TextButton
                        val dateFormat = DateTimeComponents.Format {
                            dayOfMonth(); char('.'); monthNumber(); char('.'); year()
                        }
                        val birthday = selectedDate.mapEpochTimeToInstant().format(dateFormat)
                        onSelectedDate.invoke(birthday)
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
                        text = label,
                    )
                },
            )
        }
    }
}