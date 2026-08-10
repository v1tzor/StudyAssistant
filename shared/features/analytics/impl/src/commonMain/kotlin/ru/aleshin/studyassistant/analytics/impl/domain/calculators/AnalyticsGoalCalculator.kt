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
import kotlinx.datetime.TimeZone
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsGoalDistribution
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal interface AnalyticsGoalCalculator {

    fun calculate(
        goals: List<Goal>,
        currentTime: Instant,
        target: AnalyticsTarget?,
    ): AnalyticsGoalDistribution

    class Base(
        private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ) : AnalyticsGoalCalculator {

        override fun calculate(
            goals: List<Goal>,
            currentTime: Instant,
            target: AnalyticsTarget?,
        ): AnalyticsGoalDistribution {
            val filteredGoals = goals.filter { goal ->
                when (target) {
                    is AnalyticsTarget.Organization -> goal.contentHomework?.organization?.uid == target.uid
                    is AnalyticsTarget.Subject -> goal.contentHomework?.subject?.uid == target.uid
                    is AnalyticsTarget.Employee -> false
                    null -> true
                }
            }
            val actualDuration = filteredGoals.sumOf { goal -> goal.time.elapsedDuration(currentTime) }
            val desiredDuration = filteredGoals.sumOf { it.desiredTime ?: 0L }
            return AnalyticsGoalDistribution(
                planned = filteredGoals.size,
                completed = filteredGoals.count { it.isDone },
                overdue = filteredGoals.count {
                    !it.isDone && it.targetDate.startThisDay(timeZone) < currentTime.startThisDay(timeZone)
                },
                homeworkGoals = filteredGoals.count { it.contentType == GoalType.HOMEWORK },
                todoGoals = filteredGoals.count { it.contentType == GoalType.TODO },
                desiredDuration = desiredDuration,
                actualDuration = actualDuration,
                completionRate = if (desiredDuration == 0L) {
                    null
                } else {
                    actualDuration.toFloat() / desiredDuration
                },
                hasActiveTimer = filteredGoals.any { goal -> goal.time.isActive() },
            )
        }

        private fun GoalTime.elapsedDuration(currentTime: Instant): Long = when (this) {
            is GoalTime.Timer -> pastStopTime + activeDuration(currentTime, startTimePoint, isActive)
            is GoalTime.Stopwatch -> pastStopTime + activeDuration(currentTime, startTimePoint, isActive)
            GoalTime.None -> 0L
        }

        private fun GoalTime.isActive(): Boolean = when (this) {
            is GoalTime.Timer -> isActive
            is GoalTime.Stopwatch -> isActive
            GoalTime.None -> false
        }

        private fun activeDuration(currentTime: Instant, start: Instant, isActive: Boolean): Long {
            return if (isActive) {
                (currentTime.toEpochMilliseconds() - start.toEpochMilliseconds()).coerceAtLeast(0L)
            } else {
                0L
            }
        }
    }
}
