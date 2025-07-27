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

package ru.aleshin.studyassistant.editor.impl.domain.interactors

import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.managers.reminders.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 26.07.2024.
 */
internal interface TodoInteractor {

    suspend fun addOrUpdateTodo(todo: Todo): DomainResult<EditorFailures, UID>
    suspend fun fetchTodoById(uid: UID): FlowDomainResult<EditorFailures, Todo?>
    suspend fun deleteTodo(targetId: UID): UnitDomainResult<EditorFailures>

    class Base(
        private val todoRepository: TodoRepository,
        private val goalsRepository: DailyGoalsRepository,
        private val todoReminderManager: TodoReminderManager,
        private val dateManager: DateManager,
        private val eitherWrapper: EditorEitherWrapper,
    ) : TodoInteractor {

        override suspend fun addOrUpdateTodo(todo: Todo) = eitherWrapper.wrap {
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val updatedTodo = todo.copy(updatedAt = updatedAt)
            todoRepository.addOrUpdateTodo(updatedTodo).apply {
                todoReminderManager.scheduleReminders(
                    targetId = this,
                    name = updatedTodo.name,
                    deadline = updatedTodo.deadline,
                    notifications = updatedTodo.notifications,
                )
                val linkedGoal = goalsRepository.fetchGoalByContentId(updatedTodo.uid).first()
                if (linkedGoal != null) {
                    val updatedGoal = linkedGoal.copy(contentTodo = updatedTodo, updatedAt = updatedAt)
                    goalsRepository.addOrUpdateGoal(updatedGoal)
                }
            }
        }

        override suspend fun fetchTodoById(uid: UID) = eitherWrapper.wrapFlow {
            todoRepository.fetchTodoById(uid)
        }

        override suspend fun deleteTodo(targetId: UID) = eitherWrapper.wrapUnit {
            todoRepository.deleteTodo(targetId)
            todoReminderManager.clearAllReminders(targetId)
        }
    }
}