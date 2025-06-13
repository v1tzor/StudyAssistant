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

package ru.aleshin.studyassistant.tasks.impl.domain.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.tasks.DetailsGroupedTodos
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.convertToDetails
import ru.aleshin.studyassistant.core.domain.managers.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
internal interface TodoInteractor {

    suspend fun fetchWeekGroupedTodosByTimeRange(timeRange: TimeRange): FlowDomainResult<TasksFailures, DetailsGroupedTodos>
    suspend fun fetchCompletedTodos(): FlowDomainResult<TasksFailures, List<Todo>>
    suspend fun updateTodoDone(todo: Todo): UnitDomainResult<TasksFailures>

    class Base(
        private val todoRepository: TodoRepository,
        private val todoReminderManager: TodoReminderManager,
        private val goalsRepository: DailyGoalsRepository,
        private val usersRepository: UsersRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: TasksEitherWrapper,
    ) : TodoInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchWeekGroupedTodosByTimeRange(timeRange: TimeRange) = eitherWrapper.wrapFlow {
            val ticker = dateManager.secondTicker()
            val currentTime = dateManager.fetchCurrentInstant()
            val shortGoalsFlow = goalsRepository.fetchShortActiveDailyGoals(targetUser)
            val weekCompletedTodosFlow = todoRepository.fetchCompletedTodos(timeRange, targetUser).map { todos ->
                todos.sortedBy { it.deadline }
            }
            val activeTodosFlow = todoRepository.fetchActiveTodos(targetUser).map { todos ->
                todos.sortedBy { it.deadline }
            }

            return@wrapFlow combine(
                weekCompletedTodosFlow,
                activeTodosFlow,
                shortGoalsFlow,
                ticker,
            ) { completedTodos, activeTodos, goals, _ ->
                val runningTodos = mutableListOf<Todo>()
                val errorTodos = mutableListOf<Todo>()
                activeTodos.forEach { todo ->
                    val deadline = todo.deadline
                    if (deadline != null) {
                        if (deadline >= currentTime) runningTodos.add(todo) else errorTodos.add(todo)
                    } else {
                        runningTodos.add(todo)
                    }
                }

                DetailsGroupedTodos(
                    completedTodos = completedTodos.map { todo ->
                        todo.convertToDetails(
                            deadlineTimeLeft = null,
                            status = TodoStatus.COMPLETE,
                            progress = 1f,
                            linkedGoal = null,
                        )
                    },
                    runningTodos = runningTodos.map { todo ->
                        val leftTime = todo.deadline?.let { deadline ->
                            deadline.toEpochMilliseconds() - currentTime.toEpochMilliseconds()
                        }
                        val progress = todo.deadline?.let { deadline ->
                            currentTime.toEpochMilliseconds() / deadline.toEpochMilliseconds().toFloat()
                        }?.coerceIn(0f, 1f) ?: 0f
                        todo.convertToDetails(
                            deadlineTimeLeft = leftTime,
                            status = TodoStatus.IN_PROGRESS,
                            progress = progress,
                            linkedGoal = goals.find { it.contentId == todo.uid },
                        )
                    },
                    errorTodos = errorTodos.map { todo ->
                        val expiredTime = todo.deadline?.let { deadline ->
                            deadline.toEpochMilliseconds() - currentTime.toEpochMilliseconds()
                        }
                        todo.convertToDetails(
                            deadlineTimeLeft = expiredTime,
                            status = TodoStatus.NOT_COMPLETE,
                            progress = 0f,
                            linkedGoal = goals.find { it.contentId == todo.uid },
                        )
                    },
                )
            }
        }

        override suspend fun fetchCompletedTodos() = eitherWrapper.wrapFlow {
            todoRepository.fetchCompletedTodos(null, targetUser)
        }

        override suspend fun updateTodoDone(todo: Todo) = eitherWrapper.wrapUnit {
            val currentTime = dateManager.fetchCurrentInstant()
            val linkedGoal = goalsRepository.fetchGoalByContentId(todo.uid, targetUser).first()
            if (todo.isDone) {
                val canceledTodo = todo.copy(
                    isDone = false,
                    completeDate = null,
                )
                if (linkedGoal != null && linkedGoal.targetDate >= currentTime.startThisDay()) {
                    cancelLinkedGoal(linkedGoal)
                }

                todoRepository.addOrUpdateTodo(canceledTodo, targetUser)
                todoReminderManager.clearAllReminders(todo.uid)
            } else {
                val completedTodo = todo.copy(
                    isDone = true,
                    completeDate = currentTime,
                )
                if (linkedGoal != null && !linkedGoal.isDone) completeLinkedGoal(linkedGoal)

                todoRepository.addOrUpdateTodo(completedTodo, targetUser)
                todoReminderManager.scheduleReminders(todo.uid, todo.name, todo.deadline, todo.notifications)
            }
        }

        private suspend fun completeLinkedGoal(linkedGoal: Goal) {
            val currentTime = dateManager.fetchCurrentInstant()
            val updatedGoal = linkedGoal.copy(
                time = when (linkedGoal.time) {
                    is GoalTime.Stopwatch -> with(linkedGoal.time as GoalTime.Stopwatch) {
                        val stopTime = startTimePoint.toEpochMilliseconds()
                        val timeAfterStop = currentTime.toEpochMilliseconds() - stopTime
                        return@with copy(
                            pastStopTime = pastStopTime + timeAfterStop,
                            isActive = false,
                        )
                    }
                    is GoalTime.Timer -> with(linkedGoal.time as GoalTime.Timer) {
                        val stopTime = startTimePoint.toEpochMilliseconds()
                        val timeAfterStop = currentTime.toEpochMilliseconds() - stopTime
                        return@with copy(
                            pastStopTime = pastStopTime + timeAfterStop,
                            isActive = false,
                        )
                    }
                    is GoalTime.None -> GoalTime.None
                },
                isDone = true,
                completeDate = currentTime,
            )
            goalsRepository.addOrUpdateGoal(updatedGoal, targetUser)
        }

        private suspend fun cancelLinkedGoal(linkedGoal: Goal) {
            val currentTime = dateManager.fetchCurrentInstant()
            val canceledGoal = linkedGoal.copy(
                time = when (linkedGoal.time) {
                    is GoalTime.Stopwatch -> (linkedGoal.time as GoalTime.Stopwatch).copy(
                        pastStopTime = 0L,
                        startTimePoint = currentTime,
                        isActive = false,
                    )
                    is GoalTime.Timer -> (linkedGoal.time as GoalTime.Timer).copy(
                        pastStopTime = 0L,
                        startTimePoint = currentTime,
                        isActive = false,
                    )
                    is GoalTime.None -> GoalTime.None
                },
                isDone = false,
                completeDate = null,
            )
            goalsRepository.addOrUpdateGoal(canceledGoal, targetUser)
        }
    }
}