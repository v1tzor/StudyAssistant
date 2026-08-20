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

package ru.aleshin.studyassistant.schedule.impl.presentation.models.importing

import kotlinx.datetime.LocalTime
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.presentation.models.organizations.NumberedDurationUi
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
internal object ImportFastEditDurationMath {

    fun classDurations(classes: List<ScheduleImportClassUi>): List<Pair<Int, Millis>> {
        return sortedClasses(classes).mapIndexed { index, classModel ->
            index + 1 to (durationMillis(classModel.startTime, classModel.endTime) ?: 0L)
        }
    }

    fun breakDurations(classes: List<ScheduleImportClassUi>): List<Pair<Int, Millis>> {
        return sortedClasses(classes).zipWithNext { current, next ->
            durationMillis(current.endTime, next.startTime)?.coerceAtLeast(0L) ?: 0L
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

    fun firstStartInstant(classes: List<ScheduleImportClassUi>): Instant? {
        val start = sortedClasses(classes).firstOrNull()?.startTime ?: return null
        val parsed = parseClock(start) ?: return null
        return Clock.System.now().startThisDay().setHoursAndMinutes(parsed)
    }

    private fun sortedClasses(classes: List<ScheduleImportClassUi>): List<ScheduleImportClassUi> {
        return classes.sortedBy { classModel ->
            parseClock(classModel.startTime)?.toMinutes() ?: Int.MAX_VALUE
        }
    }

    private fun durationMillis(start: String, end: String): Millis? {
        val from = parseClock(start) ?: return null
        val to = parseClock(end) ?: return null
        val minutes = to.toMinutes() - from.toMinutes()
        if (minutes < 0) return null
        return minutes * MILLIS_IN_MINUTE
    }

    private fun parseClock(value: String?): LocalTime? {
        val raw = value?.trim().orEmpty().replace('.', ':')
        if (raw.isEmpty()) return null
        val parts = raw.split(':')
        if (parts.size !in 2..3) return null
        val hour = parts[0].toIntOrNull() ?: return null
        val minute = parts[1].toIntOrNull() ?: return null
        return runCatching { LocalTime(hour, minute) }.getOrNull()
    }

    private fun LocalTime.toMinutes(): Int = hour * 60 + minute

    private const val MILLIS_IN_MINUTE = 60_000L
    private const val MEGA_DURATION_MS = 3 * 60 * 60 * 1000L
    private const val DEFAULT_CLASS_MS = 45 * 60 * 1000L
}
