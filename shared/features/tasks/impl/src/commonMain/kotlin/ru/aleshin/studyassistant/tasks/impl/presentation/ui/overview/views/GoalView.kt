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

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.material3.SwipeToDismissBoxValue.Settled
import androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents.Formats
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.toShortTimeString
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority.HIGH
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority.MEDIUM
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority.STANDARD
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.mappers.toMinutesOrHoursTitle
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.core.ui.views.dayMonthFormat
import ru.aleshin.studyassistant.core.ui.views.shortWeekdayDayMonthFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalTimeUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 02.06.2025.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun GoalViewItem(
    modifier: Modifier = Modifier,
    goal: GoalDetailsUi,
    onClick: () -> Unit,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
) {
    val density = LocalDensity.current
    val dismissState = remember {
        SwipeToDismissBoxState(
            initialValue = Settled,
            density = density,
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    EndToStart -> onComplete()
                    StartToEnd -> onDelete()
                    else -> {}
                }
                false
            },
            positionalThreshold = { it * .4f }
        )
    }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = goal.number.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        Row(
            modifier = modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SwipeToDismissBox(
                state = dismissState,
                modifier = modifier.weight(1f).clipToBounds(),
                backgroundContent = {
                    SwipeToDismissBackground(
                        dismissState = dismissState,
                        shape = MaterialTheme.shapes.medium,
                        endToStartContent = {
                            if (goal.isDone) {
                                Icon(imageVector = Icons.Default.Cancel, contentDescription = null)
                            } else {
                                Icon(imageVector = Icons.Default.Check, contentDescription = null)
                            }
                        },
                        startToEndContent = {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        },
                        endToStartColor = if (goal.isDone) {
                            StudyAssistantRes.colors.accents.orangeContainer
                        } else {
                            StudyAssistantRes.colors.accents.greenContainer
                        },
                        startToEndColor = StudyAssistantRes.colors.accents.redContainer,
                    )
                },
            ) {
                when (goal.contentType) {
                    GoalType.HOMEWORK -> HomeworkGoalView(
                        onClick = onClick,
                        homework = checkNotNull(goal.contentHomework),
                        time = goal.time,
                        isDone = goal.isDone,
                    )
                    GoalType.TODO -> TodoGoalView(
                        onClick = onClick,
                        todo = checkNotNull(goal.contentTodo),
                        time = goal.time,
                        isDone = goal.isDone,
                    )
                }
            }
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = Icons.Default.DragHandle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
internal fun GoalViewEmptyItem(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = TasksThemeRes.strings.goalsViewListEmptyTitle,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                style = MaterialTheme.typography.titleSmall,
            )
        }
    }
}

@Composable
internal fun HomeworkGoalView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    homework: HomeworkUi,
    time: GoalTimeUi,
    isDone: Boolean,
) {
    val subjectColor = homework.subject?.color?.let { Color(it) }
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = subjectColor?.copy(alpha = 0.1f) ?: MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(end = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.padding(vertical = 8.dp).fillMaxHeight().width(4.dp),
                shape = MaterialTheme.shapes.small,
                color = subjectColor ?: MaterialTheme.colorScheme.outline,
                content = { Box(modifier = Modifier.fillMaxHeight()) }
            )
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = homework.deadline.formatByTimeZone(
                        format = Formats.shortWeekdayDayMonthFormat(StudyAssistantRes.strings)
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    text = homework.subject?.name ?: StudyAssistantRes.strings.noneTitle,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
            GoalTimeStatus(
                time = time,
                isDone = isDone,
            )
        }
    }
}


@Composable
internal fun TodoGoalView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    todo: TodoDetailsUi,
    time: GoalTimeUi,
    isDone: Boolean,
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .padding(end = 8.dp)
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f).padding(horizontal = 8.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Column {
                    Text(
                        text = todo.priority.mapToString(StudyAssistantRes.strings),
                        color = when (todo.priority) {
                            STANDARD -> MaterialTheme.colorScheme.onSurfaceVariant
                            MEDIUM -> StudyAssistantRes.colors.accents.orange
                            HIGH -> StudyAssistantRes.colors.accents.red
                        },
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = todo.name,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    val deadlineDateFormat = Formats.dayMonthFormat(StudyAssistantRes.strings)
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(StudyAssistantRes.icons.tasksOutline),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = buildString {
                            append(TasksThemeRes.strings.untilDeadlineDateSuffix, " ")
                            append(todo.deadline?.formatByTimeZone(deadlineDateFormat))
                        },
                        maxLines = 1,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            GoalTimeStatus(
                time = time,
                isDone = isDone,
            )
        }
    }
}

@Composable
private fun GoalTimeStatus(
    modifier: Modifier = Modifier,
    time: GoalTimeUi,
    isDone: Boolean,
) {
    when (time) {
        is GoalTimeUi.Stopwatch -> if (isDone) {
            Row(
                modifier = modifier.padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = time.elapsedTime.toMinutesOrHoursTitle(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            Surface(
                modifier = modifier,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = 4.dp,
                        end = 6.dp,
                        bottom = 4.dp,
                        top = 4.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Outlined.Timer,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = time.elapsedTime.toShortTimeString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        is GoalTimeUi.Timer -> if (isDone) {
            Row(
                modifier = modifier.padding(2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = time.pastStopTime.toMinutesOrHoursTitle(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelSmall,
                )
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        } else {
            Surface(
                modifier = modifier,
                shape = MaterialTheme.shapes.small,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                Row(
                    modifier = Modifier.padding(
                        start = 4.dp,
                        end = 6.dp,
                        bottom = 4.dp,
                        top = 4.dp
                    ),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(TasksThemeRes.icons.timer),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = time.leftTime.toShortTimeString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }

        is GoalTimeUi.None -> if (isDone) {
            Icon(
                modifier = modifier.size(18.dp),
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}