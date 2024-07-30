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

package ru.aleshin.studyassistant.info.impl.domain.interactors

import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.extensions.millis
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.repositories.BaseScheduleRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.info.impl.domain.common.InfoEitherWrapper
import ru.aleshin.studyassistant.info.impl.domain.entities.InfoFailures
import ru.aleshin.studyassistant.info.impl.domain.entities.OrganizationClassesInfo

/**
 * @author Stanislav Aleshin on 16.06.2024.
 */
internal interface ClassesInfoInteractor {

    suspend fun fetchClassesInfo(organizationId: UID): DomainResult<InfoFailures, OrganizationClassesInfo>

    class Base(
        private val scheduleRepository: BaseScheduleRepository,
        private val usersRepository: UsersRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: InfoEitherWrapper,
    ) : ClassesInfoInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchClassesInfo(organizationId: UID) = eitherWrapper.wrap {
            val currentWeek = dateManager.fetchCurrentWeek()
            val schedules = scheduleRepository.fetchSchedulesByVersion(currentWeek, null, targetUser)
            val classes = schedules.first().groupBy { it.week }.mapValues { entry ->
                val allClasses = entry.value.map { schedule -> schedule.classes }.extractAllItem()
                return@mapValues allClasses.filter { classModel ->
                    classModel.organization.uid == organizationId
                }
            }

            val numberOfClassesInWeek = classes.mapValues { entry ->
                entry.value.size
            }
            val classesDurationInWeek = classes.mapValues { entry ->
                entry.value.sumOf { it.timeRange.periodDuration().millis() }
            }

            return@wrap OrganizationClassesInfo(
                numberOfClassesInWeek = numberOfClassesInWeek,
                classesDurationInWeek = classesDurationInWeek,
            )
        }
    }
}