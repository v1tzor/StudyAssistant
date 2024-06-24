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

package ru.aleshin.studyassistant.schedule.impl.domain.interactors

import entities.tasks.Homework
import functional.UID
import functional.UnitDomainResult
import repositories.HomeworksRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures

/**
 * @author Stanislav Aleshin on 21.06.2024.
 */
internal interface HomeworkInteractor {

    suspend fun updateHomework(homework: Homework): UnitDomainResult<ScheduleFailures>

    class Base(
        private val homeworksRepository: HomeworksRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: ScheduleEitherWrapper,
    ) : HomeworkInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun updateHomework(homework: Homework) = eitherWrapper.wrapUnit {
            homeworksRepository.addOrUpdateHomework(homework, targetUser)
        }
    }
}