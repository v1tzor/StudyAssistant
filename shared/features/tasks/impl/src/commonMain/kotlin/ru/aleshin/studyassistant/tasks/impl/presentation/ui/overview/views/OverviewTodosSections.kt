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

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalCreateModelUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.DeleteGoalWarningDialog
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.GoalCreatorDialog
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewItem
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewItemPlaceholder
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewNoneItem

/**
 * @author Stanislav Aleshin on 12.06.2025.
 */
@Composable
internal fun OverviewTodosInProgressSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    todos: List<TodoDetailsUi>,
    onOpenTodoTask: (TodoDetailsUi) -> Unit,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
    onScheduleGoal: (GoalCreateModelUi) -> Unit,
    onDeleteGoal: (GoalShortUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = TasksThemeRes.strings.inProgressTodosSectionHeader,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
            )
            Surface(
                modifier = Modifier.size(20.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = (todos.count()).toString(),
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        Crossfade(
            modifier = Modifier.animateContentSize(),
            targetState = isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { loading ->
            if (!isLoading) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (todos.isNotEmpty()) {
                        items(todos, key = { it.uid }) { todo ->
                            var goalCreatorState by rememberSaveable { mutableStateOf(false) }
                            var deleteWarningDialogState by rememberSaveable { mutableStateOf(false) }

                            TodoViewItem(
                                modifier = Modifier.animateItem(),
                                onClick = { onOpenTodoTask(todo) },
                                isDone = todo.isDone,
                                todoText = todo.name,
                                description = todo.description,
                                status = todo.status,
                                linkedGoal = todo.linkedGoal,
                                deadline = todo.deadline,
                                deadlineLeftTime = todo.deadlineTimeLeft,
                                progress = todo.progress,
                                priority = todo.priority,
                                completeTime = todo.completeDate,
                                onChangeDone = { onChangeTodoDone(todo, it) },
                                onScheduleGoal = { goalCreatorState = true },
                                onDeleteGoal = { deleteWarningDialogState = true }
                            )

                            if (goalCreatorState) {
                                GoalCreatorDialog(
                                    contentType = GoalType.TODO,
                                    currentDate = currentDate,
                                    contentHomework = null,
                                    contentTodo = todo,
                                    onDismiss = { goalCreatorState = false },
                                    onCreate = {
                                        goalCreatorState = false
                                        onScheduleGoal(it)
                                    },
                                )
                            }

                            if (deleteWarningDialogState) {
                                DeleteGoalWarningDialog(
                                    onDismiss = { deleteWarningDialogState = false },
                                    onDelete = {
                                        todo.linkedGoal?.let { onDeleteGoal(it) }
                                        deleteWarningDialogState = false
                                    },
                                )
                            }
                        }
                    } else {
                        item {
                            TodoViewNoneItem(modifier = Modifier.fillParentMaxWidth())
                        }
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false,
                ) {
                    items(Placeholder.OVERVIEW_TODOS) { TodoViewItemPlaceholder() }
                }
            }
        }
    }
}

@Composable
internal fun OverviewTodosErrorSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    todos: List<TodoDetailsUi>,
    onOpenTodoTask: (TodoDetailsUi) -> Unit,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
    onScheduleGoal: (GoalCreateModelUi) -> Unit,
    onDeleteGoal: (GoalShortUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = TasksThemeRes.strings.errorTodosSectionHeader,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
            )
            Surface(
                modifier = Modifier.size(20.dp),
                shape = RoundedCornerShape(6.dp),
                color = MaterialTheme.colorScheme.errorContainer,
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = (todos.count()).toString(),
                        color = MaterialTheme.colorScheme.error,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        Crossfade(
            modifier = Modifier.animateContentSize(),
            targetState = isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { loading ->
            if (!isLoading) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (todos.isNotEmpty()) {
                        items(todos, key = { it.uid }) { todo ->
                            var goalCreatorState by rememberSaveable { mutableStateOf(false) }
                            var deleteWarningDialogState by rememberSaveable { mutableStateOf(false) }

                            TodoViewItem(
                                modifier = Modifier.animateItem(),
                                onClick = { onOpenTodoTask(todo) },
                                isDone = todo.isDone,
                                todoText = todo.name,
                                linkedGoal = todo.linkedGoal,
                                description = todo.description,
                                status = todo.status,
                                deadline = todo.deadline,
                                deadlineLeftTime = todo.deadlineTimeLeft,
                                progress = todo.progress,
                                priority = todo.priority,
                                completeTime = todo.completeDate,
                                onChangeDone = { onChangeTodoDone(todo, it) },
                                onScheduleGoal = { goalCreatorState = true },
                                onDeleteGoal = { deleteWarningDialogState = true },
                            )

                            if (deleteWarningDialogState) {
                                DeleteGoalWarningDialog(
                                    onDismiss = { deleteWarningDialogState = false },
                                    onDelete = {
                                        todo.linkedGoal?.let { onDeleteGoal(it) }
                                        deleteWarningDialogState = false
                                    },
                                )
                            }

                            if (goalCreatorState) {
                                GoalCreatorDialog(
                                    contentType = GoalType.TODO,
                                    currentDate = currentDate,
                                    contentHomework = null,
                                    contentTodo = todo,
                                    onDismiss = { goalCreatorState = false },
                                    onCreate = {
                                        goalCreatorState = false
                                        onScheduleGoal(it)
                                    },
                                )
                            }
                        }
                    } else {
                        item {
                            TodoViewNoneItem(modifier = Modifier.fillParentMaxWidth())
                        }
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false,
                ) {
                    items(Placeholder.OVERVIEW_TODOS) { TodoViewItemPlaceholder() }
                }
            }
        }
    }
}

@Composable
internal fun OverviewTodosCompletedSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    todos: List<TodoDetailsUi>,
    onOpenTodoTask: (TodoDetailsUi) -> Unit,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                modifier = Modifier.weight(1f),
                text = TasksThemeRes.strings.completedTodosSectionHeader,
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.titleMedium,
            )
            Surface(
                modifier = Modifier.size(20.dp),
                shape = RoundedCornerShape(6.dp),
                color = StudyAssistantRes.colors.accents.greenContainer,
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        text = (todos.count()).toString(),
                        color = StudyAssistantRes.colors.accents.green,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
        Crossfade(
            modifier = Modifier.animateContentSize(),
            targetState = isLoading,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        ) { loading ->
            if (!isLoading) {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    if (todos.isNotEmpty()) {
                        items(todos, key = { it.uid }) { todo ->
                            TodoViewItem(
                                modifier = Modifier.animateItem(),
                                onClick = { onOpenTodoTask(todo) },
                                isDone = todo.isDone,
                                todoText = todo.name,
                                linkedGoal = todo.linkedGoal,
                                description = todo.description,
                                status = todo.status,
                                deadline = todo.deadline,
                                deadlineLeftTime = todo.deadlineTimeLeft,
                                progress = todo.progress,
                                priority = todo.priority,
                                completeTime = todo.completeDate,
                                onChangeDone = { onChangeTodoDone(todo, it) },
                                onScheduleGoal = {},
                                onDeleteGoal = {},
                            )
                        }
                    } else {
                        item {
                            TodoViewNoneItem(modifier = Modifier.fillParentMaxWidth())
                        }
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = false,
                ) {
                    items(Placeholder.OVERVIEW_TODOS) { TodoViewItemPlaceholder() }
                }
            }
        }
    }
}