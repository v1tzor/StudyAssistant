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

package ru.aleshin.studyassistant.editor.impl.presentation.models.classes

import ru.aleshin.studyassistant.core.common.extensions.epochTimeDuration
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.presentation.models.organizations.NumberedDurationUi

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
internal object FastEditDurationMath {

    fun classDurations(timeRanges: List<TimeRange>): List<Pair<Int, Millis>> {
        return timeRanges.mapIndexed { index, timeRange ->
            index + 1 to epochTimeDuration(timeRange)
        }
    }

    fun breakDurations(timeRanges: List<TimeRange>): List<Pair<Int, Millis>> {
        return timeRanges.zipWithNext { current, next ->
            epochTimeDuration(current.to, next.from).coerceAtLeast(0L)
        }.mapIndexed { index, duration ->
            index + 1 to duration
        }
    }

    fun groupedDurations(
        durations: List<Pair<Int, Millis>>,
    ): Pair<Millis?, List<NumberedDurationUi>> {
        if (durations.isEmpty()) return null to emptyList()
        val grouped = durations.groupBy { duration -> duration.second }
        val mode = grouped.maxBy { entry -> entry.value.size }.key
        val megaCloned = durations.size > 1 && grouped.size == 1 && mode >= MEGA_DURATION_MS
        if (megaCloned) {
            return DEFAULT_CLASS_MS to emptyList()
        }
        val specifics = durations.filter { duration -> duration.second != mode }.map { duration ->
            NumberedDurationUi(number = duration.first, duration = duration.second)
        }
        return mode to specifics
    }

    private const val MEGA_DURATION_MS = 3 * 60 * 60 * 1000L
    private const val DEFAULT_CLASS_MS = 45 * 60 * 1000L
}
