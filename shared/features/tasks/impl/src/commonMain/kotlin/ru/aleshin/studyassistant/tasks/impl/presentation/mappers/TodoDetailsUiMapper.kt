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

package ru.aleshin.studyassistant.tasks.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoDetails
import ru.aleshin.studyassistant.core.presentation.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.tasks.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
internal fun TodoDetails.mapToUi() = TodoDetailsUi(
    uid = uid,
    deadline = deadline,
    deadlineTimeLeft = deadlineTimeLeft,
    progress = progress,
    name = name,
    description = description,
    status = status,
    priority = priority,
    notifications = notifications.mapToUi(),
    linkedGoal = linkedGoal?.mapToUi(),
    isDone = isDone,
    createdAt = createdAt,
    completeDate = completeDate,
    updatedAt = updatedAt,
)

internal fun TodoDetailsUi.mapToDomain() = Todo(
    uid = uid,
    deadline = deadline,
    name = name,
    description = description,
    priority = priority,
    notifications = notifications.mapToDomain(),
    isDone = isDone,
    createdAt = createdAt,
    completeDate = completeDate,
    updatedAt = updatedAt,
)
