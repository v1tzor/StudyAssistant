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
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.managers.TodoReminderManager
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
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
        private val usersRepository: UsersRepository,
        private val goalsRepository: DailyGoalsRepository,
        private val todoReminderManager: TodoReminderManager,
        private val eitherWrapper: EditorEitherWrapper,
    ) : TodoInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addOrUpdateTodo(todo: Todo) = eitherWrapper.wrap {
            todoRepository.addOrUpdateTodo(todo, targetUser).apply {
                todoReminderManager.scheduleReminders(this, todo.name, todo.deadline, todo.notifications)
                val linkedGoal = goalsRepository.fetchGoalByContentId(todo.uid, targetUser).first()
                if (linkedGoal != null) {
                    goalsRepository.addOrUpdateGoal(linkedGoal.copy(contentTodo = todo), targetUser)
                }
            }
        }

        override suspend fun fetchTodoById(uid: UID) = eitherWrapper.wrapFlow {
            todoRepository.fetchTodoById(uid, targetUser)
        }

        override suspend fun deleteTodo(targetId: UID) = eitherWrapper.wrapUnit {
            todoRepository.deleteTodo(targetId, targetUser)
            todoReminderManager.clearAllReminders(targetId)
        }
    }
}