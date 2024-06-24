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

import entities.tasks.HomeworkDetails
import entities.tasks.convertToDetails
import functional.FlowDomainResult
import functional.TimeRange
import functional.UID
import kotlinx.coroutines.flow.map
import repositories.BaseScheduleRepository
import repositories.CustomScheduleRepository
import repositories.HomeworksRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.tasks.impl.domain.common.TasksEitherWrapper
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures

/**
 * @author Stanislav Aleshin on 20.06.2024.
 */
internal interface HomeworksInteractor {

    suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange): FlowDomainResult<TasksFailures, List<HomeworkDetails>>

    class Base(
        private val homeworksRepository: HomeworksRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: TasksEitherWrapper,
    ) : HomeworksInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchHomeworksByTimeRange(timeRange: TimeRange) = eitherWrapper.wrapFlow {
//            val baseSchedules = baseScheduleRepository.fetchSchedulesByTimeRange(timeRange, null, targetUser).first()
//            val customSchedules = customScheduleRepository.fetchSchedulesByTimeRange(timeRange, targetUser).first()
            val homeworksFlow = homeworksRepository.fetchHomeworksByTimeRange(timeRange, targetUser).map { homeworks ->
                homeworks.map { it.convertToDetails(null) }
            }
            homeworksFlow
        }
    }
}