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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import models.StandardDuration
import ru.aleshin.studyassistant.core.common.extensions.hoursToMillis
import ru.aleshin.studyassistant.core.common.extensions.minutesToMillis
import ru.aleshin.studyassistant.core.common.extensions.toHorses
import ru.aleshin.studyassistant.core.common.extensions.toMinutesInHours
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.mappers.toMinutesOrHoursTitle
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 26.05.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun DurationPickerDialog(
    modifier: Modifier = Modifier,
    headerTitle: String,
    duration: Millis?,
    onDismiss: () -> Unit,
    onSelectedDuration: (Millis) -> Unit,
) {
    var hour by rememberSaveable { mutableStateOf<Int?>(duration?.toHorses()?.toInt()) }
    var minute by rememberSaveable { mutableStateOf<Int?>(duration?.toMinutesInHours()?.toInt()) }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = modifier.width(243.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(20.dp),
                horizontalAlignment = Alignment.End,
            ) {
                TimePickerHeader(title = headerTitle)
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DurationPickerHourMinuteSelector(
                        hours = hour?.toString() ?: "",
                        minutes = minute?.toString() ?: "",
                        isEnableSupportText = true,
                        onMinutesChanges = { value ->
                            if (value.isEmpty()) {
                                hour = null
                            } else if (value.toIntOrNull() != null && value.length <= 2) {
                                hour = value.toIntOrNull()
                            }
                        },
                        onHoursChanges = { value ->
                            if (value.isEmpty()) {
                                minute = null
                            } else if (value.toIntOrNull() != null && value.length <= 2) {
                                minute = value.toIntOrNull()
                            }
                        },
                    )
                    LazyRow(
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(StandardDuration.entries.toTypedArray()) {
                            AssistChip(
                                onClick = {
                                    hour = it.hour
                                    minute = it.minute
                                },
                                label = {
                                    val millis = it.hour.hoursToMillis() + it.minute.minutesToMillis()
                                    Text(text = millis.toMinutesOrHoursTitle())
                                },
                                border = AssistChipDefaults.assistChipBorder(
                                    enabled = true,
                                    borderColor = MaterialTheme.colorScheme.outlineVariant,
                                ),
                            )
                        }
                    }
                }
                TimePickerActions(
                    enabled = hour != null && minute != null && hour!! < 24,
                    onDismiss = onDismiss,
                    onConfirm = {
                        val hoursInMillis = checkNotNull(hour).hoursToMillis()
                        val time = hoursInMillis + checkNotNull(minute).minutesToMillis()
                        onSelectedDuration.invoke(time)
                    },
                )
            }
        }
    }
}

@Composable
internal fun DurationPickerHourMinuteSelector(
    modifier: Modifier = Modifier,
    hours: String,
    minutes: String,
    isEnableSupportText: Boolean = false,
    onMinutesChanges: (String) -> Unit,
    onHoursChanges: (String) -> Unit,
) = Row(
    modifier = modifier.padding(horizontal = 24.dp),
    verticalAlignment = Alignment.CenterVertically,
) {
    val requester = remember { FocusRequester() }
    OutlinedTextField(
        modifier = Modifier.weight(1f),
        value = hours,
        textStyle = MaterialTheme.typography.displayMedium.copy(textAlign = TextAlign.Center),
        onValueChange = { value ->
            onMinutesChanges(value)
            if (value.length == 2 && value.toIntOrNull() in 0..23) requester.requestFocus()
        },
        shape = MaterialTheme.shapes.small,
        supportingText = if (isEnableSupportText) { {
            Text(StudyAssistantRes.strings.hoursTitle)
        } } else {
            null
        },
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
        value = minutes,
        textStyle = MaterialTheme.typography.displayMedium.copy(textAlign = TextAlign.Center),
        onValueChange = onHoursChanges,
        shape = MaterialTheme.shapes.small,
        supportingText = if (isEnableSupportText) { {
            Text(StudyAssistantRes.strings.minutesTitle)
        } } else {
            null
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    )
}
