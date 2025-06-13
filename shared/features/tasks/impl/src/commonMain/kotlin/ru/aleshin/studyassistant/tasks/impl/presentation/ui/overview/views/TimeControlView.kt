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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SettingsBackupRestore
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.toShortTimeString
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.mappers.toMinutesAndHoursTitle
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.dialog.DurationPickerDialog
import ru.aleshin.studyassistant.core.ui.views.menu.ChooserDropdownMenu
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToString
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalTimeDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 05.06.2025.
 */
@Composable
internal fun TimeControlSection(
    modifier: Modifier = Modifier,
    goal: GoalDetailsUi,
    onStartTime: () -> Unit,
    onPauseTime: () -> Unit,
    onReset: () -> Unit,
    onChangeTimeType: (GoalTime.Type) -> Unit,
    onChangeDesiredTime: (Millis?) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.width(IntrinsicSize.Max),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            DesiredTimeView(
                modifier = Modifier.fillMaxWidth(),
                enabled = !goal.isDone,
                desiredTime = goal.desiredTime,
                onChange = { onChangeDesiredTime(it) },
            )
            RealTimeView(
                modifier = Modifier.fillMaxWidth(),
                desiredTime = goal.desiredTime,
                realTime = goal.time.realElapsedTime,
                isActive = goal.time.activeStatus,
            )
        }
        VerticalDivider()
        TimeControlContent(
            modifier = Modifier.weight(1f),
            goalTime = goal.time,
            isDone = goal.isDone,
            onReset = onReset,
            onPauseTime = onPauseTime,
            onStartTime = onStartTime,
            onChangeTimeType = onChangeTimeType,
        )
    }
}

@Composable
private fun DesiredTimeView(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    desiredTime: Millis?,
    onChange: (Millis?) -> Unit,
) {
    var durationPickerState by rememberSaveable { mutableStateOf(false) }

    Surface(
        onClick = { durationPickerState = true },
        enabled = enabled,
        modifier = modifier.animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            Column {
                Text(
                    text = TasksThemeRes.strings.goalSheetDesiredTimeLabel,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = (desiredTime ?: 0).toMinutesAndHoursTitle(),
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }
            if (enabled) {
                Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            } else {
                Box(modifier = Modifier.size(24.dp))
            }
        }
    }

    if (durationPickerState) {
        DurationPickerDialog(
            headerTitle = TasksThemeRes.strings.goalSheetDesiredTimePickerTitle,
            duration = desiredTime,
            onDismiss = { durationPickerState = false },
            onSelectedDuration = {
                onChange(it.takeIf { it > 0 })
                durationPickerState = false
            },
        )
    }
}

@Composable
private fun RealTimeView(
    modifier: Modifier = Modifier,
    desiredTime: Millis?,
    realTime: Millis?,
    isActive: Boolean,
) {
    Surface(
        modifier = modifier.animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = TasksThemeRes.strings.goalSheetRealTimeLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = (realTime ?: 0).toMinutesAndHoursTitle(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isActive) {
                    Icon(
                        modifier = Modifier.size(24.dp),
                        painter = painterResource(TasksThemeRes.icons.inProgress),
                        contentDescription = null,
                        tint = StudyAssistantRes.colors.accents.orange,
                    )
                } else if (realTime != null && desiredTime != null) {
                    if (realTime <= desiredTime) {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(TasksThemeRes.icons.fasterThenDesired),
                            contentDescription = null,
                            tint = StudyAssistantRes.colors.accents.green,
                        )
                    } else {
                        Icon(
                            modifier = Modifier.size(24.dp),
                            painter = painterResource(TasksThemeRes.icons.slowerThenDesired),
                            contentDescription = null,
                            tint = StudyAssistantRes.colors.accents.red,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeControlContent(
    modifier: Modifier = Modifier,
    goalTime: GoalTimeDetailsUi,
    isDone: Boolean,
    onStartTime: () -> Unit,
    onPauseTime: () -> Unit,
    onReset: () -> Unit,
    onChangeTimeType: (GoalTime.Type) -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        var isOpenTimeTypeChooserMenu by rememberSaveable { mutableStateOf(false) }
        Box {
            AssistChip(
                onClick = { isOpenTimeTypeChooserMenu = true },
                enabled = !isDone,
                modifier = Modifier.height(32.dp).fillMaxWidth(),
                label = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = goalTime.type.mapToString(),
                        textAlign = TextAlign.Center,
                    )
                },
                leadingIcon = {
                    when (goalTime.type) {
                        GoalTime.Type.TIMER -> Icon(
                            painter = painterResource(TasksThemeRes.icons.timer),
                            contentDescription = null,
                        )

                        GoalTime.Type.STOPWATCH -> Icon(
                            imageVector = Icons.Outlined.Timer,
                            contentDescription = null,
                        )

                        GoalTime.Type.NONE -> Icon(
                            imageVector = Icons.Outlined.Schedule,
                            contentDescription = null,
                        )
                    }
                },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.ArrowDropDown,
                        contentDescription = null,
                    )
                },
            )
            GoalTimeTypeChooserMenu(
                expanded = isOpenTimeTypeChooserMenu,
                selected = goalTime.type,
                onDismiss = { isOpenTimeTypeChooserMenu = false },
                onChoose = {
                    onChangeTimeType(it)
                    isOpenTimeTypeChooserMenu = false
                }
            )
        }
        TimerStopwatchView(
            goalTime = goalTime,
            isDone = isDone,
            onReset = onReset,
        )
        StartStopTimeButton(
            modifier = Modifier.fillMaxWidth(),
            enabled = when (goalTime) {
                is GoalTimeDetailsUi.None -> false
                is GoalTimeDetailsUi.Stopwatch -> !isDone
                is GoalTimeDetailsUi.Timer -> !isDone && goalTime.targetTime != 0L
            },
            isActive = goalTime.activeStatus,
            onStartTime = onStartTime,
            onPauseTime = onPauseTime,
        )
    }
}

