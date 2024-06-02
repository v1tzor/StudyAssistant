/*
 * Copyright 2023 Stanislav Aleshin
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
 * imitations under the License.
 */

package views

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
import extensions.mapEpochTimeToInstant
import extensions.startThisDay
import kotlinx.datetime.Instant
import theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 09.08.2023.
 */
@Composable
@ExperimentalMaterial3Api
fun BaseDatePickerDialog(
    modifier: Modifier = Modifier,
    state: DatePickerState = rememberDatePickerState(),
    headline: String = StudyAssistantRes.strings.datePickerDialogHeader,
    title: String = StudyAssistantRes.strings.datePickerDialogHeader,
    onDismiss: () -> Unit,
    onConfirmDate: (Instant?) -> Unit,
) {
    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { onConfirmDate(state.selectedDateMillis?.mapEpochTimeToInstant()?.startThisDay()) }
            ) {
                Text(
                    text = StudyAssistantRes.strings.selectConfirmTitle,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = StudyAssistantRes.strings.cancelTitle,
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
