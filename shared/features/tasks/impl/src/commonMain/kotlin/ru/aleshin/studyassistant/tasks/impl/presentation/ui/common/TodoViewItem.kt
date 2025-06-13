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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.common

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents.Formats
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority.HIGH
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority.MEDIUM
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority.STANDARD
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoStatus
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.mappers.toLanguageString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.HorizontalProgressIndicator
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.dayMonthFormat
import ru.aleshin.studyassistant.core.ui.views.shortDayMonthTimeFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import kotlin.time.DurationUnit.MILLISECONDS
import kotlin.time.toDuration

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
@Composable
internal fun TodoViewItem(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    isDone: Boolean,
    todoText: String,
    description: String?,
    linkedGoal: GoalShortUi?,
    status: TodoStatus,
    deadline: Instant?,
    deadlineLeftTime: Millis?,
    progress: Float,
    priority: TaskPriority,
    completeTime: Instant?,
    onChangeDone: (Boolean) -> Unit,
    onScheduleGoal: () -> Unit,
    onDeleteGoal: () -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.width(IntrinsicSize.Min),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.width(IntrinsicSize.Min).padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            TodoViewHeader(
                modifier = Modifier.widthIn(min = 200.dp, max = 380.dp).fillMaxWidth(),
                isDone = isDone,
                enabled = enabled,
                todoText = todoText,
                priority = priority,
                onChangeDone = onChangeDone,
            )
            if (description != null) {
                Text(
                    modifier = Modifier.widthIn(min = 200.dp, max = 380.dp),
                    text = description,
                    color = MaterialTheme.colorScheme.onSurface,
                    minLines = 2,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
            TodoViewFooter(
                status = status,
                deadline = deadline,
                linkedGoal = linkedGoal,
                deadlineLeftTime = deadlineLeftTime,
                completeTime = completeTime,
                progress = progress,
                onScheduleGoal = onScheduleGoal,
                onDeleteGoal = onDeleteGoal,
            )
        }
    }
}

@Composable
internal fun TodoViewItemPlaceholder(
    modifier: Modifier = Modifier,
) {
    PlaceholderBox(
        modifier = modifier.width(221.dp).height(164.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    )
}

@Composable
private fun TodoViewHeader(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isDone: Boolean,
    todoText: String,
    priority: TaskPriority,
    onChangeDone: (Boolean) -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = priority.mapToString(StudyAssistantRes.strings),
                color = when (priority) {
                    STANDARD -> MaterialTheme.colorScheme.onSurfaceVariant
                    MEDIUM -> StudyAssistantRes.colors.accents.orange
                    HIGH -> StudyAssistantRes.colors.accents.red
                },
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelMedium,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = todoText,
                color = MaterialTheme.colorScheme.onSurface,
                textDecoration = if (isDone) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleSmall,
            )
        }
        RadioButton(
            selected = isDone,
            onClick = {
                onChangeDone(!isDone)
            },
            enabled = enabled,
            modifier = Modifier.size(32.dp),
        )
    }
}

@Composable
private fun TodoViewFooter(
    modifier: Modifier = Modifier,
    status: TodoStatus,
    linkedGoal: GoalShortUi?,
    deadline: Instant?,
    deadlineLeftTime: Millis?,
    completeTime: Instant?,
    progress: Float,
    onScheduleGoal: () -> Unit,
    onDeleteGoal: () -> Unit,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                when (status) {
                    TodoStatus.IN_PROGRESS -> {
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
                                append(
                                    deadline?.formatByTimeZone(deadlineDateFormat)
                                        ?: TasksThemeRes.strings.noneDeadlineTitle
                                )
                            },
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (deadlineLeftTime != null) {
                            Text(
                                text = deadlineLeftTime.toDuration(MILLISECONDS).toLanguageString(),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }

                    TodoStatus.COMPLETE -> {
                        val deadlineDateFormat = Formats.dayMonthFormat(StudyAssistantRes.strings)
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(TasksThemeRes.icons.deadline),
                            contentDescription = null,
                            tint = StudyAssistantRes.colors.accents.green,
                        )
                        Text(
                            text = buildString {
                                append(TasksThemeRes.strings.untilDeadlineDateSuffix, " ")
                                append(
                                    deadline?.formatByTimeZone(deadlineDateFormat) ?: TasksThemeRes.strings.noneDeadlineTitle
                                )
                            },
                            color = StudyAssistantRes.colors.accents.green,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (completeTime != null) {
                            Text(
                                text = completeTime.formatByTimeZone(format = Formats.shortDayMonthTimeFormat()),
                                color = MaterialTheme.colorScheme.primary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.labelSmall,
                            )
                        }
                    }

                    TodoStatus.NOT_COMPLETE -> {
                        val deadlineDateFormat = Formats.dayMonthFormat(StudyAssistantRes.strings)
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(TasksThemeRes.icons.homeworkError),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                        )
                        Text(
                            color = MaterialTheme.colorScheme.error,
                            text = buildString {
                                append(TasksThemeRes.strings.untilDeadlineDateSuffix, " ")
                                append(
                                    deadline?.formatByTimeZone(deadlineDateFormat) ?: TasksThemeRes.strings.noneDeadlineTitle
                                )
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = TasksThemeRes.strings.overdueDeadlineTitle,
                            color = MaterialTheme.colorScheme.error,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
            when (status) {
                TodoStatus.IN_PROGRESS -> HorizontalProgressIndicator(
                    modifier = Modifier.height(10.dp).fillMaxWidth(),
                    progress = { progress },
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                )

                TodoStatus.NOT_COMPLETE -> HorizontalProgressIndicator(
                    modifier = Modifier.height(10.dp).fillMaxWidth(),
                    progress = { 1f },
                    color = MaterialTheme.colorScheme.error,
                    trackColor = MaterialTheme.colorScheme.error,
                )

                TodoStatus.COMPLETE -> Unit
            }
        }
        if (status != TodoStatus.COMPLETE) {
            if (linkedGoal == null) {
                Surface(
                    onClick = onScheduleGoal,
                    modifier = Modifier.size(36.dp),
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                ) {
                    Icon(
                        modifier = Modifier.wrapContentSize(Alignment.Center).size(24.dp),
                        painter = painterResource(TasksThemeRes.icons.calendarGoto),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    )
                }
            } else {
                Surface(
                    onClick = onDeleteGoal,
                    modifier = Modifier.size(36.dp),
                    shape = MaterialTheme.shapes.small,
                    color = Color.Transparent,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                ) {
                    Icon(
                        modifier = Modifier.wrapContentSize(Alignment.Center).size(24.dp),
                        painter = painterResource(TasksThemeRes.icons.timerPlay),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

@Composable
internal fun TodoViewNoneItem(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(StudyAssistantRes.icons.practicalTasks),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = TasksThemeRes.strings.noneTodosTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}