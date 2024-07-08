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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.tokens.dayOfWeekShortNames
import ru.aleshin.studyassistant.core.ui.theme.tokens.monthNames
import ru.aleshin.studyassistant.core.ui.views.MediumDragHandle
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkErrorsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTaskComponentUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoErrorsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.fetchAllTasks
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 04.07.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun TaskErrorsBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    homeworkErrors: HomeworkErrorsUi?,
    todoErrors: TodoErrorsUi?,
    onDismissRequest: () -> Unit,
    onEditHomework: (HomeworkDetailsUi) -> Unit,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier,
        dragHandle = { MediumDragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            OverdueTodosBottomSheetSection(
                overdueTodos = todoErrors?.overdueTodos ?: emptyList(),
                onChangeTodoDone = onChangeTodoDone,
            )
            OverdueHomeworksBottomSheetSection(
                overdueTasks = homeworkErrors?.overdueTasks ?: emptyList(),
                onDoHomework = onDoHomework,
                onSkipHomework = onSkipHomework,
            )
            DetachedHomeworksBottomSheetSection(
                detachedActiveTasks = homeworkErrors?.detachedActiveTasks ?: emptyList(),
                onEditHomework = onEditHomework,
            )
        }
    }
}

@Composable
private fun OverdueTodosBottomSheetSection(
    modifier: Modifier = Modifier,
    overdueTodos: List<TodoDetailsUi>,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = TasksThemeRes.strings.overdueTodosHeader,
                modifier = Modifier.weight(1f),
                color = StudyAssistantRes.colors.accents.red,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = overdueTodos.size.toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        if (overdueTodos.isEmpty()) {
            NoneErrorsView()
        } else if (overdueTodos.size <= 3) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                overdueTodos.forEach { todo ->
                    ErrorTodoView(
                        deadline = todo.deadline,
                        todoText = todo.name,
                        actions = {
                            DoAndSkipActions(
                                onDo = { onChangeTodoDone(todo, true) },
                                onSkip = { onChangeTodoDone(todo, true) },
                            )
                        },
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(190.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(overdueTodos, key = { it.uid }) { todo ->
                    ErrorTodoView(
                        deadline = todo.deadline,
                        todoText = todo.name,
                        actions = {
                            DoAndSkipActions(
                                onDo = { onChangeTodoDone(todo, true) },
                                onSkip = { onChangeTodoDone(todo, true) },
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun OverdueHomeworksBottomSheetSection(
    modifier: Modifier = Modifier,
    overdueTasks: List<HomeworkDetailsUi>,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = TasksThemeRes.strings.overdueHomeworksHeader,
                modifier = Modifier.weight(1f),
                color = StudyAssistantRes.colors.accents.red,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = overdueTasks.size.toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        if (overdueTasks.isEmpty()) {
            NoneErrorsView()
        } else if (overdueTasks.size <= 3) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                overdueTasks.forEach { homework ->
                    ErrorHomeworkView(
                        deadline = homework.deadline,
                        subject = homework.subject,
                        theoreticalTasks = homework.theoreticalTasks.components,
                        practicalTasks = homework.practicalTasks.components,
                        presentationTasks = homework.presentationTasks.components,
                        actions = {
                            DoAndSkipActions(
                                onDo = { onDoHomework(homework) },
                                onSkip = { onSkipHomework(homework) },
                            )
                        }
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(190.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(overdueTasks, key = { it.uid }) { homework ->
                    ErrorHomeworkView(
                        deadline = homework.deadline,
                        subject = homework.subject,
                        theoreticalTasks = homework.theoreticalTasks.components,
                        practicalTasks = homework.practicalTasks.components,
                        presentationTasks = homework.presentationTasks.components,
                        actions = {
                            DoAndSkipActions(
                                onDo = { onDoHomework(homework) },
                                onSkip = { onSkipHomework(homework) },
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun DetachedHomeworksBottomSheetSection(
    modifier: Modifier = Modifier,
    detachedActiveTasks: List<HomeworkDetailsUi>,
    onEditHomework: (HomeworkDetailsUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = TasksThemeRes.strings.detachedActiveHomeworksHeader,
                modifier = Modifier.weight(1f),
                color = StudyAssistantRes.colors.accents.red,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = detachedActiveTasks.size.toString(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelLarge,
            )
        }
        if (detachedActiveTasks.isEmpty()) {
            NoneErrorsView()
        } else if (detachedActiveTasks.size <= 3) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                detachedActiveTasks.forEach { homework ->
                    ErrorHomeworkView(
                        deadline = homework.deadline,
                        subject = homework.subject,
                        theoreticalTasks = homework.theoreticalTasks.components,
                        practicalTasks = homework.practicalTasks.components,
                        presentationTasks = homework.presentationTasks.components,
                        actions = { EditAction(onEdit = { onEditHomework(homework) }) },
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().height(190.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(detachedActiveTasks, key = { it.uid }) { homework ->
                    ErrorHomeworkView(
                        deadline = homework.deadline,
                        subject = homework.subject,
                        theoreticalTasks = homework.theoreticalTasks.components,
                        practicalTasks = homework.practicalTasks.components,
                        presentationTasks = homework.presentationTasks.components,
                        actions = { EditAction(onEdit = { onEditHomework(homework) }) },
                    )
                }
            }
        }
    }
}

@Composable
private fun ErrorHomeworkView(
    modifier: Modifier = Modifier,
    deadline: Instant,
    subject: SubjectUi?,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
    actions: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxHeight().width(4.dp),
            shape = MaterialTheme.shapes.small,
            color = subject?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.outline,
            content = { Box(modifier = Modifier.fillMaxHeight()) }
        )
        ErrorHomeworkViewContent(
            modifier = Modifier.weight(1f),
            deadline = deadline,
            subject = subject?.name,
            theoreticalTasks = theoreticalTasks,
            practicalTasks = practicalTasks,
            presentationTasks = presentationTasks,
        )
        actions()
    }
}

@Composable
private fun ErrorHomeworkViewContent(
    modifier: Modifier = Modifier,
    deadline: Instant,
    subject: String?,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Column {
            val coreStrings = StudyAssistantRes.strings
            val dateFormat = DateTimeComponents.Format {
                dayOfWeek(coreStrings.dayOfWeekShortNames())
                chars(", ")
                dayOfMonth()
                char(' ')
                monthName(coreStrings.monthNames())
            }
            Text(
                text = deadline.formatByTimeZone(dateFormat),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
            Text(
                text = subject ?: StudyAssistantRes.strings.noneTitle,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ErrorHomeworkTaskCountView(
                painter = painterResource(StudyAssistantRes.icons.theoreticalTasks),
                count = theoreticalTasks.fetchAllTasks().size,
            )
            ErrorHomeworkTaskCountView(
                painter = painterResource(StudyAssistantRes.icons.practicalTasks),
                count = practicalTasks.fetchAllTasks().size,
            )
            ErrorHomeworkTaskCountView(
                painter = painterResource(StudyAssistantRes.icons.presentationTasks),
                count = presentationTasks.fetchAllTasks().size,
            )
        }
    }
}

@Composable
private fun ErrorHomeworkTaskCountView(
    modifier: Modifier = Modifier,
    painter: Painter,
    count: Int,
    description: String? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = painter,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun ErrorTodoView(
    modifier: Modifier = Modifier,
    todoText: String,
    deadline: Instant?,
    actions: @Composable () -> Unit,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (deadline != null) {
                    val coreStrings = StudyAssistantRes.strings
                    val dateFormat = DateTimeComponents.Format {
                        dayOfWeek(coreStrings.dayOfWeekShortNames())
                        chars(", ")
                        dayOfMonth()
                        char(' ')
                        monthName(coreStrings.monthNames())
                    }
                    Text(
                        text = deadline.formatByTimeZone(dateFormat),
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }
                Text(
                    modifier = Modifier.weight(1f),
                    text = todoText,
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 3,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
        actions()
    }
}

@Composable
private fun DoAndSkipActions(
    onDo: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(modifier = modifier) {
        IconButton(
            onClick = onDo,
            modifier = Modifier.size(40.dp),
            enabled = enabled,
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = StudyAssistantRes.colors.accents.green,
            )
        }
        IconButton(
            onClick = onSkip,
            modifier = Modifier.size(40.dp),
            enabled = enabled,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = StudyAssistantRes.colors.accents.red,
            )
        }
    }
}

@Composable
private fun EditAction(
    onEdit: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    IconButton(
        onClick = onEdit,
        modifier = modifier.size(40.dp),
        enabled = enabled,
    ) {
        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
}

@Composable
private fun NoneErrorsView(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(1f),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = TasksThemeRes.strings.noneErrorsTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}