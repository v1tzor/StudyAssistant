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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.contract

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoUi

/**
 * @author Stanislav Aleshin on 28.07.2024
 */
@Serializable
internal data class TodoState(
    val isLoading: Boolean = true,
    val completedTodos: List<TodoUi> = emptyList(),
) : StoreState

internal sealed class TodoEvent : StoreEvent {
    data object Started : TodoEvent()
    data class UpdateTodoDone(val todo: TodoUi, val isDone: Boolean) : TodoEvent()
    data class ClickTodoTask(val todo: TodoUi?) : TodoEvent()
    data object ClickBack : TodoEvent()
}

internal sealed class TodoEffect : StoreEffect {
    data class ShowError(val failures: TasksFailures) : TodoEffect()
}

internal sealed class TodoAction : StoreAction {
    data class UpdateTodos(val todos: List<TodoUi>) : TodoAction()
    data class UpdateLoading(val isLoading: Boolean) : TodoAction()
}

internal sealed class TodoOutput : BaseOutput {
    data object NavigateToBack : TodoOutput()
    data class NavigateToTodoEditor(val config: EditorConfig.Todo) : TodoOutput()
}