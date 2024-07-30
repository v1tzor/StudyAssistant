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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority.HIGH
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority.MEDIUM
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority.STANDARD
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoStatus
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.mappers.toLanguageString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.InfoBadge
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.dayMonthFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import kotlin.time.Duration

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
    status: TodoStatus,
    deadline: Instant?,
    toDeadlineDuration: Duration?,
    priority: TaskPriority,
    onChangeDone: (Boolean) -> Unit,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.width(IntrinsicSize.Max),
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            TodoViewHeader(
                isDone = isDone,
                enabled = enabled,
                todoText = todoText,
                priority = priority,
                onChangeDone = onChangeDone,
            )
            HorizontalDivider()
            TodoViewFooter(
                deadline = deadline,
                toDeadlineDuration = toDeadlineDuration,
                status = status,
            )
        }
    }
}

@Composable
internal fun TodoViewItemPlaceholder(
    modifier: Modifier = Modifier,
) {
    PlaceholderBox(
        modifier = modifier.fillMaxWidth().height(130.dp),
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
    Column(
        modifier = modifier.padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.widthIn(max = 400.dp).weight(1f),
                text = todoText,
                color = MaterialTheme.colorScheme.onSurface,
                textDecoration = if (isDone) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                },
                overflow = TextOverflow.Ellipsis,
                maxLines = 4,
                style = MaterialTheme.typography.bodyMedium,
            )
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
}

@Composable
private fun TodoViewFooter(
    modifier: Modifier = Modifier,
    deadline: Instant?,
    toDeadlineDuration: Duration?,
    status: TodoStatus,
) {
    Row(
        modifier = modifier.padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (deadline != null) {
            val deadlineDateFormat = DateTimeComponents.Formats.dayMonthFormat(StudyAssistantRes.strings)
            when (status) {
                TodoStatus.COMPLETE -> {
                    InfoBadge(
                        modifier = modifier,
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                painter = painterResource(StudyAssistantRes.icons.tasksOutline),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        content = {
                            Text(
                                text = buildString {
                                    append(TasksThemeRes.strings.untilDeadlineDateSuffix, " ")
                                    append(deadline.formatByTimeZone(deadlineDateFormat))
                                },
                                maxLines = 1,
                            )
                        }
                    )
                }
                TodoStatus.IN_PROGRESS -> {
                    InfoBadge(
                        modifier = modifier,
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                painter = painterResource(StudyAssistantRes.icons.tasksOutline),
                                contentDescription = null,
                                tint = StudyAssistantRes.colors.accents.yellow,
                            )
                        },
                        containerColor = StudyAssistantRes.colors.accents.yellowContainer,
                        content = {
                            Text(
                                text = buildString {
                                    append(TasksThemeRes.strings.untilDeadlineDateSuffix, " ")
                                    append(deadline.formatByTimeZone(deadlineDateFormat))
                                },
                                maxLines = 1,
                            )
                        }
                    )
                    if (toDeadlineDuration != null) {
                        InfoBadge(
                            modifier = modifier,
                            containerColor = StudyAssistantRes.colors.accents.yellowContainer,
                            content = {
                                Text(text = toDeadlineDuration.toLanguageString(), maxLines = 1)
                            }
                        )
                    }
                }
                TodoStatus.NOT_COMPLETE -> {
                    InfoBadge(
                        modifier = modifier,
                        leadingIcon = {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = StudyAssistantRes.colors.accents.red,
                            )
                        },
                        containerColor = StudyAssistantRes.colors.accents.redContainer,
                        content = {
                            Text(
                                text = buildString {
                                    append(TasksThemeRes.strings.untilDeadlineDateSuffix, " ")
                                    append(deadline.formatByTimeZone(deadlineDateFormat))
                                },
                                maxLines = 1,
                            )
                        }
                    )
                    if (toDeadlineDuration != null) {
                        InfoBadge(
                            modifier = modifier,
                            containerColor = StudyAssistantRes.colors.accents.redContainer,
                            content = {
                                Text(text = toDeadlineDuration.toLanguageString(), maxLines = 1)
                            }
                        )
                    }
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