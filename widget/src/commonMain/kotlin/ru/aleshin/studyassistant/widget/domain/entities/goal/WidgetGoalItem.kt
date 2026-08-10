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

package ru.aleshin.studyassistant.widget.domain.entities.goal

import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
data class WidgetGoalItem(
    val uid: UID,
    val number: Int,
    val contentType: GoalType,
    val title: String?,
    val color: Int?,
    val timeType: GoalTime.Type,
    val elapsedTime: Long,
    val targetTime: Long?,
    val progress: Float,
    val status: WidgetGoalStatus,
)
