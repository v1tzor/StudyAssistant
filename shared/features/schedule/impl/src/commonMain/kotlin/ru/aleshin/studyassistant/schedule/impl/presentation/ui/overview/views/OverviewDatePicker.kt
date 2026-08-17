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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.date_picker_cancel
import ru.aleshin.studyassistant.schedule.impl.resources.date_picker_confirm

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OverviewDatePicker(
    modifier: Modifier = Modifier,
    selectedDate: Instant?,
    onDismiss: () -> Unit,
    onDateSelect: (Instant) -> Unit,
) {
    val pickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toPickerMillis(),
    )
    DatePickerDialog(
        modifier = modifier,
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                enabled = pickerState.selectedDateMillis != null,
                onClick = {
                    val selectedMillis = pickerState.selectedDateMillis ?: return@TextButton
                    onDateSelect(selectedMillis.fromPickerMillis())
                    onDismiss()
                },
            ) {
                Text(stringResource(Res.string.date_picker_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.date_picker_cancel))
            }
        },
    ) {
        DatePicker(
            state = pickerState,
            showModeToggle = false,
        )
    }
}

private fun Instant.toPickerMillis(): Long {
    return toLocalDateTime(TimeZone.currentSystemDefault()).date
        .atStartOfDayIn(TimeZone.UTC)
        .toEpochMilliseconds()
}

private fun Long.fromPickerMillis(): Instant {
    return Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)
        .date
        .atStartOfDayIn(TimeZone.currentSystemDefault())
}
