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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.ButtonDefaults.ContentPadding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.toInstant
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.currentScreenSize

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TimePickerDialog(
    modifier: Modifier = Modifier,
    initTime: Instant?,
    timeRestriction: LocalTime? = null,
    header: String = StudyAssistantRes.strings.timePickerDialogHeader,
    showCurrentTimeSelector: Boolean = false,
    onDismiss: () -> Unit,
    onConfirmTime: (Instant) -> Unit,
) {
    val pickerState = rememberTimePickerState(
        initialHour = initTime?.dateTime()?.hour ?: 0,
        initialMinute = initTime?.dateTime()?.minute ?: 0,
    )
    var showingPicker by rememberSaveable { mutableStateOf(false) }
    val localTime = remember(pickerState.hour, pickerState.minute) {
        LocalTime(pickerState.hour, pickerState.minute)
    }
    val configuration = currentScreenSize()

    BasicTimePickerDialog(
        modifier = modifier,
        enabledConfirm = if (timeRestriction != null) localTime <= timeRestriction else true,
        showCurrentTimeSelector = showCurrentTimeSelector,
        title = header,
        onCancel = onDismiss,
        onConfirm = {
            val time = LocalTime(
                hour = pickerState.hour,
                minute = pickerState.minute,
                second = 0,
                nanosecond = 0,
            )
            val dateTime = time.atDate((initTime ?: Clock.System.now()).dateTime().date)
            onConfirmTime.invoke(dateTime.toInstant(TimeZone.currentSystemDefault()))
        },
        onCurrentTimeChoose = {
            val currentDateTime = Clock.System.now().dateTime()
            pickerState.hour = currentDateTime.hour + if (currentDateTime.minute == 59) 1 else 0
            pickerState.minute = if (currentDateTime.minute != 59) currentDateTime.minute + 1 else 0
        },
        toggle = {
            if (configuration.height > 400.dp) {
                IconButton(
                    modifier = Modifier.size(36.dp),
                    onClick = { showingPicker = !showingPicker }
                ) {
                    Icon(
                        imageVector = if (showingPicker) {
                            Icons.Default.Keyboard
                        } else {
                            Icons.Default.Timer
                        },
                        contentDescription = null,
                    )
                }
            }
        }
    ) {
        if (showingPicker && configuration.height > 400.dp) {
            TimePicker(state = pickerState)
        } else {
            TimeInput(state = pickerState)
        }
    }
}

@Composable
fun BasicTimePickerDialog(
    modifier: Modifier = Modifier,
    enabledConfirm: Boolean,
    showCurrentTimeSelector: Boolean,
    title: String,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    onCurrentTimeChoose: () -> Unit,
    toggle: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
    Dialog(
        onDismissRequest = onCancel,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = modifier
                .width(IntrinsicSize.Min)
                .height(IntrinsicSize.Min),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(top = 24.dp, bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
                    text = title,
                    style = MaterialTheme.typography.labelMedium
                )
                Box(modifier = Modifier.padding(horizontal = 24.dp)) { content() }
                TimePickerActions(
                    modifier = Modifier.fillMaxWidth(),
                    enabledConfirm = enabledConfirm,
                    showCurrentTimeSelector = showCurrentTimeSelector,
                    paddingValues = PaddingValues(start = 16.dp, end = 8.dp),
                    onDismissClick = onCancel,
                    toggle = toggle,
                    onCurrentTimeChoose = onCurrentTimeChoose,
                    onConfirmClick = onConfirm
                )
            }
        }
    }
}

@Composable
internal fun TimePickerActions(
    modifier: Modifier = Modifier,
    enabledConfirm: Boolean = true,
    showCurrentTimeSelector: Boolean = true,
    paddingValues: PaddingValues = PaddingValues(bottom = 20.dp, start = 16.dp, end = 24.dp),
    toggle: @Composable () -> Unit = {},
    onDismissClick: () -> Unit,
    onCurrentTimeChoose: () -> Unit,
    onConfirmClick: () -> Unit,
) = Row(
    modifier = modifier.padding(paddingValues),
    verticalAlignment = Alignment.CenterVertically,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        toggle()
        if (showCurrentTimeSelector) {
            IconButton(
                modifier = Modifier.size(36.dp),
                onClick = onCurrentTimeChoose
            ) {
                Icon(
                    painter = painterResource(StudyAssistantRes.icons.timeOutline),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
    Spacer(modifier = Modifier.weight(1f))
    TextButton(
        onClick = onDismissClick,
        contentPadding = PaddingValues(
            start = 6.dp,
            top = ContentPadding.calculateTopPadding(),
            end = 6.dp,
            bottom = ContentPadding.calculateBottomPadding()
        ),
    ) {
        Text(text = StudyAssistantRes.strings.cancelTitle)
    }
    TextButton(enabled = enabledConfirm, onClick = onConfirmClick) {
        Text(text = StudyAssistantRes.strings.okConfirmTitle)
    }
}