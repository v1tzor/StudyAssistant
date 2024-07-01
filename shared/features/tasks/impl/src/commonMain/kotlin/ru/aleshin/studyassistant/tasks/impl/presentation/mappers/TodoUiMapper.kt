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

package ru.aleshin.studyassistant.tasks.impl.presentation.mappers

import entities.tasks.Todo
import entities.tasks.TodoStatus
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TodoErrors
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoErrorsUi

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
internal fun Todo.mapToUi(status: TodoStatus) = TodoDetailsUi(
    uid = uid,
    deadline = deadline,
    name = name,
    status = status,
    priority = priority,
    notification = notification,
    isDone = isDone,
    completeDate = completeDate,
)

internal fun TodoDetailsUi.mapToDomain() = Todo(
    uid = uid,
    deadline = deadline,
    name = name,
    priority = priority,
    notification = notification,
    isDone = isDone,
    completeDate = completeDate,
)

internal fun TodoErrors.mapToUi() = TodoErrorsUi(
    overdueTodos = overdueTodos.map { it.mapToUi(TodoStatus.NOT_COMPLETE) },
)