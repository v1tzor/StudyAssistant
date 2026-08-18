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
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.utcEpochDateToLocalStartOfDay
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.cancel_title as core_cancel_title
import ru.aleshin.studyassistant.core.ui.resources.date_picker_dialog_header as core_date_picker_dialog_header
import ru.aleshin.studyassistant.core.ui.resources.select_confirm_title as core_select_confirm_title

/**
 * @author Stanislav Aleshin on 09.08.2023.
 */
@Composable
@ExperimentalMaterial3Api
fun BaseDatePickerDialog(
    modifier: Modifier = Modifier,
    state: DatePickerState = rememberDatePickerState(),
    headline: String = stringResource(CoreRes.string.core_date_picker_dialog_header),
    title: String = stringResource(CoreRes.string.core_date_picker_dialog_header),
    onDismiss: () -> Unit,
    onConfirmDate: (Instant?) -> Unit,
) {
    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirmDate(state.selectedDateMillis?.utcEpochDateToLocalStartOfDay())
                }
            ) {
                Text(
                    text = stringResource(CoreRes.string.core_select_confirm_title),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = stringResource(CoreRes.string.core_cancel_title),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    ) {
        DatePicker(
            state = state,
            headline = {
                Text(
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, bottom = 12.dp),
                    text = headline
                )
            },
            title = {
                Text(
                    modifier = Modifier.padding(start = 24.dp, end = 12.dp, top = 16.dp),
                    text = title
                )
            }
        )
    }
}
