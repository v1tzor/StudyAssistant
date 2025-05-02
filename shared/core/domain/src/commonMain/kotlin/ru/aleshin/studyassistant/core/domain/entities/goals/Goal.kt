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

package ru.aleshin.studyassistant.core.domain.entities.goals

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo

/**
 * @author Stanislav Aleshin on 18.04.2025.
 */
data class Goal(
    val uid: UID,
    val contentType: GoalType,
    val contentHomework: Homework? = null,
    val contentTodo: Todo? = null,
    val number: Int = 0,
    val targetDate: Instant,
    val desiredTime: Millis?,
    val time: GoalTime,
    val completeAfterTimeElapsed: Boolean = false,
    val isDone: Boolean = false,
    val completeDate: Instant?,
)