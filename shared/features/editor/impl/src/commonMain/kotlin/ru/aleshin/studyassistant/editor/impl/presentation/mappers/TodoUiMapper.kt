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

package ru.aleshin.studyassistant.editor.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.entities.tasks.TodoNotifications
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.TodoNotificationsUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.TodoUi

/**
 * @author Stanislav Aleshin on 26.07.2024.
 */
internal fun Todo.mapToUi() = TodoUi(
    uid = uid,
    deadline = deadline,
    name = name,
    description = description,
    priority = priority,
    notifications = notifications.mapToUi(),
    isDone = isDone,
    completeDate = completeDate,
)

internal fun TodoNotifications.mapToUi() = TodoNotificationsUi(
    beforeStart = beforeStart,
    fifteenMinutesBefore = fifteenMinutesBefore,
    oneHourBefore = oneHourBefore,
    threeHourBefore = threeHourBefore,
    oneDayBefore = oneDayBefore,
    oneWeekBefore = oneWeekBefore,
)
internal fun TodoUi.mapToDomain() = Todo(
    uid = uid,
    deadline = deadline,
    name = name,
    description = description,
    priority = priority,
    notifications = notifications.mapToDomain(),
    isDone = isDone,
    completeDate = completeDate,
)

internal fun TodoNotificationsUi.mapToDomain() = TodoNotifications(
    beforeStart = beforeStart,
    fifteenMinutesBefore = fifteenMinutesBefore,
    oneHourBefore = oneHourBefore,
    threeHourBefore = threeHourBefore,
    oneDayBefore = oneDayBefore,
    oneWeekBefore = oneWeekBefore,
)