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

package ru.aleshin.studyassistant.widget.domain.interactors

import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.setHoursAndMinutes
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.goals.Goal
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.fetchAllTasks
import ru.aleshin.studyassistant.core.domain.entities.tasks.toHomeworkComponents
import ru.aleshin.studyassistant.core.domain.interactors.TodoCompletionInteractor
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.widget.domain.common.WidgetEitherWrapper
import ru.aleshin.studyassistant.widget.domain.entities.WidgetDisplaySettings
import ru.aleshin.studyassistant.widget.domain.entities.WidgetFailure
import ru.aleshin.studyassistant.widget.domain.entities.goal.WidgetGoalItem
import ru.aleshin.studyassistant.widget.domain.entities.goal.WidgetGoalStatus
import ru.aleshin.studyassistant.widget.domain.entities.goal.WidgetGoals
import ru.aleshin.studyassistant.widget.domain.entities.homework.WidgetHomeworkGroup
import ru.aleshin.studyassistant.widget.domain.entities.homework.WidgetHomeworkItem
import ru.aleshin.studyassistant.widget.domain.entities.homework.WidgetHomeworks
import ru.aleshin.studyassistant.widget.domain.entities.schedule.WidgetSchedule
import ru.aleshin.studyassistant.widget.domain.entities.schedule.WidgetScheduleItem
import ru.aleshin.studyassistant.widget.domain.entities.schedule.WidgetScheduleStatus
import ru.aleshin.studyassistant.widget.domain.entities.todo.WidgetTodoItem
import ru.aleshin.studyassistant.widget.domain.entities.todo.WidgetTodoStatus
import ru.aleshin.studyassistant.widget.domain.entities.todo.WidgetTodos

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
interface WidgetInteractor {

    suspend fun fetchSchedule(): DomainResult<WidgetFailure, WidgetSchedule>
    suspend fun fetchHomeworks(): DomainResult<WidgetFailure, WidgetHomeworks>
    suspend fun fetchTodos(): DomainResult<WidgetFailure, WidgetTodos>
    suspend fun fetchGoals(): DomainResult<WidgetFailure, WidgetGoals>
    suspend fun fetchDisplaySettings(): DomainResult<WidgetFailure, WidgetDisplaySettings>
    suspend fun setTodoDone(todoId: UID, isDone: Boolean): UnitDomainResult<WidgetFailure>

