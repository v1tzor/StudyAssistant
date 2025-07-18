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

package ru.aleshin.studyassistant.auth.impl.domain.interactors

import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.auth.impl.domain.common.AuthEitherWrapper
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository

/**
 * @author Stanislav Aleshin on 29.08.2024.
 */
internal interface AppUserInteractor {

    suspend fun fetchAppUser(): FlowDomainResult<AuthFailures, AppUser>

    suspend fun checkEmailVerification(): FlowDomainResult<AuthFailures, Boolean>

    class Base(
        private val usersRepository: UsersRepository,
        private val eitherWrapper: AuthEitherWrapper,
    ) : AppUserInteractor {

        override suspend fun fetchAppUser() = eitherWrapper.wrapFlow {
            val firebaseUser = checkNotNull(usersRepository.fetchCurrentAuthUser())
            usersRepository.fetchUserById(firebaseUser.uid).map { appUser ->
                checkNotNull(appUser)
            }
        }

        override suspend fun checkEmailVerification() = eitherWrapper.wrapFlow {
            usersRepository.fetchStateChanged().map { it?.emailVerification == true }
        }
    }
}