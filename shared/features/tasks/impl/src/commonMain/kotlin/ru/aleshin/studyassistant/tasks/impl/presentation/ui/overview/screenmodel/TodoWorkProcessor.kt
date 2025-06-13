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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.screenmodel

import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.startOfWeek
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.TodoInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEffect

/**
 * @author Stanislav Aleshin on 27.06.2024.
 */
internal interface TodoWorkProcessor : FlowWorkProcessor<TodoWorkCommand, OverviewAction, OverviewEffect> {

    class Base(private val todoInteractor: TodoInteractor) : TodoWorkProcessor {

        override suspend fun work(command: TodoWorkCommand) = when (command) {
            is TodoWorkCommand.LoadTodos -> loadTodosWork(command.currentDate)
            is TodoWorkCommand.UpdateTodoDone -> updateTodoDoneWork(command.todo)
        }

        private fun loadTodosWork(currentDate: Instant) = flow<OverviewWorkResult> {
            val targetTimeRange = TimeRange(
                from = currentDate.startOfWeek(),
                to = currentDate.endOfWeek(),
            )
            todoInteractor.fetchWeekGroupedTodosByTimeRange(targetTimeRange).collectAndHandle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
                onRightAction = { groupedTodos ->
                    emit(ActionResult(OverviewAction.UpdateTodos(groupedTodos.mapToUi())))
                },
            )
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateTasksLoading(true)))
        }

        private fun updateTodoDoneWork(todo: TodoDetailsUi) = flow {
            todoInteractor.updateTodoDone(todo.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
            )
        }
    }
}

internal sealed class TodoWorkCommand : WorkCommand {
    data class LoadTodos(val currentDate: Instant) : TodoWorkCommand()
    data class UpdateTodoDone(val todo: TodoDetailsUi) : TodoWorkCommand()
}

internal typealias OverviewWorkResult = WorkResult<OverviewAction, OverviewEffect>