    class Base(
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val homeworksRepository: HomeworksRepository,
        private val todoRepository: TodoRepository,
        private val goalsRepository: DailyGoalsRepository,
        private val generalSettingsRepository: GeneralSettingsRepository,
        private val todoCompletionInteractor: TodoCompletionInteractor,
        private val dateManager: DateManager,
        private val eitherWrapper: WidgetEitherWrapper,
    ) : WidgetInteractor {

        override suspend fun fetchSchedule() = eitherWrapper.wrap {
            val currentTime = dateManager.fetchCurrentInstant()
            val currentDate = currentTime.startThisDay()
            val calendarSettings = calendarSettingsRepository.fetchSettings().first()
            val numberOfWeek = currentDate.dateTime().date.numberOfRepeatWeek(
                calendarSettings.numberOfWeek
            )
            val baseSchedule = baseScheduleRepository.fetchScheduleByDate(
                currentDate,
                numberOfWeek,
            ).first()
            val customSchedule = customScheduleRepository.fetchScheduleByDate(currentDate).first()
            val classes = if (customSchedule != null) {
                customSchedule.classes
            } else {
                baseSchedule?.classes?.filter { classModel ->
                    calendarSettings.holidays.none { holidays ->
                        val dateMatches = TimeRange(holidays.start, holidays.end)
                            .containsDate(currentDate)
                        val organizationMatches = holidays.organizations
                            .contains(classModel.organization.uid)
                        dateMatches && organizationMatches
                    }
                }.orEmpty()
            }.sortedBy { it.timeRange.from.dateTime().time }
            val items = classes.map { classModel ->
                classModel.toWidgetItem(currentDate, currentTime)
            }

            WidgetSchedule(
                generatedAt = currentTime,
                date = currentDate,
                customScheduleId = customSchedule?.uid,
                baseScheduleId = baseSchedule?.uid,
                items = items,
                nextUpdateAt = fetchNextUpdateAt(
                    currentTime = currentTime,
                    candidates = items.flatMap { listOf(it.start, it.end) },
                ),
            )
        }

        override suspend fun fetchHomeworks() = eitherWrapper.wrap {
            val currentTime = dateManager.fetchCurrentInstant()
            val currentDate = currentTime.startThisDay()
            val targetTimeRange = TimeRange(
                from = currentDate,
                to = currentDate.shiftDay(HOMEWORK_FUTURE_DAYS).endThisDay(),
            )
            val overdueHomeworks = homeworksRepository.fetchOverdueHomeworks(currentTime).first()
            val rangedHomeworks = homeworksRepository.fetchHomeworksByTimeRange(targetTimeRange).first()
            val homeworks = (overdueHomeworks + rangedHomeworks)
                .associateBy { it.uid }
                .values
                .filter { !it.isDone && it.completeDate == null }
                .sortedBy { it.deadline }
            val groups = homeworks
                .groupBy { it.deadline.startThisDay() }
                .toSortedMap()
                .map { (date, dateHomeworks) ->
                    WidgetHomeworkGroup(
                        date = date,
                        items = dateHomeworks.map { homework ->
                            WidgetHomeworkItem(
                                uid = homework.uid,
                                subjectId = homework.subject?.uid,
                                organizationId = homework.organization.uid,
                                subjectName = homework.subject?.name,
                                subjectColor = homework.subject?.color,
                                deadline = homework.deadline,
                                status = HomeworkStatus.calculate(
                                    isDone = homework.isDone,
                                    completeDate = homework.completeDate,
                                    deadline = homework.deadline,
                                    currentTime = currentTime,
                                ),
                                theoreticalTasksCount = homework.theoreticalTasks
                                    .toHomeworkComponents()
                                    .fetchAllTasks()
                                    .size,
                                practicalTasksCount = homework.practicalTasks
                                    .toHomeworkComponents()
                                    .fetchAllTasks()
                                    .size,
                                presentationTasksCount = homework.presentationTasks
                                    .toHomeworkComponents()
                                    .fetchAllTasks()
                                    .size,
                            )
                        },
                    )
                }

            WidgetHomeworks(
                generatedAt = currentTime,
                groups = groups,
                nextUpdateAt = fetchNextUpdateAt(
                    currentTime = currentTime,
                    candidates = homeworks.map { it.deadline },
                ),
            )
        }

        override suspend fun fetchTodos() = eitherWrapper.wrap {
            val currentTime = dateManager.fetchCurrentInstant()
            val todos = todoRepository.fetchActiveTodos().first()
                .filterNot { it.isDone }
                .sortedWith(
                    compareBy(
                        { todo ->
                            val deadline = todo.deadline
                            when {
                                deadline != null && deadline < currentTime -> 0
                                deadline != null -> 1
                                else -> 2
                            }
                        },
                        { todo -> todo.deadline?.toEpochMilliseconds() ?: Long.MAX_VALUE },
                        { todo -> todo.createdAt.toEpochMilliseconds() },
                    )
                )
            val items = todos.map { todo ->
                val deadline = todo.deadline
                WidgetTodoItem(
                    uid = todo.uid,
                    name = todo.name,
                    description = todo.description?.takeIf { it.isNotBlank() },
                    deadline = deadline,
                    priority = todo.priority,
                    status = if (deadline != null && deadline < currentTime) {
                        WidgetTodoStatus.OVERDUE
                    } else {
                        WidgetTodoStatus.ACTIVE
                    },
                )
            }

            WidgetTodos(
                generatedAt = currentTime,
                items = items,
                nextUpdateAt = fetchNextUpdateAt(
                    currentTime = currentTime,
                    candidates = todos.mapNotNull { it.deadline },
                ),
            )
        }

        override suspend fun fetchGoals() = eitherWrapper.wrap {
            val currentTime = dateManager.fetchCurrentInstant()
            val currentDate = currentTime.startThisDay()
            val goals = goalsRepository.fetchDailyGoalsByDate(currentDate).first()
                .sortedBy { it.number }
            val items = goals.map { goal -> goal.toWidgetItem(currentTime) }
            val timeBoundaries = goals.mapNotNull { goal ->
                val targetTime = goal.fetchTargetTime() ?: return@mapNotNull null
                val elapsedTime = goal.fetchElapsedTime(currentTime)
                val remainingTime = targetTime - elapsedTime
                if (goal.time.isActive() && remainingTime > 0L) {
                    Instant.fromEpochMilliseconds(currentTime.toEpochMilliseconds() + remainingTime)
                } else {
                    null
                }
            }

            WidgetGoals(
                generatedAt = currentTime,
                items = items,
                nextUpdateAt = fetchNextUpdateAt(currentTime, timeBoundaries),
            )
        }

        override suspend fun fetchDisplaySettings() = eitherWrapper.wrap {
            val settings = generalSettingsRepository.fetchSettings().first()
            WidgetDisplaySettings(
                theme = settings.themeType,
                language = settings.languageType,
            )
        }

        override suspend fun setTodoDone(todoId: UID, isDone: Boolean) = eitherWrapper.wrapUnit {
            todoCompletionInteractor.setDone(todoId, isDone)
        }

        private fun Class.toWidgetItem(
            date: Instant,
            currentTime: Instant,
        ): WidgetScheduleItem {
            val start = date.setHoursAndMinutes(timeRange.from)
            val end = date.setHoursAndMinutes(timeRange.to)
            val status = when {
                currentTime >= end -> WidgetScheduleStatus.COMPLETED
                currentTime >= start -> WidgetScheduleStatus.ACTIVE
                else -> WidgetScheduleStatus.UPCOMING
            }
            val targetOffice = office.ifBlank { subject?.office.orEmpty() }.takeIf { it.isNotBlank() }

            return WidgetScheduleItem(
                uid = uid,
                scheduleId = scheduleId,
                eventType = eventType,
                title = subject?.name ?: customData?.takeIf { it.isNotBlank() },
                office = targetOffice,
                color = subject?.color,
                start = start,
                end = end,
                status = status,
            )
        }

        private fun Goal.toWidgetItem(currentTime: Instant): WidgetGoalItem {
            val elapsedTime = fetchElapsedTime(currentTime)
            val targetTime = fetchTargetTime()
            val progress = if (targetTime != null && targetTime > 0L) {
                (elapsedTime / targetTime.toFloat()).coerceIn(0f, 1f)
            } else if (isDone) {
                1f
            } else {
                0f
            }
            val status = when {
                isDone -> WidgetGoalStatus.COMPLETED
                targetTime != null && targetTime > 0L && elapsedTime >= targetTime -> {
                    WidgetGoalStatus.ACHIEVED
                }
                else -> WidgetGoalStatus.ACTIVE
            }

            return WidgetGoalItem(
                uid = uid,
                number = number,
                contentType = contentType,
                title = contentHomework?.subject?.name ?: contentTodo?.name,
                color = contentHomework?.subject?.color,
                timeType = time.type,
                elapsedTime = elapsedTime,
                targetTime = targetTime,
                progress = progress,
                status = status,
            )
        }

        private fun Goal.fetchElapsedTime(currentTime: Instant): Long = when (val goalTime = time) {
            is GoalTime.Stopwatch -> goalTime.pastStopTime + if (goalTime.isActive) {
                currentTime.toEpochMilliseconds() - goalTime.startTimePoint.toEpochMilliseconds()
            } else {
                0L
            }
            is GoalTime.Timer -> goalTime.pastStopTime + if (goalTime.isActive) {
                currentTime.toEpochMilliseconds() - goalTime.startTimePoint.toEpochMilliseconds()
            } else {
                0L
            }
            GoalTime.None -> 0L
        }.coerceAtLeast(0L)

        private fun Goal.fetchTargetTime(): Long? = when (val goalTime = time) {
            is GoalTime.Timer -> goalTime.targetTime
            is GoalTime.Stopwatch -> desiredTime
            GoalTime.None -> desiredTime
        }

        private fun GoalTime.isActive(): Boolean = when (this) {
            is GoalTime.Stopwatch -> isActive
            is GoalTime.Timer -> isActive
            GoalTime.None -> false
        }

        private fun fetchNextUpdateAt(
            currentTime: Instant,
            candidates: List<Instant>,
        ): Instant {
            return (candidates + currentTime.startThisDay().shiftDay(1))
                .filter { it > currentTime }
                .minOrNull() ?: currentTime.startThisDay().shiftDay(1)
        }
    }
}

private const val HOMEWORK_FUTURE_DAYS = 14
