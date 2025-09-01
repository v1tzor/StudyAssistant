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

package ru.aleshin.studyassistant.editor.impl.presentation.models.tasks

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority

/**
 * @author Stanislav Aleshin on 26.07.2024.
 */
@Immutable
@Serializable
internal data class EditTodoUi(
    val uid: UID,
    val deadline: Instant? = null,
    val name: String = "",
    val description: String? = null,
    val priority: TaskPriority = TaskPriority.STANDARD,
    val notifications: TodoNotificationsUi = TodoNotificationsUi(),
    val isDone: Boolean = false,
    val createdAt: Instant,
    val completeDate: Instant? = null,
) {

    fun isValid() = name.isNotBlank()

    companion object {
        fun createEditModel(
            uid: UID?,
            enableNotifications: Boolean = true,
            createdAt: Instant,
        ) = EditTodoUi(
            uid = uid ?: "",
            createdAt = createdAt,
            notifications = TodoNotificationsUi(beforeStart = enableNotifications),
        )
    }
}

internal fun TodoUi.convertToEdit() = EditTodoUi(
    uid = uid,
    deadline = deadline,
    name = name,
    description = description,
    priority = priority,
    notifications = notifications,
    isDone = isDone,
    completeDate = completeDate,
    createdAt = createdAt,
)

internal fun EditTodoUi.convertToBase() = TodoUi(
    uid = uid,
    deadline = deadline,
    name = name,
    description = description,
    priority = priority,
    notifications = notifications,
    isDone = isDone,
    completeDate = completeDate,
    createdAt = createdAt,
)