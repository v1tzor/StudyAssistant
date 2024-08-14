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

package ru.aleshin.studyassistant.domain.interactors

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.messages.UniversalPushToken
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.domain.common.MainEitherWrapper
import ru.aleshin.studyassistant.domain.entities.MainFailures

/**
 * @author Stanislav Aleshin on 25.04.2024.
 */
interface AppUserInteractor {

    suspend fun fetchAppUser(): FlowDomainResult<MainFailures, AppUser?>
    suspend fun fetchAppToken(): FlowDomainResult<MainFailures, UniversalPushToken>
    suspend fun updateUser(user: AppUser): UnitDomainResult<MainFailures>
    suspend fun checkIsAuthorized(): DomainResult<MainFailures, Boolean>

    class Base(
        private val usersRepository: UsersRepository,
        private val messagingRepository: MessageRepository,
        private val eitherWrapper: MainEitherWrapper,
    ) : AppUserInteractor {

        override suspend fun fetchAppToken() = eitherWrapper.wrapFlow {
            messagingRepository.fetchToken()
        }

        @OptIn(ExperimentalCoroutinesApi::class)
        override suspend fun fetchAppUser() = eitherWrapper.wrapFlow {
            usersRepository.fetchAuthStateChanged().flatMapLatest { authUser ->
                val appUserId = authUser?.uid ?: return@flatMapLatest flowOf(null)
                usersRepository.fetchUserById(appUserId)
            }
        }
        override suspend fun updateUser(user: AppUser) = eitherWrapper.wrapUnit {
            usersRepository.addOrUpdateAppUser(user)
        }

        override suspend fun checkIsAuthorized() = eitherWrapper.wrap {
            usersRepository.fetchCurrentAppUser() != null
        }
    }
}