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

package ru.aleshin.studyassistant.settings.impl.domain.interactors

import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.settings.impl.domain.common.SettingsEitherWrapper
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures

/**
 * @author Stanislav Aleshin on 28.08.2024.
 */
internal interface SyncInteractor {

    suspend fun transferRemoteData(): UnitDomainResult<SettingsFailures>

    suspend fun transferLocalData(): UnitDomainResult<SettingsFailures>

    class Base(
        private val organizationsRepository: OrganizationsRepository,
        private val subjectsRepository: SubjectsRepository,
        private val employeeRepository: EmployeeRepository,
        private val homeworksRepository: HomeworksRepository,
        private val todosRepository: TodoRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: SettingsEitherWrapper,
    ) : SyncInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun transferRemoteData() = eitherWrapper.wrapUnit {
            transferData(DataTransferDirection.REMOTE_TO_LOCAL)
        }

        override suspend fun transferLocalData() = eitherWrapper.wrapUnit {
            transferData(DataTransferDirection.LOCAL_TO_REMOTE)
        }

        private suspend fun transferData(direction: DataTransferDirection) {
            subjectsRepository.transferData(direction, targetUser)
            employeeRepository.transferData(direction, targetUser)
            homeworksRepository.transferData(direction, targetUser)
            todosRepository.transferData(direction, targetUser)
            baseScheduleRepository.transferData(direction, targetUser)
            customScheduleRepository.transferData(direction, targetUser)
            calendarSettingsRepository.transferData(direction, targetUser)
            organizationsRepository.transferData(direction, targetUser)
        }
    }
}