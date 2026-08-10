/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.core.domain.interactors

import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
interface TodoCompletionInteractor {

    suspend fun setDone(todoId: UID, isDone: Boolean)

    class Base(
        private val todoRepository: TodoRepository,
        private val todoReminderManager: TodoReminderManager,
        private val goalsRepository: DailyGoalsRepository,
        private val dateManager: DateManager,
    ) : TodoCompletionInteractor {

        override suspend fun setDone(todoId: UID, isDone: Boolean) {
            val todo = todoRepository.fetchTodoById(todoId).first() ?: return
            if (todo.isDone == isDone) return

            val currentTime = dateManager.fetchCurrentInstant()
            val linkedGoal = goalsRepository.fetchGoalByContentId(todo.uid).first()
            if (isDone) {
                val completedTodo = todo.copy(
                    isDone = true,
                    completeDate = currentTime,
                    updatedAt = currentTime.toEpochMilliseconds(),
                )
                if (linkedGoal != null && !linkedGoal.isDone) completeLinkedGoal(linkedGoal)

                todoRepository.addOrUpdateTodo(completedTodo)
                todoReminderManager.scheduleReminders(
                    todo.uid,
                    todo.name,
                    todo.deadline,
                    todo.notifications,
                )
            } else {
                val reopenedTodo = todo.copy(
                    isDone = false,
                    completeDate = null,
                    updatedAt = currentTime.toEpochMilliseconds(),
                )
                if (linkedGoal != null && linkedGoal.targetDate >= currentTime.startThisDay()) {
                    reopenLinkedGoal(linkedGoal)
                }

                todoRepository.addOrUpdateTodo(reopenedTodo)
                todoReminderManager.clearAllReminders(todo.uid)
            }
        }

        private suspend fun completeLinkedGoal(linkedGoal: Goal) {
            val currentTime = dateManager.fetchCurrentInstant()
            val updatedGoal = linkedGoal.copy(
                time = when (linkedGoal.time) {
                    is GoalTime.Stopwatch -> with(linkedGoal.time) {
                        val stopTime = startTimePoint.toEpochMilliseconds()
                        val timeAfterStop = currentTime.toEpochMilliseconds() - stopTime
                        copy(
                            pastStopTime = pastStopTime + timeAfterStop,
                            isActive = false,
                        )
                    }

                    is GoalTime.Timer -> with(linkedGoal.time) {
                        val stopTime = startTimePoint.toEpochMilliseconds()
                        val timeAfterStop = currentTime.toEpochMilliseconds() - stopTime
                        copy(
                            pastStopTime = pastStopTime + timeAfterStop,
                            isActive = false,
                        )
                    }

                    is GoalTime.None -> GoalTime.None
                },
                isDone = true,
                completeDate = currentTime,
                updatedAt = currentTime.toEpochMilliseconds(),
            )
            goalsRepository.addOrUpdateGoal(updatedGoal)
        }

        private suspend fun reopenLinkedGoal(linkedGoal: Goal) {
            val currentTime = dateManager.fetchCurrentInstant()
            val reopenedGoal = linkedGoal.copy(
                time = when (linkedGoal.time) {
                    is GoalTime.Stopwatch -> linkedGoal.time.copy(
                        pastStopTime = 0L,
                        startTimePoint = currentTime,
                        isActive = false,
                    )

                    is GoalTime.Timer -> linkedGoal.time.copy(
                        pastStopTime = 0L,
                        startTimePoint = currentTime,
                        isActive = false,
                    )

                    is GoalTime.None -> GoalTime.None
                },
                isDone = false,
                completeDate = null,
                updatedAt = currentTime.toEpochMilliseconds(),
            )
            goalsRepository.addOrUpdateGoal(reopenedGoal)
        }
    }
}
