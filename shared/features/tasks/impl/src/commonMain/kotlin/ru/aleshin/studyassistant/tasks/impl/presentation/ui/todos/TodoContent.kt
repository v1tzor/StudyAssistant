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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewItem
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewItemPlaceholder
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.TodoViewNoneItem
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract.TodoViewState

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
@Composable
internal fun TodoContent(
    state: TodoViewState,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onOpenTodoTask: (TodoDetailsUi) -> Unit,
    onChangeTodoDone: (TodoDetailsUi, Boolean) -> Unit,
) = with(state) {
    val completedTodos = todos.filter { it.completeDate != null }
    val activeTodos = todos.filter { it.completeDate == null }

    Crossfade(
        modifier = modifier,
        targetState = isLoading,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
    ) { loading ->
        if (!loading) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                if (todos.isEmpty()) {
                    item {
                        TodoViewNoneItem(
                            modifier = Modifier.fillParentMaxWidth().padding(16.dp),
                        )
                    }
                } else {
                    items(completedTodos, key = { it.uid }) { todo ->
                        TodoViewItem(
                            modifier = Modifier
                                .animateItem()
                                .fillParentMaxWidth()
                                .padding(horizontal = 16.dp),
                            onClick = { onOpenTodoTask(todo) },
                            isDone = todo.isDone,
                            todoText = todo.name,
                            status = todo.status,
                            deadline = todo.deadline,
                            toDeadlineDuration = todo.toDeadlineDuration,
                            priority = todo.priority,
                            onChangeDone = { onChangeTodoDone(todo, it) }
                        )
                    }

                    if (completedTodos.isNotEmpty()) {
                        item { HorizontalDivider() }
                    }

                    items(activeTodos, key = { it.uid }) { todo ->
                        TodoViewItem(
                            modifier = Modifier
                                .animateItem()
                                .fillParentMaxWidth()
                                .padding(horizontal = 16.dp),
                            onClick = { onOpenTodoTask(todo) },
                            isDone = todo.isDone,
                            todoText = todo.name,
                            status = todo.status,
                            deadline = todo.deadline,
                            toDeadlineDuration = todo.toDeadlineDuration,
                            priority = todo.priority,
                            onChangeDone = { onChangeTodoDone(todo, it) }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(60.dp)) }
                }
            }
            LaunchedEffect(isLoading) {
                if (!isLoading) {
                    val targetIndex = completedTodos.size
                    listState.animateScrollToItem(targetIndex)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp).fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                userScrollEnabled = false,
            ) {
                items(Constants.Placeholder.TODOS) {
                    TodoViewItemPlaceholder()
                }
            }
        }
    }
}