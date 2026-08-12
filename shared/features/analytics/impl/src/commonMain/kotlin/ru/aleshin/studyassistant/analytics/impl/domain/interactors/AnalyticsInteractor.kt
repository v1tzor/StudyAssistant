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

package ru.aleshin.studyassistant.analytics.impl.domain.interactors

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.analytics.impl.domain.calculators.AnalyticsGoalCalculator
import ru.aleshin.studyassistant.analytics.impl.domain.calculators.AnalyticsRangeCalculator
import ru.aleshin.studyassistant.analytics.impl.domain.calculators.AnalyticsReportCalculator
import ru.aleshin.studyassistant.analytics.impl.domain.calculators.AnalyticsScheduleCalculator
import ru.aleshin.studyassistant.analytics.impl.domain.common.AnalyticsEitherWrapper
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsFailures
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsGoalDistribution
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsOverview
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsPeriod
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsRangeSelection
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.settings.NotificationSettings
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.NotificationSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal interface AnalyticsInteractor {

    suspend fun fetchDefault(target: AnalyticsTarget?): FlowDomainResult<AnalyticsFailures, AnalyticsOverview>
    suspend fun fetchPeriod(
        period: AnalyticsPeriod,
        anchor: Instant,
        target: AnalyticsTarget?,
    ): FlowDomainResult<AnalyticsFailures, AnalyticsOverview>

    suspend fun fetchChangedPeriod(
        period: AnalyticsPeriod,
        selection: AnalyticsRangeSelection,
        target: AnalyticsTarget?,
    ): FlowDomainResult<AnalyticsFailures, AnalyticsOverview>

    suspend fun fetchCustom(
        from: Instant,
        to: Instant,
        target: AnalyticsTarget?,
    ): FlowDomainResult<AnalyticsFailures, AnalyticsOverview>

    suspend fun fetchShifted(
        selection: AnalyticsRangeSelection,
        amount: Int,
        target: AnalyticsTarget?,
    ): FlowDomainResult<AnalyticsFailures, AnalyticsOverview>

    suspend fun fetchSelection(
        selection: AnalyticsRangeSelection,
        target: AnalyticsTarget?,
    ): FlowDomainResult<AnalyticsFailures, AnalyticsOverview>

    class Base(
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val notificationSettingsRepository: NotificationSettingsRepository,
        private val homeworksRepository: HomeworksRepository,
        private val todoRepository: TodoRepository,
        private val goalsRepository: DailyGoalsRepository,
        private val rangeCalculator: AnalyticsRangeCalculator,
        private val scheduleCalculator: AnalyticsScheduleCalculator,
        private val reportCalculator: AnalyticsReportCalculator,
        private val goalCalculator: AnalyticsGoalCalculator,
        private val dateManager: DateManager,
        private val eitherWrapper: AnalyticsEitherWrapper,
    ) : AnalyticsInteractor {

        override suspend fun fetchDefault(target: AnalyticsTarget?) = fetchSelection(
            selection = rangeCalculator.createDefault(dateManager.fetchCurrentInstant()),
            target = target,
        )

        override suspend fun fetchPeriod(
            period: AnalyticsPeriod,
            anchor: Instant,
            target: AnalyticsTarget?,
        ) = fetchSelection(
            selection = rangeCalculator.selectPeriod(
                period = period,
                anchor = anchor,
                currentTime = dateManager.fetchCurrentInstant(),
            ),
            target = target,
        )

        override suspend fun fetchChangedPeriod(
            period: AnalyticsPeriod,
            selection: AnalyticsRangeSelection,
            target: AnalyticsTarget?,
        ) = fetchSelection(
            selection = rangeCalculator.changePeriod(
                period = period,
                selection = selection,
                currentTime = dateManager.fetchCurrentInstant(),
            ),
            target = target,
        )

        override suspend fun fetchCustom(
            from: Instant,
            to: Instant,
            target: AnalyticsTarget?,
        ) = fetchSelection(
            selection = rangeCalculator.selectCustom(
                from = from,
                to = to,
                currentTime = dateManager.fetchCurrentInstant(),
            ),
            target = target,
        )

        override suspend fun fetchShifted(
            selection: AnalyticsRangeSelection,
            amount: Int,
            target: AnalyticsTarget?,
        ) = fetchSelection(
            selection = rangeCalculator.shift(
                selection = selection,
                amount = amount,
                currentTime = dateManager.fetchCurrentInstant(),
            ),
            target = target,
        )

        override suspend fun fetchSelection(
            selection: AnalyticsRangeSelection,
            target: AnalyticsTarget?,
        ) = eitherWrapper.wrapFlow {
            val sourceRange = TimeRange(
                from = selection.previousRange.from,
                to = selection.range.to,
            )
            val schedulesFlow = combine(
                baseScheduleRepository.fetchSchedulesByVersion(sourceRange, null),
                customScheduleRepository.fetchSchedulesByTimeRange(sourceRange),
                calendarSettingsRepository.fetchSettings(),
            ) { baseSchedules, customSchedules, calendarSettings ->
                val currentSchedules = scheduleCalculator.calculate(
                    range = selection.range,
                    baseSchedules = baseSchedules,
                    customSchedules = customSchedules,
                    calendarSettings = calendarSettings,
                )
                val previousSchedules = scheduleCalculator.calculate(
                    range = selection.previousRange,
                    baseSchedules = baseSchedules,
                    customSchedules = customSchedules,
                    calendarSettings = calendarSettings,
                )
                currentSchedules to previousSchedules
            }
            val homeworksFlow = combine(
                homeworksRepository.fetchHomeworksByTimeRange(selection.range),
                homeworksRepository.fetchHomeworksByTimeRange(selection.previousRange),
                homeworksRepository.fetchCompletedHomeworksByTimeRange(selection.range),
            ) { current, previous, completed -> Triple(current, previous, completed) }
            val todosFlow = combine(
                todoRepository.fetchTodosByTimeRange(selection.range),
                todoRepository.fetchTodosByTimeRange(selection.previousRange),
                todoRepository.fetchCompletedTodos(selection.range),
            ) { current, previous, completed -> Triple(current, previous, completed) }
            val staticReportFlow = combine(
                schedulesFlow,
                homeworksFlow,
                todosFlow,
                notificationSettingsRepository.fetchSettings(),
                dateManager.secondTicker(),
            ) { schedules, homeworks, todos, notificationSettings, _ ->
                reportCalculator.calculate(
                    selection = selection,
                    currentTime = dateManager.fetchCurrentInstant(),
                    currentClasses = schedules.first,
                    previousClasses = schedules.second,
                    currentHomeworks = homeworks.first,
                    previousHomeworks = homeworks.second,
                    completedHomeworks = homeworks.third,
                    currentTodos = todos.first,
                    previousTodos = todos.second,
                    completedTodos = todos.third,
                    goalDistribution = EMPTY_GOAL_DISTRIBUTION,
                    workloadThreshold = notificationSettings.highWorkload ?: NotificationSettings.WORKLOAD_HIGH_VALUE,
                    target = target,
                )
            }.distinctUntilChanged()
            val goalsFlow = combine(
                goalsRepository.fetchDailyGoalsByTimeRange(selection.range),
                dateManager.secondTicker(),
            ) { goals, _ ->
                goalCalculator.calculate(
                    goals = goals,
                    currentTime = dateManager.fetchCurrentInstant(),
                    target = target,
                )
            }.distinctUntilChanged()
            combine(staticReportFlow, goalsFlow) { report, goalDistribution ->
                report.copy(
                    goalDistribution = goalDistribution,
                    hasData = report.hasData || goalDistribution.planned > 0,
                )
            }.distinctUntilChanged()
        }
    }
}

private val EMPTY_GOAL_DISTRIBUTION = AnalyticsGoalDistribution(
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
