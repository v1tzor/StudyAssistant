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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ClickableTextField
import ru.aleshin.studyassistant.core.ui.views.DialogButtons
import ru.aleshin.studyassistant.core.ui.views.DialogHeader
import ru.aleshin.studyassistant.core.ui.views.dialog.TimePickerDialog
import ru.aleshin.studyassistant.core.ui.views.timeFormat
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.NumberedDurationUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.ScheduleTimeIntervalsUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.NumberedDurationsList

/**
 * @author Stanislav Aleshin on 26.05.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ScheduleIntervalsDialog(
    modifier: Modifier = Modifier,
    intervals: ScheduleTimeIntervalsUi,
    organization: String,
    onDismiss: () -> Unit,
    onConfirm: (ScheduleTimeIntervalsUi) -> Unit,
) {
    val editableIntervals = remember { mutableStateOf(intervals) }

    BasicAlertDialog(onDismissRequest = onDismiss, modifier = modifier) {
        Surface(
            modifier = Modifier.width(350.dp).wrapContentHeight(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
            shadowElevation = 4.dp,
        ) {
            Column {
                DialogHeader(
                    header = EditorThemeRes.strings.scheduleIntervalsDialogHeader,
                    title = organization,
                    titleColor = MaterialTheme.colorScheme.primary,
                )
                HorizontalDivider()
                Column(
                    modifier = Modifier.padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    StartOfClassesField(
                        modifier = Modifier.padding(top = 12.dp),
                        startOfClassTime = editableIntervals.value.firstClassTime,
                        onChangeTime = {
                            editableIntervals.value = editableIntervals.value.copy(firstClassTime = it)
                        }
                    )
                    ClassesAndBreaksIntervalsView(
                        baseClassDuration = editableIntervals.value.baseClassDuration,
                        baseBreakDuration = editableIntervals.value.baseBreakDuration,
                        specificClassDuration = remember {
                            derivedStateOf { editableIntervals.value.specificClassDuration }
                        },
                        specificBreakDuration = remember {
                            derivedStateOf { editableIntervals.value.specificBreakDuration }
                        },
                        onChangeBaseClassDuration = {
                            editableIntervals.value = editableIntervals.value.copy(baseClassDuration = it)
                        },
                        onChangeSpecificClassDurations = {
                            editableIntervals.value = editableIntervals.value.copy(specificClassDuration = it)
                        },
                        onChangeBaseBreakDuration = {
                            editableIntervals.value = editableIntervals.value.copy(baseBreakDuration = it)
                        },
                        onChangeSpecificBreakDurations = {
                            editableIntervals.value = editableIntervals.value.copy(specificBreakDuration = it)
                        },
                    )
                }
                DialogButtons(
                    confirmTitle = StudyAssistantRes.strings.saveConfirmTitle,
                    onCancelClick = onDismiss,
                    onConfirmClick = {
                        onConfirm(editableIntervals.value)
                    },
                )
            }
        }
    }
}

@Composable
internal fun StartOfClassesField(
    modifier: Modifier = Modifier,
    startOfClassTime: Instant?,
    onChangeTime: (Instant?) -> Unit,
) {
    var isOpenTimePickerDialog by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        ClickableTextField(
            onClick = { isOpenTimePickerDialog = true },
            value = startOfClassTime?.formatByTimeZone(DateTimeComponents.Formats.timeFormat()),
            label = EditorThemeRes.strings.startOfClassesTitle,
            placeholder = EditorThemeRes.strings.startOfClassesPlaceholder,
            leadingIcon = {
                Icon(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(StudyAssistantRes.icons.timeOutline),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
        )

        if (isOpenTimePickerDialog) {
            TimePickerDialog(
                initTime = startOfClassTime,
                onDismiss = { isOpenTimePickerDialog = false },
                onConfirmTime = {
                    onChangeTime(it)
                    isOpenTimePickerDialog = false
                },
            )
        }
    }
}

@Composable
private fun ClassesAndBreaksIntervalsView(
    modifier: Modifier = Modifier,
    baseClassDuration: Millis?,
    baseBreakDuration: Millis?,
    specificClassDuration: State<List<NumberedDurationUi>>,
    specificBreakDuration: State<List<NumberedDurationUi>>,
    onChangeBaseClassDuration: (Millis?) -> Unit,
    onChangeSpecificClassDurations: (List<NumberedDurationUi>) -> Unit,
    onChangeBaseBreakDuration: (Millis?) -> Unit,
    onChangeSpecificBreakDurations: (List<NumberedDurationUi>) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ClassIntervalsView(
            modifier = Modifier.weight(1f),
            baseClassDuration = baseClassDuration,
            specificClassDurations = specificClassDuration,
            onChangeBaseDuration = onChangeBaseClassDuration,
            onChangeSpecificDurations = onChangeSpecificClassDurations,
        )
        BreakIntervalsView(
            modifier = Modifier.weight(1f),
            baseBreakDuration = baseBreakDuration,
            specificBreakDurations = specificBreakDuration,
            onChangeBaseDuration = onChangeBaseBreakDuration,
            onChangeSpecificDurations = onChangeSpecificBreakDurations,
        )
    }
}

@Composable
private fun ClassIntervalsView(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    baseClassDuration: Millis?,
    specificClassDurations: State<List<NumberedDurationUi>>,
    onChangeBaseDuration: (Millis?) -> Unit,
    onChangeSpecificDurations: (List<NumberedDurationUi>) -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(StudyAssistantRes.icons.classes),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = EditorThemeRes.strings.classesTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            NumberedDurationsList(
                scrollState = scrollState,
                baseDuration = baseClassDuration,
                specificDurations = specificClassDurations,
                onChangeBaseDuration = onChangeBaseDuration,
                onChangeSpecificDurations = onChangeSpecificDurations
            )
        }
    }
}

@Composable
private fun BreakIntervalsView(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    baseBreakDuration: Millis?,
    specificBreakDurations: State<List<NumberedDurationUi>>,
    onChangeBaseDuration: (Millis?) -> Unit,
    onChangeSpecificDurations: (List<NumberedDurationUi>) -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(EditorThemeRes.icons.breaks),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = EditorThemeRes.strings.breaksTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            NumberedDurationsList(
                scrollState = scrollState,
                baseDuration = baseBreakDuration,
                specificDurations = specificBreakDurations,
                onChangeBaseDuration = onChangeBaseDuration,
                onChangeSpecificDurations = onChangeSpecificDurations,
                numberedContainerColor = MaterialTheme.colorScheme.tertiary,
                durationContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            )
        }
    }
}