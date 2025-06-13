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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoStatus
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoUi
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
    onOpenTodoTask: (TodoUi) -> Unit,
    onChangeTodoDone: (TodoUi, Boolean) -> Unit,
) = with(state) {
    Crossfade(
        modifier = modifier,
        targetState = isLoading,
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
                if (completedTodos.isNotEmpty()) {
                    items(completedTodos, key = { it.uid }) { todo ->
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
                if (completedTodos.isNotEmpty()) {
                    items(Constants.Placeholder.TODOS) {
                        TodoViewItemPlaceholder()
                    }
                }
            }
        }
    }
}