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

package ru.aleshin.studyassistant.core.ui.views.dialog

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
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.utcEpochDateToLocalStartOfDay
import ru.aleshin.studyassistant.core.ui.views.dayMonthYearFormat
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.cancel_title as core_cancel_title
import ru.aleshin.studyassistant.core.ui.resources.date_picker_dialog_header as core_date_picker_dialog_header
import ru.aleshin.studyassistant.core.ui.resources.select_confirm_title as core_select_confirm_title

/**
 * @author Stanislav Aleshin on 27.04.2024
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BirthdayDatePicker(
    modifier: Modifier = Modifier,
    label: String,
    onDismiss: () -> Unit,
    onSelectedDate: (String) -> Unit,
) {
    val datePickerState = rememberDatePickerState()
    val confirmEnabled by remember { derivedStateOf { datePickerState.selectedDateMillis != null } }

    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = confirmEnabled,
                onClick = {
                    val selectedDate = datePickerState.selectedDateMillis ?: return@TextButton
                    val dateFormat = DateTimeComponents.Formats.dayMonthYearFormat()
                    val birthday = selectedDate.utcEpochDateToLocalStartOfDay().formatByTimeZone(dateFormat)
                    onSelectedDate.invoke(birthday)
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
                    text = label,
                )
            },
        )
    }
}