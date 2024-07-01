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

import architecture.screenmodel.work.ActionResult
import architecture.screenmodel.work.EffectResult
import architecture.screenmodel.work.FlowWorkProcessor
import architecture.screenmodel.work.WorkCommand
import architecture.screenmodel.work.WorkResult
import entities.tasks.HomeworkStatus
import entities.tasks.TodoStatus
import extensions.endOfWeek
import extensions.extractAllItem
import extensions.shiftWeek
import extensions.startOfWeek
import extensions.startThisDay
import functional.Constants.Delay
import functional.TimeRange
import functional.collectAndHandle
import functional.handle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.datetime.Instant
import managers.DateManager
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.HomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.TodoInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkScopeUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.fetchAllTasks
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract.OverviewEffect

/**
 * @author Stanislav Aleshin on 27.06.2024.
 */
internal interface OverviewWorkProcessor :
    FlowWorkProcessor<OverviewWorkCommand, OverviewAction, OverviewEffect> {

    class Base(
        private val homeworksInteractor: HomeworksInteractor,
        private val todoInteractor: TodoInteractor,
        private val scheduleInteractor: ScheduleInteractor,
        private val dateManager: DateManager,
    ) : OverviewWorkProcessor {

        override suspend fun work(command: OverviewWorkCommand) = when (command) {
            is OverviewWorkCommand.LoadHomeworks -> loadHomeworksWork(command.currentDate)
            is OverviewWorkCommand.LoadHomeworkErrors -> loadTaskErrorsWork(command.currentDate)
            is OverviewWorkCommand.LoadTodos -> loadTodosWork(command.currentDate)
            is OverviewWorkCommand.LoadActiveSchedule -> loadActiveScheduleWork(command.currentDate)
            is OverviewWorkCommand.UpdateHomework -> updateHomeworkWork(command.homework)
            is OverviewWorkCommand.UpdateTodo -> updateTodoWork(command.todo)
        }

        private fun loadHomeworksWork(currentDate: Instant) = flow<OverviewWorkResult> {
            val targetTimeRange = TimeRange(
                from = currentDate.startOfWeek().shiftWeek(-1),
                to = currentDate.endOfWeek().shiftWeek(+1),
            )
            homeworksInteractor.fetchHomeworksByTimeRange(targetTimeRange).collectAndHandle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
                onRightAction = { homeworkList ->
                    val homeworks = homeworkList.map {
                        val status = HomeworkStatus.calculate(it.isDone, it.completeDate, it.deadline, currentDate)
                        return@map it.mapToUi(status)
                    }
                    val groupedHomeworks = homeworks.groupBy { homework ->
                        homework.deadline.startThisDay()
                    }
                    val homeworksMap = buildMap {
                        targetTimeRange.periodDates().forEach { date ->
                            put(date, groupedHomeworks[date] ?: emptyList<HomeworkDetailsUi>())
                        }
                    }
                    val homeworksScope = calculateHomeworkScope(homeworksMap)
                    emit(ActionResult(OverviewAction.UpdateHomeworks(homeworksMap, homeworksScope)))
                    emitAll(cycleUpdateHomeworkStatus(homeworksMap))
                },
            )
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateHomeworksLoading(true)))
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadTaskErrorsWork(currentDate: Instant) = flow {
            val homeworkErrorsFlow = homeworksInteractor.fetchHomeworkErrors(currentDate)
            val todoErrorsFlow = todoInteractor.fetchTodoErrors(currentDate)
            homeworkErrorsFlow.flatMapLatestWithResult(
                secondFlow = todoErrorsFlow,
                onError = { OverviewEffect.ShowError(it) },
                onData = { homeworkErrors, todoErrors ->
                    OverviewAction.UpdateTaskErrors(homeworkErrors.mapToUi(), todoErrors.mapToUi())
                },
            ).collect { result ->
                emit(result)
            }
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateErrorsLoading(true)))
        }

        private fun loadTodosWork(currentDate: Instant) = flow<OverviewWorkResult> {
            val targetTimeRange = TimeRange(
                from = currentDate.startOfWeek().shiftWeek(-1),
                to = currentDate.endOfWeek().shiftWeek(+1),
            )
            todoInteractor.fetchTodosByTimeRange(targetTimeRange).collectAndHandle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
                onRightAction = { todoList ->
                    val todos = todoList.map {
                        val status = TodoStatus.calculate(it.isDone, it.deadline, currentDate)
                        return@map it.mapToUi(status)
                    }
                    emit(ActionResult(OverviewAction.UpdateTodos(todos)))
                    emitAll(cycleUpdateTodoStatus(todos))
                },
            )
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateTasksLoading(true)))
        }

        private fun loadActiveScheduleWork(currentDate: Instant) = flow<OverviewWorkResult> {
            scheduleInteractor.fetchScheduleByDate(currentDate).collectAndHandle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
                onRightAction = { schedule ->
                    emit(ActionResult(OverviewAction.UpdateActiveSchedule(schedule.mapToUi())))
                },
            )
        }

        private fun updateHomeworkWork(homework: HomeworkDetailsUi) = flow {
            homeworksInteractor.updateHomework(homework.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
            )
        }

        private fun updateTodoWork(todo: TodoDetailsUi) = flow {
            todoInteractor.updateTodo(todo.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
            )
        }

        private fun cycleUpdateHomeworkStatus(homeworksMap: Map<Instant, List<HomeworkDetailsUi>>) = flow {
            val updatedHomeworksMap = homeworksMap.toMutableMap()
            while (currentCoroutineContext().isActive) {
                delay(Delay.UPDATE_TASK_STATUS)

                var isUpdated = false
                val currentInstant = dateManager.fetchCurrentInstant()
                val updatedHomeworks = updatedHomeworksMap.values.toList().extractAllItem()

                updatedHomeworks.forEach { homework ->
                    val status = homework.status
                    if (status == HomeworkStatus.WAIT || status == HomeworkStatus.IN_FUTURE) {
                        val newStatus = HomeworkStatus.calculate(
                            isDone = homework.isDone,
                            completeDate = homework.completeDate,
                            deadline = homework.deadline,
                            currentTime = currentInstant,
                        )
                        if (newStatus != status) {
                            isUpdated = true
                            val date = homework.deadline.startThisDay()
                            val newHomework = homework.copy(status = newStatus)
                            val newHomeworks = updatedHomeworksMap[date]!!.toMutableList().apply {
                                remove(homework)
                                add(newHomework)
                            }
                            updatedHomeworksMap[date] = newHomeworks
                        }
                    }
                }
                if (isUpdated) {
                    val homeworksScope = calculateHomeworkScope(updatedHomeworksMap)
                    emit(ActionResult(OverviewAction.UpdateHomeworks(updatedHomeworksMap, homeworksScope)))
                }
            }
        }

        private fun cycleUpdateTodoStatus(todos: List<TodoDetailsUi>) = flow {
            val updatedTodos = todos.toMutableList()
            while (currentCoroutineContext().isActive) {
                delay(Delay.UPDATE_TASK_STATUS)

                var isUpdated = false
                val currentInstant = dateManager.fetchCurrentInstant()

                updatedTodos.forEachIndexed { todoIndex, todo ->
                    if (todo.status == TodoStatus.IN_PROGRESS) {
                        val newStatus = TodoStatus.calculate(todo.isDone, todo.deadline, currentInstant)
                        if (newStatus != todo.status) {
                            isUpdated = true
                            val newTodo = todo.copy(status = newStatus)
                            updatedTodos[todoIndex] = newTodo
                        }
                    }
                }
                if (isUpdated) {
                    emit(ActionResult(OverviewAction.UpdateTodos(updatedTodos)))
                }
            }
        }

        private fun calculateHomeworkScope(
            homeworksMap: Map<Instant, List<HomeworkDetailsUi>>
        ): HomeworkScopeUi {
            return HomeworkScopeUi(
                theoreticalTasks = homeworksMap.mapValues { homeworkEntry ->
                    homeworkEntry.value.sumOf { homework ->
                        return@sumOf homework.theoreticalTasks.components.fetchAllTasks().size
                    }
                },
                practicalTasks = homeworksMap.mapValues { homeworkEntry ->
                    homeworkEntry.value.sumOf { homework ->
                        return@sumOf homework.practicalTasks.components.fetchAllTasks().size
                    }
                },
                presentationTasks = homeworksMap.mapValues { homeworkEntry ->
                    homeworkEntry.value.sumOf { homework ->
                        return@sumOf homework.presentationTasks.components.fetchAllTasks().size
                    }
                },
            )
        }
    }
}

internal sealed class OverviewWorkCommand : WorkCommand {
    data class LoadHomeworks(val currentDate: Instant) : OverviewWorkCommand()
    data class LoadHomeworkErrors(val currentDate: Instant) : OverviewWorkCommand()
    data class LoadTodos(val currentDate: Instant) : OverviewWorkCommand()
    data class LoadActiveSchedule(val currentDate: Instant) : OverviewWorkCommand()
    data class UpdateHomework(val homework: HomeworkDetailsUi) : OverviewWorkCommand()
    data class UpdateTodo(val todo: TodoDetailsUi) : OverviewWorkCommand()
}

internal typealias OverviewWorkResult = WorkResult<OverviewAction, OverviewEffect>