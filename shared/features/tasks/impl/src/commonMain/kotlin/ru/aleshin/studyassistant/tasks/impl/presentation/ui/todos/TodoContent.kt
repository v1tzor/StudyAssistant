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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoStatus
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewItem
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewItemPlaceholder
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewNoneItem
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoState
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.store.TodoComponent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.views.TodoTopBar

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
@Composable
internal fun TodoContent(
    todoComponent: TodoComponent,
    modifier: Modifier = Modifier,
) {
    val store = todoComponent.store
    val state by store.stateAsState()
    val strings = TasksThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseTodoContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onOpenTodoTask = {
                    store.dispatchEvent(TodoEvent.ClickTodoTask(it))
                },
                onChangeTodoDone = { todo, done ->
                    store.dispatchEvent(TodoEvent.UpdateTodoDone(todo, done))
                },
            )
        },
        topBar = {
            TodoTopBar(
                onBackClick = { store.dispatchEvent(TodoEvent.ClickBack) },
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    store.dispatchEvent(TodoEvent.ClickTodoTask(null))
                },
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    )

    store.handleEffects { effect ->
        when (effect) {
            is TodoEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun BaseTodoContent(
    state: TodoState,
    modifier: Modifier = Modifier,
    onOpenTodoTask: (TodoUi) -> Unit,
    onChangeTodoDone: (TodoUi, Boolean) -> Unit,
) {
    Crossfade(
        modifier = modifier,
        targetState = state.isLoading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ) { loading ->
        if (!loading) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(190.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.completedTodos.isNotEmpty()) {
                    items(state.completedTodos, key = { it.uid }) { todo ->
                        TodoViewItem(
                            modifier = Modifier.animateItem(),
                            onClick = { onOpenTodoTask(todo) },
                            isDone = todo.isDone,
                            todoText = todo.name,
                            description = todo.description,
                            status = TodoStatus.COMPLETE,
                            linkedGoal = null,
                            deadline = todo.deadline,
                            deadlineLeftTime = null,
                            progress = 1f,
                            priority = todo.priority,
                            completeTime = todo.completeDate,
                            onChangeDone = { onChangeTodoDone(todo, it) },
                            onScheduleGoal = {},
                            onDeleteGoal = {}
                        )
                    }
                } else {
                    item {
                        TodoViewNoneItem(modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                columns = GridCells.Adaptive(190.dp),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (state.completedTodos.isNotEmpty()) {
                    items(Constants.Placeholder.TODOS) {
                        TodoViewItemPlaceholder()
                    }
                }
            }
        }
    }
}