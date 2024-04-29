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

package ru.aleshin.studyassistant.auth.impl.domain.common

import handlers.ErrorHandler
import exceptions.FirebaseAuthException
import exceptions.FirebaseUserException
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures

/**
 * @author Stanislav Aleshin on 16.04.2024
 */
internal interface AuthErrorHandler : ErrorHandler<AuthFailures> {

    class Base : AuthErrorHandler {

        override fun handle(throwable: Throwable) = when (throwable) {
            is FirebaseAuthException -> AuthFailures.AuthorizationError
            is FirebaseUserException -> AuthFailures.NotFoundUserInfoError
            is IllegalArgumentException -> AuthFailures.CredentialsError
            else -> AuthFailures.OtherError(throwable)
        }
    }
}
