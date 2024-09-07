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

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi

/**
 * @author Stanislav Aleshin on 28.07.2024
 */
@Parcelize
@Immutable
internal data class TodoViewState(
    val isLoading: Boolean = true,
    val selectedTimeRange: TimeRange? = null,
    val todos: List<TodoDetailsUi> = emptyList(),
) : BaseViewState

internal sealed class TodoEvent : BaseEvent {
    data object Init : TodoEvent()
    data object CurrentTimeRange : TodoEvent()
    data object NextTimeRange : TodoEvent()
    data object PreviousTimeRange : TodoEvent()
    data class UpdateTodoDone(val todo: TodoDetailsUi, val isDone: Boolean) : TodoEvent()
    data class NavigateToTodoEditor(val todo: TodoDetailsUi?) : TodoEvent()
    data object NavigateToBack : TodoEvent()
}

internal sealed class TodoEffect : BaseUiEffect {
    data class ShowError(val failures: TasksFailures) : TodoEffect()
    data class NavigateToGlobal(val pushScreen: Screen) : TodoEffect()
    data object NavigateToBack : TodoEffect()
}

internal sealed class TodoAction : BaseAction {
    data class UpdateTodos(val todos: List<TodoDetailsUi>) : TodoAction()
    data class UpdateTimeRange(val selectedTimeRange: TimeRange?) : TodoAction()
    data class UpdateLoading(val isLoading: Boolean) : TodoAction()
}