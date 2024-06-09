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

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import entities.organizations.Millis
import kotlinx.datetime.Instant
import kotlinx.datetime.format
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import mappers.toMinutesOrHoursTitle
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.NumberedDurationUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.ScheduleTimeIntervalsUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes
import theme.material.full
import views.ClickableTextField
import views.DialogButtons
import views.DialogHeader
import views.dialog.BaseTimePickerDialog
import views.dialog.DurationPickerDialog

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
    var editableIntervals by remember { mutableStateOf(intervals) }

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
                        startOfClassTime = editableIntervals.firstClassTime,
                        onChangeTime = {
                            editableIntervals = editableIntervals.copy(firstClassTime = it)
                        }
                    )
                    ClassesAndBreaksIntervalsEditor(
                        intervals = editableIntervals,
                        onChangeIntervals = { editableIntervals = it },
                    )
                }
                DialogButtons(
                    confirmTitle = StudyAssistantRes.strings.saveConfirmTitle,
                    onCancelClick = onDismiss,
                    onConfirmClick = {
                        onConfirm(editableIntervals)
                    },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun StartOfClassesField(
    modifier: Modifier = Modifier,
    startOfClassTime: Instant?,
    onChangeTime: (Instant?) -> Unit,
) {
    var isOpenTimePickerDialog by remember { mutableStateOf(false) }
    val timeFormat = DateTimeComponents.Format {
        hour()
        char(':')
        minute()
    }
    val formatStartTime = startOfClassTime?.format(timeFormat)

    Box(modifier = modifier) {
        ClickableTextField(
            onClick = { isOpenTimePickerDialog = true },
            value = formatStartTime,
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
            BaseTimePickerDialog(
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
internal fun ClassesAndBreaksIntervalsEditor(
    modifier: Modifier = Modifier,
    intervals: ScheduleTimeIntervalsUi,
    onChangeIntervals: (ScheduleTimeIntervalsUi) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        ClassIntervalsEditor(
            modifier = Modifier.weight(1f),
            baseClassDuration = intervals.baseClassDuration,
            specificClassDurations = intervals.specificClassDuration,
            onChangeBaseDuration = {
                onChangeIntervals(intervals.copy(baseClassDuration = it))
            },
            onChangeSpecificDurations = {
                onChangeIntervals(intervals.copy(specificClassDuration = it))
            },
        )
        BreakIntervalsEditor(
            modifier = Modifier.weight(1f),
            baseBreakDuration = intervals.baseBreakDuration,
            specificBreakDurations = intervals.specificBreakDuration,
            onChangeBaseDuration = {
                onChangeIntervals(intervals.copy(baseBreakDuration = it))
            },
            onChangeSpecificDurations = {
                onChangeIntervals(intervals.copy(specificBreakDuration = it))
            },
        )
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun ClassIntervalsEditor(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    baseClassDuration: Millis?,
    specificClassDurations: List<NumberedDurationUi>,
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
                    painter = painterResource(EditorThemeRes.icons.classes),
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

@OptIn(ExperimentalResourceApi::class)
@Composable
internal fun BreakIntervalsEditor(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    baseBreakDuration: Millis?,
    specificBreakDurations: List<NumberedDurationUi>,
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

@Composable
internal fun NumberedDurationsList(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    baseDuration: Millis?,
    specificDurations: List<NumberedDurationUi>,
    numberedContainerColor: Color = MaterialTheme.colorScheme.primary,
    durationContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    onChangeBaseDuration: (Millis?) -> Unit,
    onChangeSpecificDurations: (List<NumberedDurationUi>) -> Unit,
) {
    Column(
        modifier = modifier.animateContentSize(spring()).heightIn(max = 252.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column {
            var isOpenDurationPickerDialog by remember { mutableStateOf(false) }
            NumberedDurationView(
                number = EditorThemeRes.strings.allTitle,
                duration = baseDuration,
                onNumberClick = null,
                onDurationClick = { isOpenDurationPickerDialog = true },
                numberedContainerColor = numberedContainerColor,
                durationContainerColor = durationContainerColor,
            )
            if (isOpenDurationPickerDialog) {
                DurationPickerDialog(
                    headerTitle = EditorThemeRes.strings.durationTitle,
                    duration = baseDuration,
                    onDismiss = { isOpenDurationPickerDialog = false },
                    onSelectedDuration = {
                        onChangeBaseDuration(it)
                        isOpenDurationPickerDialog = false
                    }
                )
            }
        }
        ExceptLine()
        specificDurations.sortedBy { it.number }.forEach { numberedDuration ->
            Column {
                var isOpenNumberChooserMenu by remember { mutableStateOf(false) }
                var isOpenDurationPickerDialog by remember { mutableStateOf(false) }
                NumberedDurationView(
                    number = numberedDuration.number.toString(),
                    duration = numberedDuration.duration,
                    onNumberClick = { isOpenNumberChooserMenu = true },
                    onDurationClick = { isOpenDurationPickerDialog = true },
                    numberedContainerColor = numberedContainerColor,
                    durationContainerColor = durationContainerColor,
                )
                if (isOpenDurationPickerDialog) {
                    DurationPickerDialog(
                        headerTitle = EditorThemeRes.strings.durationTitle,
                        duration = numberedDuration.duration,
                        onDismiss = { isOpenDurationPickerDialog = false },
                        onSelectedDuration = {
                            val updatedDurations = specificDurations.toMutableList().apply {
                                set(indexOf(numberedDuration), numberedDuration.copy(duration = it))
                            }
                            onChangeSpecificDurations(updatedDurations)
                            isOpenDurationPickerDialog = false
                        }
                    )
                }
                NumberDropdownMenu(
                    expanded = isOpenNumberChooserMenu,
                    enabled = { number -> specificDurations.find { it.number == number } == null },
                    currentNumber = numberedDuration.number,
                    onDismiss = { isOpenNumberChooserMenu = false },
                    onConfirm = {
                        val updatedDurations = specificDurations.toMutableList().apply {
                            set(indexOf(numberedDuration), numberedDuration.copy(number = it))
                        }
                        onChangeSpecificDurations(updatedDurations)
                        isOpenNumberChooserMenu = false
                    },
                )
            }
        }
        Column {
            var isOpenNumberedDurationCreatorMenu by remember { mutableStateOf(false) }
            NumberedDurationCreator(
                onClick = { isOpenNumberedDurationCreatorMenu = true },
            )
            NumberedDurationCreatorDropdownMenu(
                expanded = isOpenNumberedDurationCreatorMenu,
                specificDurations = specificDurations,
                onDismiss = { isOpenNumberedDurationCreatorMenu = false },
                onCreate = { numberedDuration ->
                    val updatedDurations = specificDurations.toMutableList().apply {
                        if (specificDurations.find { it.number == numberedDuration.number } == null) {
                            add(numberedDuration)
                        } else {
                            set(indexOfFirst { it.number == numberedDuration.number }, numberedDuration)
                        }
                    }
                    onChangeSpecificDurations(updatedDurations)
                    isOpenNumberedDurationCreatorMenu = false
                },
            )
        }
    }
}

@Composable
internal fun NumberedDurationView(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    number: String,
    duration: Millis?,
    onNumberClick: (() -> Unit)?,
    onDurationClick: (() -> Unit)?,
    numberedContainerColor: Color = MaterialTheme.colorScheme.primary,
    durationContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .animateContentSize()
                .height(32.dp)
                .clip(RoundedCornerShape(100.dp, 0.dp, 0.dp, 100.dp))
                .background(numberedContainerColor)
                .clickable(
                    enabled = enabled && onNumberClick != null,
                    onClick = { if (onNumberClick != null) onNumberClick() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = number,
                color = MaterialTheme.colorScheme.contentColorFor(numberedContainerColor),
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Box(
            modifier = Modifier
                .height(32.dp)
                .weight(1f)
                .clip(RoundedCornerShape(0.dp, 100.dp, 100.dp, 0.dp))
                .background(durationContainerColor)
                .clickable(
                    enabled = enabled && onDurationClick != null,
                    onClick = { if (onDurationClick != null) onDurationClick() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (duration != null) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = duration.toMinutesOrHoursTitle(),
                    color = MaterialTheme.colorScheme.contentColorFor(durationContainerColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = StudyAssistantRes.strings.specifyTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
internal fun NumberedDurationCreator(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(28.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.full(),
        color = containerColor,
        interactionSource = interactionSource,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = EditorThemeRes.strings.addTitle,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
internal fun ExceptLine(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(horizontal = 4.dp),
) {
    Row(
        modifier = modifier.padding(paddingValues),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = EditorThemeRes.strings.exceptTitle,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodySmall,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}
