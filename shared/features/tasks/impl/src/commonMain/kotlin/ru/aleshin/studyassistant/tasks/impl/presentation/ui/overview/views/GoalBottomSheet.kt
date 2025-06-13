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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents.Formats
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority.HIGH
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority.MEDIUM
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority.STANDARD
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.dayMonthFormat
import ru.aleshin.studyassistant.core.ui.views.sheet.MediumDragHandle
import ru.aleshin.studyassistant.core.ui.views.shortWeekdayDayMonthFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.mapToHomeworkTasks
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.DeleteGoalWarningDialog

/**
 * @author Stanislav Aleshin on 05.06.2025.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun GoalBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    goal: GoalDetailsUi,
    onOpenHomeworkEditor: (HomeworkUi) -> Unit,
    onOpenTodoEditor: (TodoUi) -> Unit,
    onStartTime: () -> Unit,
    onPauseTime: () -> Unit,
    onResetTime: () -> Unit,
    onChangeTimeType: (GoalTime.Type) -> Unit,
    onChangeDesiredTime: (Millis?) -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onDismissRequest: () -> Unit,
) = with(goal) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = { MediumDragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            when (contentType) {
                GoalType.HOMEWORK -> GoalBottomSheetHomeworkHeader(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    homework = contentHomework,
                    number = number,
                    onOpenEditor = onOpenHomeworkEditor,
                )
                GoalType.TODO -> GoalBottomSheetTodoHeader(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    todo = contentTodo,
                    number = number,
                    onOpenEditor = onOpenTodoEditor,
                )
            }
            TimeControlDivider()
            TimeControlSection(
                modifier = Modifier.padding(horizontal = 16.dp),
                goal = goal,
                onStartTime = onStartTime,
                onPauseTime = onPauseTime,
                onReset = onResetTime,
                onChangeTimeType = onChangeTimeType,
                onChangeDesiredTime = onChangeDesiredTime,
            )
            if (contentType == GoalType.HOMEWORK) {
                HomeworkTaskDivider()
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (contentHomework?.test != null) {
                        TestHomeworkView(testTopic = contentHomework.test)
                    }
                    GoalHomeworkTasksView(
                        theoreticalTasks = remember(contentHomework) {
                            contentHomework?.theoreticalTasks?.mapToHomeworkTasks()
                        },
                        practicalTasks = remember(contentHomework) {
                            contentHomework?.practicalTasks?.mapToHomeworkTasks()
                        },
                        presentationTasks = remember(contentHomework) {
                            contentHomework?.presentationTasks?.mapToHomeworkTasks()
                        },
                        priority = contentHomework?.priority,
                    )
                }
            }
            GoalBottomSheetActionFooter(
                isDone = isDone,
                onDelete = onDelete,
                onComplete = onComplete,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GoalBottomSheetHomeworkHeader(
    modifier: Modifier = Modifier,
    homework: HomeworkUi?,
    number: Int,
    onOpenEditor: (HomeworkUi) -> Unit,
) {
    val indicatorColor = homework?.subject?.color?.let { Color(it) }
    Surface(
        enabled = homework != null,
        onClick = { if (homework != null) onOpenEditor(homework) },
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        shape = MaterialTheme.shapes.large,
        color = indicatorColor?.copy(alpha = 0.1f) ?: MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.fillMaxHeight().width(4.dp).padding(vertical = 10.dp),
                shape = MaterialTheme.shapes.small,
                color = indicatorColor ?: MaterialTheme.colorScheme.outline,
                content = { Box(modifier = Modifier.fillMaxHeight()) }
            )
            Text(
                text = number.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
            )
            Row(
                modifier = Modifier.weight(1f).padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text = homework?.deadline?.formatByTimeZone(
                            format = Formats.shortWeekdayDayMonthFormat(StudyAssistantRes.strings)
                        ) ?: StudyAssistantRes.strings.noneTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        style = MaterialTheme.typography.titleSmall,
                    )
                    Text(
                        text = homework?.subject?.name ?: StudyAssistantRes.strings.noneTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 3,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                if (homework?.isDone == true) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GoalBottomSheetTodoHeader(
    modifier: Modifier = Modifier,
    todo: TodoUi?,
    number: Int,
    onOpenEditor: (TodoUi) -> Unit,
) {
    Surface(
        enabled = todo != null,
        onClick = { if (todo != null) onOpenEditor(todo) },
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = number.toString(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = todo?.priority?.mapToString(StudyAssistantRes.strings)
                            ?: StudyAssistantRes.strings.noneTitle,
                        color = when (todo?.priority) {
                            STANDARD -> MaterialTheme.colorScheme.onSurfaceVariant
                            MEDIUM -> StudyAssistantRes.colors.accents.orange
                            HIGH -> StudyAssistantRes.colors.accents.red
                            null -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = todo?.name ?: StudyAssistantRes.strings.noneTitle,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                if (todo?.isDone == true) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val deadlineDateFormat = Formats.dayMonthFormat(StudyAssistantRes.strings)
                Icon(
                    modifier = Modifier.size(18.dp),
                    painter = painterResource(TasksThemeRes.icons.deadline),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                Text(
                    text = buildString {
                        append(TasksThemeRes.strings.untilDeadlineDateSuffix, " ")
                        val deadlineTitle = todo?.deadline?.formatByTimeZone(deadlineDateFormat)
                        append(deadlineTitle ?: StudyAssistantRes.strings.noneTitle)
                    },
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun TimeControlDivider(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = TasksThemeRes.strings.goalSheetTimeControlTitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun HomeworkTaskDivider(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        HorizontalDivider(modifier = Modifier.weight(1f))
        Text(
            text = TasksThemeRes.strings.goalSheetHomeworkTasksTitle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall,
        )
        HorizontalDivider(modifier = Modifier.weight(1f))
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun GoalBottomSheetActionFooter(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDone: Boolean,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    var deleteWarningDialogState by remember { mutableStateOf(false) }
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = { deleteWarningDialogState = true },
            enabled = enabled,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.error,
            ),
        ) {
            Text(text = TasksThemeRes.strings.goalSheetDeleteActionLabel)
        }

        if (!isDone) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = onComplete,
                enabled = enabled,
            ) {
                Text(text = TasksThemeRes.strings.goalSheetCompleteActionLabel)
            }
        } else {
            FilledTonalButton(
                modifier = Modifier.weight(1f),
                onClick = onComplete,
                enabled = enabled,
            ) {
                Text(text = TasksThemeRes.strings.goalSheetCancelActionLabel)
            }
        }
    }
    if (deleteWarningDialogState) {
        DeleteGoalWarningDialog(
            onDismiss = { deleteWarningDialogState = false },
            onDelete = {
                onDelete()
                deleteWarningDialogState = false
            },
        )
    }
}