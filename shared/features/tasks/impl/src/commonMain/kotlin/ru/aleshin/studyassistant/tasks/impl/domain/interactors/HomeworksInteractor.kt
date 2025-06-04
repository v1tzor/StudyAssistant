/*
 * Copyright 2024 Stanislav Aleshin
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

package ru.aleshin.studyassistant.tasks.impl.domain.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.tasks.DailyHomeworks
import ru.aleshin.studyassistant.core.domain.entities.tasks.DailyHomeworksStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkScope
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.convertToDetails
import ru.aleshin.studyassistant.core.domain.entities.tasks.fetchAllTasks
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.HomeworkErrors
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures

/**
 * @author Stanislav Aleshin on 20.06.2024.
 */
internal interface HomeworksInteractor {

    suspend fun addHomeworksGroup(homeworks: List<Homework>): UnitDomainResult<TasksFailures>
    suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange): FlowDomainResult<TasksFailures, Map<Instant, DailyHomeworks>>
    suspend fun fetchHomeworkErrors(targetDate: Instant): FlowDomainResult<TasksFailures, HomeworkErrors>
    suspend fun updateHomework(homework: Homework): UnitDomainResult<TasksFailures>
    fun calculateHomeworkScope(homeworksMap: Map<Instant, DailyHomeworks>): HomeworkScope

    class Base(
        private val homeworksRepository: HomeworksRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val usersRepository: UsersRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: TasksEitherWrapper,
    ) : HomeworksInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addHomeworksGroup(homeworks: List<Homework>) = eitherWrapper.wrapUnit {
            homeworksRepository.addHomeworksGroup(homeworks, targetUser)
        }

        override suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange) = eitherWrapper.wrapFlow {
            val ticker = dateManager.secondTicker()
            val homeworksFlow = homeworksRepository.fetchHomeworksByTimeRange(timeRange, targetUser)

            return@wrapFlow combine(homeworksFlow, ticker) { homeworks, _ ->
                val currentTime = dateManager.fetchCurrentInstant()
                val detailsHomeworks = homeworks.map { homework ->
                    val status = HomeworkStatus.calculate(
                        isDone = homework.isDone,
                        completeDate = homework.completeDate,
                        deadline = homework.deadline,
                        currentTime = currentTime,
                    )
                    return@map homework.convertToDetails(status)
                }
                val groupedDetailsHomeworks = detailsHomeworks.groupBy { homework ->
                    homework.deadline.startThisDay()
                }
                val dailyHomeworksMap = buildMap {
                    timeRange.periodDates().forEach { date ->
                        val homeworksByDate = groupedDetailsHomeworks[date] ?: emptyList()
                        val dailyHomeworks = DailyHomeworks(
                            dailyStatus = DailyHomeworksStatus.calculate(
                                targetDate = date,
                                currentDate = currentTime.startThisDay(),
                                homeworkStatuses = homeworksByDate.map { it.status }
                            ),
                            homeworks = homeworksByDate.groupBy { it.status },
                        )
                        put(date, dailyHomeworks)
                    }
                }
                return@combine dailyHomeworksMap
            }
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchHomeworkErrors(targetDate: Instant) = eitherWrapper.wrapFlow {
            val maxNumberOfWeek = calendarSettingsRepository.fetchSettings(targetUser).first().numberOfWeek

            val overdueHomeworksFlow = homeworksRepository.fetchOverdueHomeworks(targetDate, targetUser)
            val activeLinkedHomeworksFlow = homeworksRepository.fetchActiveLinkedHomeworks(targetDate, targetUser)

            return@wrapFlow overdueHomeworksFlow.flatMapLatest { overdueHomeworks ->
                activeLinkedHomeworksFlow.map { activeLinkedHomeworks ->
                    val detachedActiveTasks = buildList {
                        activeLinkedHomeworks.forEach { homework ->
                            val homeworkDate = homework.deadline.startThisDay()
                            val homeworkNumberOfWeek = homeworkDate.dateTime().date.numberOfRepeatWeek(maxNumberOfWeek)
                            val customScheduleByDate = customScheduleRepository.fetchScheduleByDate(
                                homeworkDate,
                                targetUser
                            ).first()
                            val classesByDate = customScheduleByDate?.classes
                                ?: baseScheduleRepository.fetchScheduleByDate(homeworkDate, homeworkNumberOfWeek, targetUser).first()?.classes
                            if (classesByDate?.find { it.uid == homework.classId } == null) {
                                add(homework)
                            }
                        }
                    }
                    HomeworkErrors(
                        overdueTasks = overdueHomeworks,
                        detachedActiveTasks = detachedActiveTasks,
                    )
                }
            }
        }

        override suspend fun updateHomework(homework: Homework) = eitherWrapper.wrapUnit {
            homeworksRepository.addOrUpdateHomework(homework, targetUser)
        }

        override fun calculateHomeworkScope(homeworksMap: Map<Instant, DailyHomeworks>): HomeworkScope {
            return HomeworkScope(
                theoreticalTasks = homeworksMap.mapValues { homeworkEntry ->
                    homeworkEntry.value.homeworks.values.toList().extractAllItem().sumOf { homework ->
                        return@sumOf homework.theoreticalTasks.components.fetchAllTasks().size
                    }
                },
                practicalTasks = homeworksMap.mapValues { homeworkEntry ->
                    homeworkEntry.value.homeworks.values.toList().extractAllItem().sumOf { homework ->
                        return@sumOf homework.practicalTasks.components.fetchAllTasks().size
                    }
                },
                presentationTasks = homeworksMap.mapValues { homeworkEntry ->
                    homeworkEntry.value.homeworks.values.toList().extractAllItem().sumOf { homework ->
                        return@sumOf homework.presentationTasks.components.fetchAllTasks().size
                    }
                },
            )
        }
    }
}