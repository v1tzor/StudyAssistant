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

package ru.aleshin.studyassistant.tasks.impl.presentation.models.goals

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis

/**
 * @author Stanislav Aleshin on 01.06.2025.
 */
@Immutable
@Serializable
internal sealed class GoalTimeUi {

    abstract val type: GoalTime.Type

    @Serializable
    data class Timer(
        val targetTime: Millis,
        val pastStopTime: Millis = 0,
        val startTimePoint: Instant,
        val isActive: Boolean = false,
    ) : GoalTimeUi() {
        override val type = GoalTime.Type.TIMER
    }

    @Serializable
    data class Stopwatch(
        val pastStopTime: Millis = 0,
        val startTimePoint: Instant,
        val isActive: Boolean = false,
    ) : GoalTimeUi() {
        override val type = GoalTime.Type.STOPWATCH
    }

    @Serializable
    data object None : GoalTimeUi() {
        override val type = GoalTime.Type.NONE
    }
}