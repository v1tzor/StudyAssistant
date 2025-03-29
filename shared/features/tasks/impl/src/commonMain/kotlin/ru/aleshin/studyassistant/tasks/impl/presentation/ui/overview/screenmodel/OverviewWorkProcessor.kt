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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.isActive
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.ActionResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.EffectResult
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkProcessor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkCommand
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkResult
import ru.aleshin.studyassistant.core.common.extensions.endOfWeek
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startOfWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.Constants.Delay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.collectAndHandle
import ru.aleshin.studyassistant.core.common.functional.handle
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.tasks.DailyHomeworksStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoStatus
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.HomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ScheduleInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.ShareHomeworksInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.TodoInteractor
import ru.aleshin.studyassistant.tasks.impl.domain.interactors.UsersInteractor
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToDomain
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.DailyHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkScopeUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.convertToBase
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.convertToDetails
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
        private val shareInteractor: ShareHomeworksInteractor,
        private val usersInteractor: UsersInteractor,
        private val dateManager: DateManager,
    ) : OverviewWorkProcessor {

        override suspend fun work(command: OverviewWorkCommand) = when (command) {
            is OverviewWorkCommand.LoadHomeworks -> loadHomeworksWork(command.currentDate)
            is OverviewWorkCommand.LoadHomeworkErrors -> loadTaskErrorsWork(command.currentDate)
            is OverviewWorkCommand.LoadTodos -> loadTodosWork(command.currentDate)
            is OverviewWorkCommand.LoadSharedHomeworks -> loadSharedHomeworksWork()
            is OverviewWorkCommand.LoadActiveSchedule -> loadActiveScheduleWork(command.currentDate)
            is OverviewWorkCommand.UpdateHomework -> updateHomeworkWork(command.homework)
            is OverviewWorkCommand.UpdateTodo -> updateTodoWork(command.todo)
            is OverviewWorkCommand.ShareHomeworks -> shareHomeworksWork(command.sentMediatedHomeworks)
        }

        private fun loadHomeworksWork(currentDate: Instant) = channelFlow<OverviewWorkResult> {
            var cycleUpdateJob: Job? = null
            val targetTimeRange = TimeRange(
                from = currentDate.startOfWeek().shiftWeek(-1),
                to = currentDate.endOfWeek().shiftWeek(+1),
            )
            homeworksInteractor.fetchHomeworksByTimeRange(targetTimeRange).collect { homeworksEither ->
                cycleUpdateJob?.cancelAndJoin()
                homeworksEither.handle(
                    onLeftAction = { send(EffectResult(OverviewEffect.ShowError(it))) },
                    onRightAction = { homeworkList ->
                        val currentTime = dateManager.fetchCurrentInstant()
                        val homeworks = homeworkList.map { homework ->
                            val status = HomeworkStatus.calculate(
                                isDone = homework.isDone,
                                completeDate = homework.completeDate,
                                deadline = homework.deadline,
                                currentTime = currentDate.setHoursAndMinutes(currentTime),
                            )
                            return@map homework.mapToUi().convertToDetails(status)
                        }
                        val groupedHomeworks = homeworks.groupBy { homework ->
                            homework.deadline.startThisDay()
                        }
                        val homeworksMap = buildMap {
                            targetTimeRange.periodDates().forEach { date ->
                                val homeworksByDate = groupedHomeworks[date] ?: emptyList<HomeworkDetailsUi>()
                                val dailyHomeworks = DailyHomeworksUi(
                                    dailyStatus = DailyHomeworksStatus.calculate(
                                        targetDate = date,
                                        currentDate = currentTime.startThisDay(),
                                        homeworkStatuses = homeworksByDate.map { it.status }
                                    ),
                                    homeworks = homeworksByDate,
                                )
                                put(date, dailyHomeworks)
                            }
                        }
                        val homeworksScope = calculateHomeworkScope(homeworksMap)

                        send(ActionResult(OverviewAction.UpdateHomeworks(homeworksMap, homeworksScope)))

                        cycleUpdateJob = cycleUpdateHomeworkStatus(homeworksMap)
                            .onEach { send(it) }
                            .launchIn(this)
                            .apply { start() }
                    },
                )
            }
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
            ).collect { workResult ->
                emit(workResult)
            }
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateErrorsLoading(true)))
        }

        private fun loadTodosWork(currentDate: Instant) = channelFlow<OverviewWorkResult> {
            var cycleUpdateJob: Job? = null
            val targetTimeRange = TimeRange(
                from = currentDate.startOfWeek(),
                to = currentDate.endOfWeek(),
            )
            todoInteractor.fetchActiveAndTimeRangeTodos(targetTimeRange).collect { todoEither ->
                cycleUpdateJob?.cancelAndJoin()
                todoEither.handle(
                    onLeftAction = { send(EffectResult(OverviewEffect.ShowError(it))) },
                    onRightAction = { todoList ->
                        val todos = todoList.map { todo ->
                            val currentTime = dateManager.fetchCurrentInstant()
                            val duration = todo.deadline?.let { it - currentTime }
                            val status = TodoStatus.calculate(
                                isDone = todo.isDone,
                                deadline = todo.deadline,
                                currentTime = currentTime,
                            )
                            return@map todo.mapToUi(status, duration)
                        }

                        send(ActionResult(OverviewAction.UpdateTodos(todos)))

                        cycleUpdateJob = cycleUpdateTodoStatus(todos)
                            .onEach { send(it) }
                            .launchIn(this)
                            .apply { start() }
                    },
                )
            }
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateTasksLoading(true)))
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        private fun loadSharedHomeworksWork() = flow {
            val sharedHomeworksFlow = shareInteractor.fetchSharedHomeworks()
            val friendsFlow = usersInteractor.fetchAllFriends()

            sharedHomeworksFlow.flatMapLatestWithResult(
                secondFlow = friendsFlow,
                onError = { OverviewEffect.ShowError(it) },
                onData = { homeworks, friends ->
                    val allFriends = friends.map { it.mapToUi() }
                    OverviewAction.UpdateSharedHomeworks(homeworks.mapToUi(), allFriends)
                },
            ).collect { workResult ->
                emit(workResult)
            }
        }.onStart {
            emit(ActionResult(OverviewAction.UpdateShareLoading(true)))
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
            homeworksInteractor.updateHomework(homework.convertToBase().mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
            )
        }

        private fun updateTodoWork(todo: TodoDetailsUi) = flow {
            todoInteractor.updateTodo(todo.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) },
            )
        }

        private fun cycleUpdateHomeworkStatus(homeworksMap: Map<Instant, DailyHomeworksUi>) =
            flow {
                val updatedHomeworksMap = homeworksMap.toMutableMap()
                while (currentCoroutineContext().isActive) {
                    delay(Delay.UPDATE_TASK_STATUS)

                    var isUpdated = false
                    val currentInstant = dateManager.fetchCurrentInstant()
                    val updatedDailyHomeworks = updatedHomeworksMap.values.toList()
                    val updatedHomeworks = updatedDailyHomeworks.map { it.homeworks }.extractAllItem()

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
                                val newHomeworks = updatedHomeworksMap[date]!!.homeworks.toMutableList().apply {
                                    remove(homework)
                                    add(newHomework)
                                }
                                updatedHomeworksMap[date] = updatedHomeworksMap[date]!!.copy(
                                    dailyStatus = DailyHomeworksStatus.calculate(
                                        targetDate = date,
                                        currentDate = currentInstant.startThisDay(),
                                        homeworkStatuses = newHomeworks.map { it.status },
                                    ),
                                    homeworks = newHomeworks,
                                )
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
            while (currentCoroutineContext().isActive) {
                delay(Delay.UPDATE_TASK_STATUS)

                val currentInstant = dateManager.fetchCurrentInstant()

                val updatedTodos = todos.map { todo ->
                    val newStatus = TodoStatus.calculate(todo.isDone, todo.deadline, currentInstant)
                    val duration = if (todo.status != TodoStatus.COMPLETE) {
                        todo.deadline?.let { it - currentInstant }
                    } else {
                        null
                    }
                    return@map todo.copy(status = newStatus, toDeadlineDuration = duration)
                }
                emit(ActionResult(OverviewAction.UpdateTodos(updatedTodos)))
            }
        }

        private fun calculateHomeworkScope(
            homeworksMap: Map<Instant, DailyHomeworksUi>
        ): HomeworkScopeUi {
            return HomeworkScopeUi(
                theoreticalTasks = homeworksMap.mapValues { homeworkEntry ->
                    homeworkEntry.value.homeworks.sumOf { homework ->
                        return@sumOf homework.theoreticalTasks.components.fetchAllTasks().size
                    }
                },
                practicalTasks = homeworksMap.mapValues { homeworkEntry ->
                    homeworkEntry.value.homeworks.sumOf { homework ->
                        return@sumOf homework.practicalTasks.components.fetchAllTasks().size
                    }
                },
                presentationTasks = homeworksMap.mapValues { homeworkEntry ->
                    homeworkEntry.value.homeworks.sumOf { homework ->
                        return@sumOf homework.presentationTasks.components.fetchAllTasks().size
                    }
                },
            )
        }

        private fun shareHomeworksWork(sentMediatedHomeworks: SentMediatedHomeworksDetailsUi) = flow {
            shareInteractor.shareHomeworks(sentMediatedHomeworks.mapToDomain()).handle(
                onLeftAction = { emit(EffectResult(OverviewEffect.ShowError(it))) }
            )
        }
    }
}

internal sealed class OverviewWorkCommand : WorkCommand {
    data class LoadHomeworks(val currentDate: Instant) : OverviewWorkCommand()
    data class LoadHomeworkErrors(val currentDate: Instant) : OverviewWorkCommand()
    data class LoadTodos(val currentDate: Instant) : OverviewWorkCommand()
    data object LoadSharedHomeworks : OverviewWorkCommand()
    data class LoadActiveSchedule(val currentDate: Instant) : OverviewWorkCommand()
    data class UpdateHomework(val homework: HomeworkDetailsUi) : OverviewWorkCommand()
    data class UpdateTodo(val todo: TodoDetailsUi) : OverviewWorkCommand()
    data class ShareHomeworks(val sentMediatedHomeworks: SentMediatedHomeworksDetailsUi) : OverviewWorkCommand()
}

internal typealias OverviewWorkResult = WorkResult<OverviewAction, OverviewEffect>