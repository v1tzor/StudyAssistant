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

import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.daysUntil
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsComparison
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsDailyData
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsGoalDistribution
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsGranularity
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsInsight
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsLoadBucket
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsLoadDistribution
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsOverview
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsRangeSelection
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsRegularity
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsSummary
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTargetDetails
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTaskBucket
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTaskDistribution
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTaskStatus
import ru.aleshin.studyassistant.analytics.impl.domain.entities.EmployeeAnalytics
import ru.aleshin.studyassistant.analytics.impl.domain.entities.OrganizationAnalytics
import ru.aleshin.studyassistant.analytics.impl.domain.entities.SubjectAnalytics
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.analytics.DailyWorkload
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.domain.entities.tasks.Todo
import ru.aleshin.studyassistant.core.domain.entities.tasks.fetchAllTasks
import ru.aleshin.studyassistant.core.domain.entities.tasks.toHomeworkComponents

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal interface AnalyticsReportCalculator {

    fun calculate(
        selection: AnalyticsRangeSelection,
        currentTime: Instant,
        currentClasses: Map<Instant, List<Class>>,
        previousClasses: Map<Instant, List<Class>>,
        currentHomeworks: List<Homework>,
        previousHomeworks: List<Homework>,
        completedHomeworks: List<Homework>,
        currentTodos: List<Todo>,
        previousTodos: List<Todo>,
        completedTodos: List<Todo>,
        goalDistribution: AnalyticsGoalDistribution,
        workloadThreshold: Int,
        target: AnalyticsTarget?,
    ): AnalyticsOverview

    class Base(
        private val timeZone: TimeZone = TimeZone.currentSystemDefault(),
    ) : AnalyticsReportCalculator {

        override fun calculate(
            selection: AnalyticsRangeSelection,
            currentTime: Instant,
            currentClasses: Map<Instant, List<Class>>,
            previousClasses: Map<Instant, List<Class>>,
            currentHomeworks: List<Homework>,
            previousHomeworks: List<Homework>,
            completedHomeworks: List<Homework>,
            currentTodos: List<Todo>,
            previousTodos: List<Todo>,
            completedTodos: List<Todo>,
            goalDistribution: AnalyticsGoalDistribution,
            workloadThreshold: Int,
            target: AnalyticsTarget?,
        ): AnalyticsOverview {
            val filteredClasses = currentClasses.filterForTarget(target)
            val filteredPreviousClasses = previousClasses.filterForTarget(target)
            val filteredHomeworks = currentHomeworks.filterHomeworksForTarget(target)
            val filteredPreviousHomeworks = previousHomeworks.filterHomeworksForTarget(target)
            val filteredCompletedHomeworks = completedHomeworks.filterHomeworksForTarget(target)
            val filteredTodos = currentTodos.filterTodosForTarget(target)
            val filteredPreviousTodos = previousTodos.filterTodosForTarget(target)
            val filteredCompletedTodos = completedTodos.filterTodosForTarget(target)
            val summary = calculateSummary(
                classes = filteredClasses.values.flatten(),
                homeworks = filteredHomeworks,
                todos = filteredTodos,
                currentTime = currentTime,
            )
            val previousSummary = calculateSummary(
                classes = filteredPreviousClasses.values.flatten(),
                homeworks = filteredPreviousHomeworks,
                todos = filteredPreviousTodos,
                currentTime = selection.comparisonCutoff,
            )
            val loadDistribution = calculateLoadDistribution(
                selection = selection,
                classes = filteredClasses,
                homeworks = filteredHomeworks,
                todos = filteredTodos,
                threshold = workloadThreshold,
            )
            val taskDistribution = calculateTaskDistribution(
                selection = selection,
                summary = summary,
                homeworks = filteredHomeworks,
                completedHomeworks = filteredCompletedHomeworks,
                todos = filteredTodos,
                completedTodos = filteredCompletedTodos,
            )
            val organizations = calculateOrganizations(
                classes = filteredClasses,
                homeworks = filteredHomeworks,
                currentTime = currentTime,
            )
            val subjects = calculateSubjects(
                classes = filteredClasses,
                homeworks = filteredHomeworks,
                currentTime = currentTime,
            )
            val employees = calculateEmployees(filteredClasses)
            val regularity = calculateRegularity(
                classes = filteredClasses,
                completedHomeworks = filteredCompletedHomeworks,
                completedTodos = filteredCompletedTodos,
                selection = selection,
                homeworks = filteredHomeworks,
                todos = filteredTodos,
            )
            val targetDetails = target?.let {
                calculateTargetDetails(
                    target = it,
                    classes = currentClasses,
                    homeworks = currentHomeworks,
                )
            }
            return AnalyticsOverview(
                selection = selection,
                summary = summary,
                comparison = calculateComparison(
                    selection = selection,
                    currentTime = currentTime,
                    currentSummary = summary,
                    previousSummary = previousSummary,
                    currentHomeworks = filteredHomeworks,
                    previousHomeworks = filteredPreviousHomeworks,
                    currentTodos = filteredTodos,
                    previousTodos = filteredPreviousTodos,
                ),
                loadDistribution = loadDistribution,
                taskDistribution = taskDistribution,
                goalDistribution = goalDistribution,
                organizations = organizations,
                subjects = subjects,
                employees = employees,
                regularity = regularity,
                insights = calculateInsights(loadDistribution, summary, organizations, subjects),
                targetDetails = targetDetails,
                hasData = filteredClasses.values.any { it.isNotEmpty() } ||
                    filteredHomeworks.isNotEmpty() ||
                    filteredTodos.isNotEmpty() ||
                    goalDistribution.planned > 0,
            )
        }

        private fun calculateSummary(
            classes: List<Class>,
            homeworks: List<Homework>,
            todos: List<Todo>,
            currentTime: Instant,
        ): AnalyticsSummary {
            val statuses = buildList {
                homeworks.forEach { homework ->
                    add(
                        classify(homework.isDone, homework.completeDate, homework.deadline, currentTime)
                    )
                }
                todos.forEach { todo ->
                    todo.deadline?.let { deadline ->
                        add(classify(todo.isDone, todo.completeDate, deadline, currentTime))
                    }
                }
            }
            val completedOnTime = statuses.count { it == AnalyticsTaskStatus.COMPLETED_ON_TIME }
            val completedLate = statuses.count { it == AnalyticsTaskStatus.COMPLETED_LATE }
            val overdue = statuses.count { it == AnalyticsTaskStatus.OVERDUE }
            val missingCompleteDate = statuses.count { it == AnalyticsTaskStatus.MISSING_COMPLETE_DATE }
            val rateDenominator = completedOnTime + completedLate + overdue
            return AnalyticsSummary(
                plannedDuration = classes.sumOf { it.duration() },
                classesCount = classes.size,
                homeworkCount = homeworks.size,
                todoCount = todos.count { it.deadline != null },
                completedCount = completedOnTime + completedLate + missingCompleteDate,
                completedOnTime = completedOnTime,
                completedLate = completedLate,
                overdue = overdue,
                upcoming = statuses.count { it == AnalyticsTaskStatus.UPCOMING },
                missingCompleteDate = missingCompleteDate,
                undatedTodoBacklog = todos.count { it.deadline == null && !it.isDone },
                onTimeRate = completedOnTime.toRate(rateDenominator),
            )
        }

        private fun calculateComparison(
            selection: AnalyticsRangeSelection,
            currentTime: Instant,
            currentSummary: AnalyticsSummary,
            previousSummary: AnalyticsSummary,
            currentHomeworks: List<Homework>,
            previousHomeworks: List<Homework>,
            currentTodos: List<Todo>,
            previousTodos: List<Todo>,
        ): AnalyticsComparison {
            val currentCutoff = minOf(currentTime, selection.range.to)
            val currentOutcome = calculateSummary(
                classes = emptyList(),
                homeworks = currentHomeworks.filter { it.deadline <= currentCutoff },
                todos = currentTodos.filter { todo ->
                    todo.deadline?.let { deadline -> deadline <= currentCutoff } ?: true
                },
                currentTime = currentCutoff,
            )
            val previousOutcome = calculateSummary(
                classes = emptyList(),
                homeworks = previousHomeworks.filter { it.deadline <= selection.comparisonCutoff },
                todos = previousTodos.filter { todo ->
                    todo.deadline?.let { deadline -> deadline <= selection.comparisonCutoff } ?: true
                },
                currentTime = selection.comparisonCutoff,
            )
            return AnalyticsComparison(
                plannedDurationPercent = percentChange(
                    currentSummary.plannedDuration.toFloat(),
                    previousSummary.plannedDuration.toFloat(),
                ),
                commitmentsPercent = percentChange(
                    (currentSummary.homeworkCount + currentSummary.todoCount).toFloat(),
                    (previousSummary.homeworkCount + previousSummary.todoCount).toFloat(),
                ),
                onTimeRatePoints = if (
                    currentOutcome.onTimeRate != null && previousOutcome.onTimeRate != null
                ) {
                    (currentOutcome.onTimeRate - previousOutcome.onTimeRate) * PERCENT_FACTOR
                } else {
                    null
                },
                overduePercent = percentChange(
                    currentOutcome.overdue.toFloat(),
                    previousOutcome.overdue.toFloat(),
                ),
            )
        }

        private fun calculateLoadDistribution(
            selection: AnalyticsRangeSelection,
            classes: Map<Instant, List<Class>>,
            homeworks: List<Homework>,
            todos: List<Todo>,
            threshold: Int,
        ): AnalyticsLoadDistribution {
            val dailyData = selection.range.localDates().map { date ->
                val instant = date.atStartOfDayIn(timeZone)
                val dailyClasses = classes[instant].orEmpty()
                val dailyHomeworks = homeworks.filter { it.deadline.startThisDay(timeZone) == instant }
                val dailyTodos = todos.filter { it.deadline?.startThisDay(timeZone) == instant }
                AnalyticsDailyData(
                    date = date,
                    classes = dailyClasses,
                    homeworks = dailyHomeworks,
                    todos = dailyTodos,
                    workload = DailyWorkload.calculate(dailyClasses, dailyHomeworks, dailyTodos).value,
                )
            }
            val buckets = dailyData.groupBy { data -> data.date.bucketStart(selection.granularity) }
                .entries
                .sortedBy { it.key }
                .map { (_, days) ->
                    AnalyticsLoadBucket(
                        from = days.first().date.atStartOfDayIn(timeZone),
                        to = days.last().date.endOfDay(),
                        workload = days.map { it.workload }.averageOrZero(),
                        plannedDuration = days.sumOf { day -> day.classes.sumOf { it.duration() } },
                        classesCount = days.sumOf { it.classes.size },
                        homeworkCount = days.sumOf { it.homeworks.size },
                        todoCount = days.sumOf { it.todos.size },
                    )
                }
            return AnalyticsLoadDistribution(
                buckets = buckets,
                averageWorkload = dailyData.map { it.workload }.averageOrZero(),
                peakBucket = buckets.maxByOrNull { it.workload },
                daysAboveThreshold = dailyData.count { it.workload >= threshold },
                threshold = threshold,
            )
        }

        private fun calculateTaskDistribution(
            selection: AnalyticsRangeSelection,
            summary: AnalyticsSummary,
            homeworks: List<Homework>,
            completedHomeworks: List<Homework>,
            todos: List<Todo>,
            completedTodos: List<Todo>,
        ): AnalyticsTaskDistribution {
            val completedHomeworksByBucket = completedHomeworks
                .mapNotNull { homework -> homework.completeDate?.let { it to homework } }
                .groupBy { (date) -> date.toLocalDateTime(timeZone).date.bucketStart(selection.granularity) }
            val completedTodosByBucket = completedTodos
                .mapNotNull { todo -> todo.completeDate?.let { it to todo } }
                .groupBy { (date) -> date.toLocalDateTime(timeZone).date.bucketStart(selection.granularity) }
            val buckets = selection.range.localDates()
                .groupBy { it.bucketStart(selection.granularity) }
                .entries
                .sortedBy { it.key }
                .map { (bucketStart, dates) ->
                    AnalyticsTaskBucket(
                        from = bucketStart.atStartOfDayIn(timeZone),
                        to = dates.last().endOfDay(),
                        completedHomeworks = completedHomeworksByBucket[bucketStart].orEmpty().size,
                        completedTodos = completedTodosByBucket[bucketStart].orEmpty().size,
                    )
                }
            return AnalyticsTaskDistribution(
                summary = summary,
                buckets = buckets,
                testsCount = homeworks.count { !it.test.isNullOrBlank() },
                theoreticalPartsCount = homeworks.sumOf { it.theoreticalTasks.partsCount() },
                practicalPartsCount = homeworks.sumOf { it.practicalTasks.partsCount() },
                presentationPartsCount = homeworks.sumOf { it.presentationTasks.partsCount() },
                standardTodos = todos.count { it.priority == TaskPriority.STANDARD },
                mediumPriorityTodos = todos.count { it.priority == TaskPriority.MEDIUM },
                highPriorityTodos = todos.count { it.priority == TaskPriority.HIGH },
            )
        }

        private fun calculateOrganizations(
            classes: Map<Instant, List<Class>>,
            homeworks: List<Homework>,
            currentTime: Instant,
        ): List<OrganizationAnalytics> {
            val allClasses = classes.values.flatten()
            val totalDuration = allClasses.sumOf { it.duration() }
            val organizations = (allClasses.map { it.organization } + homeworks.map { it.organization })
                .distinctBy { it.uid }
            return organizations.map { organization ->
                val targetClasses = allClasses.filter { it.organization.uid == organization.uid }
                val targetHomeworks = homeworks.filter { it.organization.uid == organization.uid }
                val statuses = targetHomeworks.map {
                    classify(it.isDone, it.completeDate, it.deadline, currentTime)
                }
                val onTime = statuses.count { it == AnalyticsTaskStatus.COMPLETED_ON_TIME }
                val rateDenominator = onTime +
                    statuses.count {
                        it == AnalyticsTaskStatus.COMPLETED_LATE || it == AnalyticsTaskStatus.OVERDUE
                    }
                val duration = targetClasses.sumOf { it.duration() }
                OrganizationAnalytics(
                    organization = organization,
                    plannedDuration = duration,
                    classesCount = targetClasses.size,
                    homeworkCount = targetHomeworks.size,
                    onTimeRate = onTime.toRate(rateDenominator),
                    workloadShare = duration.toRate(totalDuration) ?: 0f,
                )
            }.sortedWith(
                compareByDescending<OrganizationAnalytics> { it.plannedDuration }
                    .thenBy { it.organization.shortName },
            )
        }

        private fun calculateSubjects(
            classes: Map<Instant, List<Class>>,
            homeworks: List<Homework>,
            currentTime: Instant,
        ): List<SubjectAnalytics> {
            val allClasses = classes.values.flatten()
            val keys = (allClasses.map { it.subject?.uid } + homeworks.map { it.subject?.uid }).distinct()
            val maxPlannedDuration = allClasses
                .groupBy { it.subject?.uid }
                .maxOfOrNull { it.value.sumOf { it.duration() } }
                ?.toFloat()
                ?.coerceAtLeast(1F) ?: 1F

            return keys.map { subjectId ->
                val targetClasses = allClasses.filter { it.subject?.uid == subjectId }
                val targetHomeworks = homeworks.filter { it.subject?.uid == subjectId }
                val statuses = targetHomeworks.map {
                    classify(it.isDone, it.completeDate, it.deadline, currentTime)
                }
                SubjectAnalytics(
                    subject = targetClasses.firstNotNullOfOrNull { it.subject } ?: targetHomeworks.firstNotNullOfOrNull { it.subject },
                    plannedDuration = targetClasses.sumOf { it.duration() },
                    workloadProgress = (targetClasses.sumOf { it.duration() }.toFloat() / maxPlannedDuration).coerceIn(0f, 1f),
                    classesCount = targetClasses.size,
                    homeworkCount = targetHomeworks.size,
                    testsCount = targetHomeworks.count { !it.test.isNullOrBlank() },
                    completedOnTime = statuses.count { it == AnalyticsTaskStatus.COMPLETED_ON_TIME },
                    overdue = statuses.count { it == AnalyticsTaskStatus.OVERDUE },
                )
            }.sortedWith(
                compareByDescending<SubjectAnalytics> { it.plannedDuration }.thenBy { it.subject?.name.orEmpty() },
            )
        }

        private fun calculateEmployees(
            classes: Map<Instant, List<Class>>,
        ): List<EmployeeAnalytics> {
            val classEntries = classes.flatMap { (date, values) -> values.map { date to it } }
            return classEntries.groupBy { (_, classModel) -> classModel.teacher?.uid }.map { (_, entries) ->
                val employee = entries.firstNotNullOfOrNull { (_, classModel) -> classModel.teacher }
                EmployeeAnalytics(
                    employee = employee,
                    plannedDuration = entries.sumOf { (_, classModel) -> classModel.duration() },
                    classesCount = entries.size,
                    subjectsCount = entries.mapNotNull { (_, classModel) -> classModel.subject?.uid }.toSet().size,
                    organizationsCount = entries.map { (_, classModel) -> classModel.organization.uid }.toSet().size,
                    mostFrequentDay = entries.groupingBy { (date) ->
                        date.toLocalDateTime(timeZone).dayOfWeek
                    }.eachCount().maxByOrNull { it.value }?.key,
                )
            }.sortedWith(
                compareByDescending<EmployeeAnalytics> { it.plannedDuration }
                    .thenBy { it.employee?.firstName.orEmpty() },
            )
        }

        private fun calculateRegularity(
            classes: Map<Instant, List<Class>>,
            completedHomeworks: List<Homework>,
            completedTodos: List<Todo>,
            selection: AnalyticsRangeSelection,
            homeworks: List<Homework>,
            todos: List<Todo>,
        ): AnalyticsRegularity {
            val dailyWorkloads = selection.range.localDates().associateWith { date ->
                val instant = date.atStartOfDayIn(timeZone)
                DailyWorkload.calculate(
                    classes = classes[instant].orEmpty(),
                    homeworks = homeworks.filter { it.deadline.startThisDay(timeZone) == instant },
                    todos = todos.filter { it.deadline?.startThisDay(timeZone) == instant },
                ).value
            }
            val completionDates = (
                completedHomeworks.mapNotNull { it.completeDate } +
                    completedTodos.mapNotNull { it.completeDate }
                )
                .map { it.toLocalDateTime(timeZone).date }
                .filter { it.atStartOfDayIn(timeZone) in selection.range.from..selection.range.to }
                .toSet()
            return AnalyticsRegularity(
                averageWorkloadByWeekday = DayOfWeek.entries.associateWith { dayOfWeek ->
                    dailyWorkloads.filterKeys { it.dayOfWeek == dayOfWeek }.values.averageOrZero()
                },
                studyDays = classes.count { (_, values) -> values.isNotEmpty() },
                completionDays = completionDates.size,
                longestCompletionStreak = completionDates.longestStreak(),
            )
        }

        private fun calculateInsights(
            loadDistribution: AnalyticsLoadDistribution,
            summary: AnalyticsSummary,
            organizations: List<OrganizationAnalytics>,
            subjects: List<SubjectAnalytics>,
        ): List<AnalyticsInsight> = buildList {
            loadDistribution.peakBucket?.takeIf { it.workload > 0f }?.let { peak ->
                add(AnalyticsInsight(AnalyticsInsight.Type.PEAK_LOAD, peak.workload, peak.from))
            }
            if (loadDistribution.daysAboveThreshold > 0) {
                add(
                    AnalyticsInsight(
                        type = AnalyticsInsight.Type.OVERLOAD_DAYS,
                        value = loadDistribution.daysAboveThreshold.toFloat(),
                    )
                )
            }
            val completedWithDate = summary.completedOnTime + summary.completedLate
            if (summary.completedLate > 0 && completedWithDate > 0) {
                add(
                    AnalyticsInsight(
                        type = AnalyticsInsight.Type.LATE_COMPLETION_SHARE,
                        value = (summary.completedLate.toRate(completedWithDate) ?: 0f) * PERCENT_FACTOR,
                    )
                )
            }
            organizations.firstOrNull()?.takeIf { it.workloadShare >= CONCENTRATION_THRESHOLD }?.let {
                add(
                    AnalyticsInsight(
                        type = AnalyticsInsight.Type.ORGANIZATION_CONCENTRATION,
                        value = it.workloadShare * PERCENT_FACTOR,
                        name = it.organization.shortName,
                    )
                )
            }
            val subjectsDuration = subjects.sumOf { it.plannedDuration }
            subjects.firstOrNull()?.takeIf { subjectsDuration > 0L }?.let { subject ->
                val share = subject.plannedDuration.toRate(subjectsDuration) ?: 0f
                if (share >= CONCENTRATION_THRESHOLD) {
                    add(
                        AnalyticsInsight(
                            type = AnalyticsInsight.Type.SUBJECT_CONCENTRATION,
                            value = share * PERCENT_FACTOR,
                            name = subject.subject?.name,
                        )
                    )
                }
            }
        }.take(MAX_INSIGHTS)

        private fun calculateTargetDetails(
            target: AnalyticsTarget,
            classes: Map<Instant, List<Class>>,
            homeworks: List<Homework>,
        ): AnalyticsTargetDetails {
            val allClasses = classes.values.flatten()
            return when (target) {
                is AnalyticsTarget.Organization -> AnalyticsTargetDetails(
                    target = target,
                    organization = allClasses.firstOrNull {
                        it.organization.uid == target.uid
                    }?.organization ?: homeworks.firstOrNull {
                        it.organization.uid == target.uid
                    }?.organization,
                )
                is AnalyticsTarget.Subject -> AnalyticsTargetDetails(
                    target = target,
                    subject = allClasses.firstNotNullOfOrNull {
                        it.subject?.takeIf { subject -> subject.uid == target.uid }
                    } ?: homeworks.firstNotNullOfOrNull {
                        it.subject?.takeIf { subject -> subject.uid == target.uid }
                    },
                )
                is AnalyticsTarget.Employee -> AnalyticsTargetDetails(
                    target = target,
                    employee = allClasses.firstNotNullOfOrNull {
                        it.teacher?.takeIf { employee -> employee.uid == target.uid }
                    },
                )
            }
        }

        private fun Map<Instant, List<Class>>.filterForTarget(
            target: AnalyticsTarget?,
        ): Map<Instant, List<Class>> = mapValues { (_, classes) ->
            classes.filter { classModel ->
                when (target) {
                    is AnalyticsTarget.Organization -> classModel.organization.uid == target.uid
                    is AnalyticsTarget.Subject -> classModel.subject?.uid == target.uid
                    is AnalyticsTarget.Employee -> classModel.teacher?.uid == target.uid
                    null -> true
                }
            }
        }

        private fun List<Homework>.filterHomeworksForTarget(
            target: AnalyticsTarget?,
        ): List<Homework> {
            return filter { homework ->
                when (target) {
                    is AnalyticsTarget.Organization -> homework.organization.uid == target.uid
                    is AnalyticsTarget.Subject -> homework.subject?.uid == target.uid
                    is AnalyticsTarget.Employee -> false
                    null -> true
                }
            }
        }

        private fun List<Todo>.filterTodosForTarget(target: AnalyticsTarget?): List<Todo> {
            return if (target == null) this else emptyList()
        }

        private fun classify(
            isDone: Boolean,
            completeDate: Instant?,
            deadline: Instant,
            currentTime: Instant,
        ): AnalyticsTaskStatus = when {
            isDone && completeDate == null -> AnalyticsTaskStatus.MISSING_COMPLETE_DATE
            isDone && completeDate != null && completeDate <= deadline -> AnalyticsTaskStatus.COMPLETED_ON_TIME
            isDone -> AnalyticsTaskStatus.COMPLETED_LATE
            deadline < currentTime -> AnalyticsTaskStatus.OVERDUE
            else -> AnalyticsTaskStatus.UPCOMING
        }

        private fun TimeRange.localDates(): List<LocalDate> {
            val start = from.toLocalDateTime(timeZone).date
            val end = to.toLocalDateTime(timeZone).date
            return buildList {
                var date = start
                while (date <= end) {
                    add(date)
                    date = date.plus(1, DateTimeUnit.DAY)
                }
            }
        }

        private fun LocalDate.bucketStart(granularity: AnalyticsGranularity): LocalDate {
            return when (granularity) {
                AnalyticsGranularity.DAY -> this
                AnalyticsGranularity.WEEK -> plus(-dayOfWeek.ordinal, DateTimeUnit.DAY)
                AnalyticsGranularity.MONTH -> LocalDate(year, month, 1)
            }
        }

        private fun LocalDate.endOfDay(): Instant {
            return Instant.fromEpochMilliseconds(
                plus(1, DateTimeUnit.DAY).atStartOfDayIn(timeZone).toEpochMilliseconds() - 1L,
            )
        }

        private fun Class.duration(): Long {
            return (timeRange.to.toEpochMilliseconds() - timeRange.from.toEpochMilliseconds())
                .coerceAtLeast(0L)
        }

        private fun String.partsCount(): Int = toHomeworkComponents().fetchAllTasks().size

        private fun Collection<Float>.averageOrZero(): Float {
            return if (isEmpty()) 0f else average().toFloat()
        }

        private fun Int.toRate(denominator: Int): Float? {
            return if (denominator == 0) null else toFloat() / denominator
        }

        private fun Long.toRate(denominator: Long): Float? {
            return if (denominator == 0L) null else toFloat() / denominator
        }

        private fun percentChange(current: Float, previous: Float): Float? {
            return if (previous == 0f) null else (current - previous) / previous * PERCENT_FACTOR
        }

        private fun Set<LocalDate>.longestStreak(): Int {
            if (isEmpty()) return 0
            val sortedDates = sorted()
            var longest = 1
            var current = 1
            for (index in 1..sortedDates.lastIndex) {
                if (sortedDates[index - 1].daysUntil(sortedDates[index]) == 1) {
                    current++
                    longest = maxOf(longest, current)
                } else {
                    current = 1
                }
            }
            return longest
        }
    }
}

private const val PERCENT_FACTOR = 100f
private const val CONCENTRATION_THRESHOLD = 0.6f
private const val MAX_INSIGHTS = 3
