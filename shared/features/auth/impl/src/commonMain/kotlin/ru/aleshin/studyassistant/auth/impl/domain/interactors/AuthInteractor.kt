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

import entities.AuthCredentials
import entities.ForgotCredentials
import entities.users.AppUser
import functional.DomainResult
import functional.UnitDomainResult
import kotlinx.coroutines.delay
import ru.aleshin.studyassistant.auth.impl.domain.common.AuthEitherWrapper
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface AuthInteractor {

    suspend fun loginWithEmail(credentials: AuthCredentials) : DomainResult<AuthFailures, AppUser>

    suspend fun registerNewAccount(credentials: AuthCredentials) : DomainResult<AuthFailures, AppUser>

    suspend fun resetPassword(credentials: ForgotCredentials) : UnitDomainResult<AuthFailures>

    class Base(
        private val eitherWrapper: AuthEitherWrapper,
    ) : AuthInteractor {

        override suspend fun loginWithEmail(credentials: AuthCredentials) = eitherWrapper.wrap {
            // TODO Make firebase auth
            delay(2000L)
            return@wrap AppUser("0", "0", "Example", "example@example.com", "0")
        }

        override suspend fun registerNewAccount(credentials: AuthCredentials) = eitherWrapper.wrap {
            delay(2000L)
            return@wrap AppUser("0", "0", "Example", "example@example.com", "0")
        }

        override suspend fun resetPassword(credentials: ForgotCredentials) = eitherWrapper.wrap {
            delay(2000L)
        }
    }
}