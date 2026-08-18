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

package ru.aleshin.studyassistant.core.common.functional

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * @author Stanislav Aleshin on 18.08.2026.
 */
class TimeRangeTest {

    private val zone = TimeZone.UTC

    @Test
    fun timeEquals_sameStartDifferentEnd_isFalse() {
        val first = TimeRange(
            from = LocalDateTime(2026, 8, 18, 8, 0).toInstant(zone),
            to = LocalDateTime(2026, 8, 18, 8, 45).toInstant(zone),
        )
        val second = TimeRange(
            from = LocalDateTime(2026, 8, 18, 8, 0).toInstant(zone),
            to = LocalDateTime(2026, 8, 18, 9, 30).toInstant(zone),
        )

        assertFalse(first.timeEquals(second))
    }

    @Test
    fun timeEquals_sameStartAndEnd_isTrue() {
        val first = TimeRange(
            from = LocalDateTime(2026, 8, 18, 8, 0).toInstant(zone),
            to = LocalDateTime(2026, 8, 18, 8, 45).toInstant(zone),
        )
        val second = TimeRange(
            from = LocalDateTime(2026, 8, 18, 8, 0).toInstant(zone),
            to = LocalDateTime(2026, 8, 18, 8, 45).toInstant(zone),
        )

        assertTrue(first.timeEquals(second))
    }
}
