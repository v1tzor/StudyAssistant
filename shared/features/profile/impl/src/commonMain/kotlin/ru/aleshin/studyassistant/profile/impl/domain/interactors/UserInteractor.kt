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

import kotlinx.coroutines.flow.filterNotNull
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.profile.impl.domain.common.ProfileEitherWrapper
import ru.aleshin.studyassistant.profile.impl.domain.entities.ProfileFailures

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
internal interface UserInteractor {

    suspend fun fetchAppUser(): FlowDomainResult<ProfileFailures, AppUser>

    suspend fun fetchAllFriends(): FlowDomainResult<ProfileFailures, List<AppUser>>

    class Base(
        private val usersRepository: UsersRepository,
        private val eitherWrapper: ProfileEitherWrapper,
    ) : UserInteractor {

        override suspend fun fetchAppUser() = eitherWrapper.wrapFlow {
            usersRepository.fetchCurrentUserProfile().filterNotNull()
        }

        override suspend fun fetchAllFriends() = eitherWrapper.wrapFlow {
            usersRepository.fetchCurrentUserFriends()
        }
    }
}