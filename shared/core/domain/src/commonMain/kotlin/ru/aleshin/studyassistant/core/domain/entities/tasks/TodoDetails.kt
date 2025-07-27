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

package ru.aleshin.studyassistant.core.domain.entities.tasks

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalShort
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis

/**
 * @author Stanislav Aleshin on 20.06.2024.
 */
data class TodoDetails(
    val uid: UID,
    val deadline: Instant?,
    val deadlineTimeLeft: Millis?,
    val progress: Float,
    val name: String,
    val description: String?,
    val status: TodoStatus,
    val priority: TaskPriority,
    val notifications: TodoNotifications,
    val linkedGoal: GoalShort?,
    val isDone: Boolean = false,
    val completeDate: Instant? = null,
    val updatedAt: Long,
)

fun Todo.convertToDetails(
    deadlineTimeLeft: Millis?,
    status: TodoStatus,
    progress: Float,
    linkedGoal: GoalShort?,
) = TodoDetails(
    uid = uid,
    deadline = deadline,
    deadlineTimeLeft = deadlineTimeLeft,
    progress = progress,
    name = name,
    description = description,
    status = status,
    priority = priority,
    notifications = notifications,
    isDone = isDone,
    linkedGoal = linkedGoal,
    completeDate = completeDate,
    updatedAt = updatedAt,
)