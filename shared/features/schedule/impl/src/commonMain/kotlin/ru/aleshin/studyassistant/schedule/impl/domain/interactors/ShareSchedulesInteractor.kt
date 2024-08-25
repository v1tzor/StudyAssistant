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

import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.share.scheules.ReceivedMediatedSchedules
import ru.aleshin.studyassistant.core.domain.repositories.ShareSchedulesRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.schedule.impl.domain.common.ScheduleEitherWrapper
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures

/**
 * @author Stanislav Aleshin on 14.08.2024.
 */
internal interface ShareSchedulesInteractor {

    suspend fun fetchReceivedSharedSchedules(shareId: UID): FlowDomainResult<ScheduleFailures, ReceivedMediatedSchedules>
    suspend fun acceptOrRejectSchedules(schedules: ReceivedMediatedSchedules): UnitDomainResult<ScheduleFailures>

    class Base(
        private val shareRepository: ShareSchedulesRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: ScheduleEitherWrapper,
    ) : ShareSchedulesInteractor {

        private val currentUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchReceivedSharedSchedules(shareId: UID) = eitherWrapper.wrapFlow {
            shareRepository.fetchShareSchedulesByUser(currentUser).map { sharedSchedules ->
                checkNotNull(sharedSchedules.received[shareId])
            }
        }

        override suspend fun acceptOrRejectSchedules(schedules: ReceivedMediatedSchedules) = eitherWrapper.wrapUnit {
            val currentSharedSchedules = shareRepository.fetchRealtimeSharedSchedulesByUser(currentUser)
            val senderSharedSchedules = shareRepository.fetchRealtimeSharedSchedulesByUser(schedules.sender.uid)

            val updatedCurrentSharedSchedules = currentSharedSchedules.copy(
                received = buildMap {
                    putAll(currentSharedSchedules.received)
                    remove(schedules.uid)
                }
            )
            val updatedSenderSharedSchedules = senderSharedSchedules.copy(
                sent = buildMap {
                    putAll(senderSharedSchedules.sent)
                    remove(schedules.uid)
                }
            )

            shareRepository.addOrUpdateSharedSchedules(
                schedules = updatedCurrentSharedSchedules,
                targetUser = currentUser,
            )
            shareRepository.addOrUpdateSharedSchedules(
                schedules = updatedSenderSharedSchedules,
                targetUser = schedules.sender.uid,
            )
        }
    }
}