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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import extensions.formatByTimeZone
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import views.ClickableTextField
import views.ExpandedIcon
import views.dialog.BaseTimePickerDialog

/**
 * @author Stanislav Aleshin on 06.06.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun WorkTimeInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    startTime: Instant?,
    endTime: Instant?,
    onSelected: (Instant?, Instant?) -> Unit,
) {
    var isOpenStartTimePicker by remember { mutableStateOf(false) }
    var isOpenEndTimePicker by remember { mutableStateOf(false) }

    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Box(modifier = Modifier.height(61.dp), contentAlignment = Alignment.Center) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(EditorThemeRes.icons.workTime),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val timeFormat = DateTimeComponents.Format {
                    hour()
                    char(':')
                    minute()
                }
                ClickableTextField(
                    onClick = { isOpenStartTimePicker = true },
                    enabled = !isLoading,
                    modifier = Modifier.weight(1f),
                    value = startTime?.formatByTimeZone(timeFormat),
                    label = EditorThemeRes.strings.startTimeFieldLabel,
                    placeholder = EditorThemeRes.strings.startTimeFieldPlaceholder,
                    trailingIcon = {
                        ExpandedIcon(
                            isExpanded = isOpenStartTimePicker,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                )
                ClickableTextField(
                    modifier = Modifier.weight(1f),
                    enabled = !isLoading,
                    value = endTime?.formatByTimeZone(timeFormat),
                    label = EditorThemeRes.strings.endTimeFieldLabel,
                    placeholder = EditorThemeRes.strings.endTimeFieldPlaceholder,
                    trailingIcon = {
                        ExpandedIcon(
                            isExpanded = isOpenEndTimePicker,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    onClick = { isOpenEndTimePicker = true },
                )
            }
        }
    }

    if (isOpenStartTimePicker) {
        BaseTimePickerDialog(
            initTime = startTime,
            onDismiss = { isOpenStartTimePicker = false },
            onConfirmTime = { selectedStartTime ->
                onSelected(selectedStartTime, endTime)
                isOpenStartTimePicker = false
            },
        )
    }

    if (isOpenEndTimePicker) {
        BaseTimePickerDialog(
            initTime = endTime,
            onDismiss = { isOpenEndTimePicker = false },
            onConfirmTime = { selectedEndTime ->
                onSelected(startTime, selectedEndTime)
                isOpenEndTimePicker = false
            },
        )
    }
}
