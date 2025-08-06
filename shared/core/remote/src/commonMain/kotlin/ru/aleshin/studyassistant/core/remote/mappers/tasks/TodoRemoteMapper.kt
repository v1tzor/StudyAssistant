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

package ru.aleshin.studyassistant.core.remote.mappers.tasks

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoNotificationsPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.TodoPojoDetails

/**
 * @author Stanislav Aleshin on 06.07.2025.
 */
fun TodoPojo.convertToDetails() = TodoPojoDetails(
    uid = id,
    userId = userId,
    deadline = deadline,
    name = name,
    description = description,
    priority = priority,
    notifications = notifications.fromJson<TodoNotificationsPojo>(),
    done = done,
    createdAt = createdAt,
    completeDate = completeDate,
    updatedAt = updatedAt,
)

fun TodoPojoDetails.convertToBase() = TodoPojo(
    id = uid,
    userId = userId,
    deadline = deadline,
    name = name,
    description = description,
    priority = priority,
    notifications = notifications.toJson<TodoNotificationsPojo>(),
    done = done,
    createdAt = createdAt,
    completeDate = completeDate,
    updatedAt = updatedAt,
)