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

package ru.aleshin.studyassistant.core.domain.entities.tasks

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 18.08.2026.
 */
class HomeworkStatusTest {

    private val zone = TimeZone.currentSystemDefault()

    @Test
    fun calculate_todayDeadlineAfterMidnight_isWait() {
        val deadline = LocalDateTime(2026, 8, 18, 0, 0).toInstant(zone)
        val now = LocalDateTime(2026, 8, 18, 10, 15).toInstant(zone)

        val status = HomeworkStatus.calculate(
            isDone = false,
            completeDate = null,
            deadline = deadline,
            currentTime = now,
        )

        assertEquals(HomeworkStatus.WAIT, status)
    }

    @Test
    fun calculate_yesterdayDeadline_isNotComplete() {
        val deadline = LocalDateTime(2026, 8, 17, 0, 0).toInstant(zone)
        val now = LocalDateTime(2026, 8, 18, 10, 15).toInstant(zone)

        val status = HomeworkStatus.calculate(
            isDone = false,
            completeDate = null,
            deadline = deadline,
            currentTime = now,
        )

        assertEquals(HomeworkStatus.NOT_COMPLETE, status)
    }

    @Test
    fun calculate_skippedHomework_isSkipped() {
        val deadline = LocalDateTime(2026, 8, 18, 0, 0).toInstant(zone)
        val now = LocalDateTime(2026, 8, 18, 10, 15).toInstant(zone)

        val status = HomeworkStatus.calculate(
            isDone = false,
            completeDate = now,
            deadline = deadline,
            currentTime = now,
        )

        assertEquals(HomeworkStatus.SKIPPED, status)
    }

    @Test
    fun calculate_farFutureDeadline_isInFuture() {
        val deadline = LocalDateTime(2026, 9, 1, 0, 0).toInstant(zone)
        val now = LocalDateTime(2026, 8, 18, 10, 15).toInstant(zone)

        val status = HomeworkStatus.calculate(
            isDone = false,
            completeDate = null,
            deadline = deadline,
            currentTime = now,
        )

        assertEquals(HomeworkStatus.IN_FUTURE, status)
    }
}
