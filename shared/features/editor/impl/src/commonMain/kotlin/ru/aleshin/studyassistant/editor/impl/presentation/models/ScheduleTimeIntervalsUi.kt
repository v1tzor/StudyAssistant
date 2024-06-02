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

package ru.aleshin.studyassistant.editor.impl.presentation.models

import entities.organizations.Millis
import extensions.max
import extensions.min
import kotlinx.datetime.Instant
import platform.JavaSerializable

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal data class ScheduleTimeIntervalsUi(
    val firstClassTime: Instant? = null,
    val baseClassDuration: Millis? = null,
    val baseBreakDuration: Millis? = null,
    val specificClassDuration: List<NumberedDurationUi> = emptyList(),
    val specificBreakDuration: List<NumberedDurationUi> = emptyList(),
) : JavaSerializable {

    fun minClassDuration(): Millis? {
        val specificMin = specificClassDuration.minOfOrNull { it.duration }
        val baseMin = baseClassDuration
        return min(specificMin, baseMin)
    }

    fun minBreakDuration(): Millis? {
        val specificMin = specificBreakDuration.minOfOrNull { it.duration }
        val baseMin = baseBreakDuration
        return min(specificMin, baseMin)
    }

    fun maxClassDuration(): Millis? {
        val specificMin = specificClassDuration.maxOfOrNull { it.duration }
        val baseMin = baseClassDuration
        return max(specificMin, baseMin)
    }

    fun maxBreakDuration(): Millis? {
        val specificMin = specificBreakDuration.maxOfOrNull { it.duration }
        val baseMin = baseBreakDuration
        return max(specificMin, baseMin)
    }
}
