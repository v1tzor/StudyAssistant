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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.common.numberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
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
    suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange): FlowDomainResult<TasksFailures, List<Homework>>
    suspend fun fetchHomeworkErrors(targetDate: Instant): FlowDomainResult<TasksFailures, HomeworkErrors>
    suspend fun updateHomework(homework: Homework): UnitDomainResult<TasksFailures>

    class Base(
        private val homeworksRepository: HomeworksRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: TasksEitherWrapper,
    ) : HomeworksInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun addHomeworksGroup(homeworks: List<Homework>) = eitherWrapper.wrapUnit {
            homeworksRepository.addHomeworksGroup(homeworks, targetUser)
        }

        override suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange) = eitherWrapper.wrapFlow {
            homeworksRepository.fetchHomeworksByTimeRange(timeRange, targetUser).map { homeworkList ->
                homeworkList.sortedBy { homework -> homework.deadline }
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
                            val customScheduleByDate = customScheduleRepository.fetchScheduleByDate(homeworkDate, targetUser).first()
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
    }
}