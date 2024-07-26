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

import androidx.compose.runtime.Immutable
import dev.icerock.moko.parcelize.Parcelize
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.EditTodoUi

/**
 * @author Stanislav Aleshin on 26.07.2024
 */
@Immutable
@Parcelize
internal data class TodoViewState(
    val isLoading: Boolean = true,
    val editableTodo: EditTodoUi? = null,
) : BaseViewState

internal sealed class TodoEvent : BaseEvent {
    data class Init(val todoId: UID?) : TodoEvent()
    data class UpdateTodoName(val todo: String) : TodoEvent()
    data class UpdateDeadline(val deadline: Instant?) : TodoEvent()
    data class UpdatePriority(val priority: TaskPriority) : TodoEvent()
    data class UpdateNotification(val notification: Boolean) : TodoEvent()
    data object DeleteTodo : TodoEvent()
    data object SaveTodo : TodoEvent()
    data object NavigateToBack : TodoEvent()
}

internal sealed class TodoEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : TodoEffect()
    data object NavigateToBack : TodoEffect()
}

internal sealed class TodoAction : BaseAction {
    data class SetupEditModel(val editModel: EditTodoUi) : TodoAction()
    data class UpdateEditModel(val editModel: EditTodoUi?) : TodoAction()
    data class UpdateLoading(val isLoading: Boolean) : TodoAction()
}