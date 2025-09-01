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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.todo.contract

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.EditTodoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.TodoNotificationsUi

/**
 * @author Stanislav Aleshin on 26.07.2024
 */
@Serializable
internal data class TodoState(
    val isLoading: Boolean = true,
    val isLoadingSave: Boolean = false,
    val isPaidUser: Boolean = false,
    val editableTodo: EditTodoUi? = null,
) : StoreState

internal sealed class TodoEvent : StoreEvent {
    data class Started(val inputData: TodoInput) : TodoEvent()
    data class UpdateTodoName(val todo: String) : TodoEvent()
    data class UpdateTodoDescription(val description: String) : TodoEvent()
    data class UpdateDeadline(val deadline: Instant?) : TodoEvent()
    data class UpdatePriority(val priority: TaskPriority) : TodoEvent()
    data class UpdateNotifications(val notifications: TodoNotificationsUi) : TodoEvent()
    data object DeleteTodo : TodoEvent()
    data object SaveTodo : TodoEvent()
    data object NavigateToBilling : TodoEvent()
    data object NavigateToBack : TodoEvent()
}

internal sealed class TodoEffect : StoreEffect {
    data class ShowError(val failures: EditorFailures) : TodoEffect()
}

internal sealed class TodoAction : StoreAction {
    data class SetupEditModel(val editModel: EditTodoUi, val isPaidUser: Boolean) : TodoAction()
    data class UpdateEditModel(val editModel: EditTodoUi?) : TodoAction()
    data class UpdateLoading(val isLoading: Boolean) : TodoAction()
    data class UpdateLoadingSave(val isLoading: Boolean) : TodoAction()
}

internal data class TodoInput(
    val todoId: UID?
) : BaseInput

internal sealed class TodoOutput : BaseOutput {
    data object NavigateToBack : TodoOutput()
    data object NavigateToBilling : TodoOutput()
}