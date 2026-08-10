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

package ru.aleshin.studyassistant.analytics.impl.domain.calculators

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toInstant
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsGoalDistribution
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsInsight
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 10.08.2026.
 */
class AnalyticsReportCalculatorTest {

    private val rangeCalculator = AnalyticsRangeCalculator.Base(TimeZone.UTC)
    private val calculator = AnalyticsReportCalculator.Base(TimeZone.UTC)
    private val organization = OrganizationShort(uid = "org", shortName = "School", updatedAt = 0L)

    @Test
    fun tasksAreClassifiedWithoutCountingUndatedTodoInPeriodRate() {
        val currentTime = instant(2026, 8, 2, 12)
        val onTime = homework("on-time", instant(2026, 8, 1, 18), true, instant(2026, 8, 1, 17))
        val late = homework("late", instant(2026, 8, 1, 18), true, instant(2026, 8, 1, 20))
        val overdue = homework("overdue", instant(2026, 8, 1, 18), false, null)
        val upcoming = homework("upcoming", instant(2026, 8, 3, 18), false, null)
        val missingDate = homework("missing", instant(2026, 8, 1, 18), true, null)
        val undatedTodo = Todo(
            uid = "undated",
            deadline = null,
            name = "Backlog",
            description = null,
            priority = TaskPriority.STANDARD,
            isDone = false,
            createdAt = instant(2026, 8, 1, 10),
            updatedAt = 0L,
        )
        val selection = rangeCalculator.selectCustom(
            from = LocalDate(2026, 8, 1).atStartOfDayIn(TimeZone.UTC),
            to = LocalDate(2026, 8, 4).atStartOfDayIn(TimeZone.UTC),
            currentTime = currentTime,
        )

        val report = calculator.calculate(
            selection = selection,
            currentTime = currentTime,
            currentClasses = emptyMap(),
            previousClasses = emptyMap(),
            currentHomeworks = listOf(onTime, late, overdue, upcoming, missingDate),
            previousHomeworks = emptyList(),
            completedHomeworks = listOf(onTime, late),
            currentTodos = listOf(undatedTodo),
            previousTodos = emptyList(),
            completedTodos = emptyList(),
            goalDistribution = emptyGoals(),
            workloadThreshold = 7,
            target = null,
        )

        assertEquals(3, report.summary.completedCount)
        assertEquals(1, report.summary.completedOnTime)
        assertEquals(1, report.summary.completedLate)
        assertEquals(1, report.summary.overdue)
        assertEquals(1, report.summary.upcoming)
        assertEquals(1, report.summary.missingCompleteDate)
        assertEquals(1, report.summary.undatedTodoBacklog)
        assertEquals(1f / 3f, report.summary.onTimeRate)
        assertEquals(
            50f,
            report.insights.first { it.type == AnalyticsInsight.Type.LATE_COMPLETION_SHARE }.value,
        )
    }

    private fun homework(
        uid: String,
        deadline: Instant,
        isDone: Boolean,
        completeDate: Instant?,
    ) = Homework(
        uid = uid,
        deadline = deadline,
        organization = organization,
        isDone = isDone,
        completeDate = completeDate,
        updatedAt = 0L,
    )

    private fun instant(year: Int, month: Int, day: Int, hour: Int): Instant {
        return LocalDateTime(year, month, day, hour, 0).toInstant(TimeZone.UTC)
    }

    private fun emptyGoals() = AnalyticsGoalDistribution(
        planned = 0,
        completed = 0,
        overdue = 0,
        homeworkGoals = 0,
        todoGoals = 0,
        desiredDuration = 0L,
        actualDuration = 0L,
        completionRate = null,
        hasActiveTimer = false,
    )
}
