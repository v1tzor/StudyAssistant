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

import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.common.DataTransferDirection
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.CustomScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.DailyGoalsRepository
import ru.aleshin.studyassistant.core.domain.repositories.EmployeeRepository
import ru.aleshin.studyassistant.core.domain.repositories.HomeworksRepository
import ru.aleshin.studyassistant.core.domain.repositories.OrganizationsRepository
import ru.aleshin.studyassistant.core.domain.repositories.SubjectsRepository
import ru.aleshin.studyassistant.core.domain.repositories.TodoRepository
import ru.aleshin.studyassistant.settings.impl.domain.common.SettingsEitherWrapper
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures

/**
 * @author Stanislav Aleshin on 28.08.2024.
 */
internal interface SyncInteractor {

    suspend fun transferRemoteData(mergeData: Boolean): UnitDomainResult<SettingsFailures>

    suspend fun transferLocalData(mergeData: Boolean): UnitDomainResult<SettingsFailures>

    class Base(
        private val organizationsRepository: OrganizationsRepository,
        private val subjectsRepository: SubjectsRepository,
        private val employeeRepository: EmployeeRepository,
        private val homeworksRepository: HomeworksRepository,
        private val todosRepository: TodoRepository,
        private val baseScheduleRepository: BaseScheduleRepository,
        private val goalsRepository: DailyGoalsRepository,
        private val customScheduleRepository: CustomScheduleRepository,
        private val calendarSettingsRepository: CalendarSettingsRepository,
        private val eitherWrapper: SettingsEitherWrapper,
    ) : SyncInteractor {

        override suspend fun transferRemoteData(mergeData: Boolean) = eitherWrapper.wrapUnit {
            transferData(DataTransferDirection.REMOTE_TO_LOCAL, mergeData)
        }

        override suspend fun transferLocalData(mergeData: Boolean) = eitherWrapper.wrapUnit {
            transferData(DataTransferDirection.LOCAL_TO_REMOTE, mergeData)
        }

        private suspend fun transferData(direction: DataTransferDirection, mergeData: Boolean) {
            subjectsRepository.transferData(direction, mergeData)
            employeeRepository.transferData(direction, mergeData)
            homeworksRepository.transferData(direction, mergeData)
            todosRepository.transferData(direction, mergeData)
            baseScheduleRepository.transferData(direction, mergeData)
            customScheduleRepository.transferData(direction, mergeData)
            calendarSettingsRepository.transferData(direction)
            organizationsRepository.transferData(direction, mergeData)
            goalsRepository.transferData(direction, mergeData)
        }
    }
}