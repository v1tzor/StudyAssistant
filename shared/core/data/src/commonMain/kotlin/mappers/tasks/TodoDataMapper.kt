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

package mappers.tasks

import entities.tasks.TaskPriority
import entities.tasks.Todo
import extensions.mapEpochTimeToInstant
import models.tasks.TodoData
import ru.aleshin.studyassistant.sqldelight.tasks.TodoEntity

/**
 * @author Stanislav Aleshin on 01.07.2024.
 */
fun TodoData.mapToDomain() = Todo(
    uid = uid,
    deadline = deadline?.mapEpochTimeToInstant(),
    name = name,
    priority = TaskPriority.valueOf(priority),
    notification = notification,
    isDone = done,
    completeDate = completeDate?.mapEpochTimeToInstant(),
)

fun Todo.mapToData() = TodoData(
    uid = uid,
    deadline = deadline?.toEpochMilliseconds(),
    name = name,
    priority = priority.name,
    notification = notification,
    done = isDone,
    completeDate = completeDate?.toEpochMilliseconds(),
)

fun TodoData.mapToLocalData() = TodoEntity(
    uid = uid,
    deadline = deadline,
    name = name,
    priority = priority,
    notification = if (notification) 1L else 0L,
    is_done = if (done) 1L else 0L,
    complete_date = completeDate,
)

fun TodoEntity.mapToBaseData() = TodoData(
    uid = uid,
    deadline = deadline,
    name = name,
    priority = priority,
    notification = notification == 1L,
    done = is_done == 1L,
    completeDate = complete_date,
)