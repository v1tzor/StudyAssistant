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

package ru.aleshin.studyassistant.profile.impl.domain.interactors

import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.requests.FriendRequests
import ru.aleshin.studyassistant.core.domain.repositories.FriendRequestsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.profile.impl.domain.common.ProfileEitherWrapper
import ru.aleshin.studyassistant.profile.impl.domain.entities.ProfileFailures

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
internal interface FriendRequestsInteractor {

    suspend fun fetchAllFriendRequests(): FlowDomainResult<ProfileFailures, FriendRequests>

    class Base(
        private val requestsRepository: FriendRequestsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: ProfileEitherWrapper,
    ) : FriendRequestsInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchAllFriendRequests() = eitherWrapper.wrapFlow {
            requestsRepository.fetchShortRequestsByUser(targetUser)
        }
    }
}