@Composable
private fun GoalTimeTypeChooserMenu(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    selected: GoalTime.Type,
    onDismiss: () -> Unit,
    onChoose: (GoalTime.Type) -> Unit,
) {
    ChooserDropdownMenu(
        modifier = modifier,
        expanded = expanded,
        items = GoalTime.Type.entries,
        enabledItem = { it != selected },
        showBackItem = false,
        text = { action ->
            Text(
                text = action.mapToString(),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        },
        leadingIcon = { action ->
            when (action) {
                GoalTime.Type.TIMER -> Icon(
                    painter = painterResource(TasksThemeRes.icons.timer),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )

                GoalTime.Type.STOPWATCH -> Icon(
                    imageVector = Icons.Outlined.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )

                GoalTime.Type.NONE -> Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        onDismiss = onDismiss,
        onChoose = onChoose,
    )
}

@Composable
private fun TimerStopwatchView(
    modifier: Modifier = Modifier,
    goalTime: GoalTimeDetailsUi,
    isDone: Boolean,
    onReset: () -> Unit,
) {
    val progress by animateFloatAsState(
        targetValue = when (goalTime) {
            is GoalTimeDetailsUi.None -> 0f
            is GoalTimeDetailsUi.Stopwatch -> goalTime.progress ?: 0f
            is GoalTimeDetailsUi.Timer -> goalTime.progress
        }
    )
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                progress = { progress },
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.primaryContainer,
                strokeWidth = 4.dp,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = when (goalTime.type) {
                            GoalTime.Type.TIMER -> TasksThemeRes.strings.goalSheetLeftTimeLabel
                            GoalTime.Type.STOPWATCH -> TasksThemeRes.strings.goalSheetElapsedTimeLabel
                            GoalTime.Type.NONE -> TasksThemeRes.strings.goalSheetLeftTimeLabel
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = when (goalTime) {
                            is GoalTimeDetailsUi.None -> 0L.toShortTimeString()
                            is GoalTimeDetailsUi.Stopwatch -> goalTime.elapsedTime.toShortTimeString()
                            is GoalTimeDetailsUi.Timer -> goalTime.leftTime.toShortTimeString()
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
                if (!isDone) {
                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.size(32.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Default.SettingsBackupRestore,
                            contentDescription = null,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StartStopTimeButton(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    isActive: Boolean,
    onStartTime: () -> Unit,
    onPauseTime: () -> Unit,
) {
    Button(
        enabled = enabled,
        onClick = { if (isActive) onPauseTime() else onStartTime() },
        modifier = modifier.height(32.dp),
        colors = when (isActive) {
            true -> ButtonDefaults.buttonColors(
                containerColor = StudyAssistantRes.colors.accents.orange,
                contentColor = StudyAssistantRes.colors.accents.onOrange,
            )
            false -> ButtonDefaults.buttonColors(
                containerColor = StudyAssistantRes.colors.accents.green,
                contentColor = StudyAssistantRes.colors.accents.onGreen,
            )
        },
    ) {
        if (isActive) {
            Icon(imageVector = Icons.Default.Pause, contentDescription = null)
        } else {
            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
        }
    }
}