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

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
internal class FastEditDurationMathTest {

    @Test
    fun classDurations_emitOneEntryPerLesson() {
        val ranges = listOf(
            range(hours = 8, durationHours = 0, durationMinutes = 40),
            range(hours = 8, minutes = 50, durationHours = 0, durationMinutes = 40),
            range(hours = 9, minutes = 50, durationHours = 0, durationMinutes = 40),
        )

        val durations = FastEditDurationMath.classDurations(ranges)

        assertEquals(3, durations.size)
        assertEquals(listOf(1, 2, 3), durations.map { duration -> duration.first })
        assertTrue(durations.all { duration -> duration.second == 40 * 60_000L })
    }

    @Test
    fun groupedDurations_replaceClonedMegaSpanWithDefaultClassLength() {
        val mega = 9 * 60 * 60_000L
        val durations = List(8) { index -> index + 1 to mega }

        val grouped = FastEditDurationMath.groupedDurations(durations)

        assertEquals(45 * 60_000L, grouped.first)
        assertTrue(grouped.second.isEmpty())
    }

    @Test
    fun groupedDurations_keepSpecificExceptions() {
        val forty = 40 * 60_000L
        val thirty = 30 * 60_000L
        val durations = listOf(
            1 to forty,
            2 to forty,
            3 to thirty,
            4 to forty,
        )

        val grouped = FastEditDurationMath.groupedDurations(durations)

        assertEquals(forty, grouped.first)
        assertEquals(listOf(3 to thirty), grouped.second.map { item -> item.number to item.duration })
    }

    @Test
    fun breakDurations_keepGapsBetweenLessons() {
        val ranges = listOf(
            range(hours = 8, durationMinutes = 40),
            range(hours = 8, minutes = 50, durationMinutes = 40),
            range(hours = 9, minutes = 50, durationMinutes = 40),
        )

        val breaks = FastEditDurationMath.breakDurations(ranges)

        assertEquals(2, breaks.size)
        assertEquals(10 * 60_000L, breaks[0].second)
        assertEquals(20 * 60_000L, breaks[1].second)
    }

    private fun range(
        hours: Int,
        minutes: Int = 0,
        durationHours: Int = 0,
        durationMinutes: Int = 40,
    ): TimeRange {
        val day = Clock.System.now().startThisDay()
        val start = day.setHoursAndMinutes(hour = hours, minute = minutes)
        val end = Instant.fromEpochMilliseconds(
            start.toEpochMilliseconds() + ((durationHours * 60L) + durationMinutes) * 60_000L,
        )
        return TimeRange(from = start, to = end)
    }
}
