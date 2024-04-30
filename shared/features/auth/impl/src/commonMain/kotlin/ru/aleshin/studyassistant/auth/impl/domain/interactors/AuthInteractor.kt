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

import entities.auth.AuthCredentials
import entities.auth.ForgotCredentials
import entities.users.AppUser
import functional.DomainResult
import functional.UnitDomainResult
import kotlinx.coroutines.flow.firstOrNull
import exceptions.FirebaseAuthException
import exceptions.FirebaseUserException
import repositories.AuthRepository
import repositories.ManageUserRepository
import repositories.UsersRepository
import ru.aleshin.studyassistant.auth.impl.domain.common.AuthEitherWrapper
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthResult

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface AuthInteractor {

    suspend fun loginWithEmail(credentials: AuthCredentials) : DomainResult<AuthFailures, AppUser>

    suspend fun loginViaGoogle(idToken: String?) : DomainResult<AuthFailures, AuthResult>

    suspend fun registerNewAccount(credentials: AuthCredentials) : DomainResult<AuthFailures, AppUser>

    suspend fun resetPassword(credentials: ForgotCredentials) : UnitDomainResult<AuthFailures>

    class Base(
        private val authRepository: AuthRepository,
        private val usersRepository: UsersRepository,
        private val manageUserRepository: ManageUserRepository,
        private val eitherWrapper: AuthEitherWrapper,
    ) : AuthInteractor {

        override suspend fun loginWithEmail(credentials: AuthCredentials) = eitherWrapper.wrap {
            val firebaseUser = authRepository.signInWithEmail(credentials)
            val userInfo = usersRepository.fetchAppUserById(firebaseUser.uid).firstOrNull()
            return@wrap userInfo ?: throw FirebaseUserException()
        }

        override suspend fun loginViaGoogle(idToken: String?) = eitherWrapper.wrap {
            val firebaseUser = authRepository.signInViaGoogle(idToken)
            val userInfo = usersRepository.fetchAppUserById(firebaseUser.uid).firstOrNull()
            return@wrap if (userInfo != null) {
                AuthResult(user = userInfo, isNewUser = false)
            } else {
                val newUserInfo = AppUser.createNewUser(
                    uid = firebaseUser.uid,
                    username = checkNotNull(firebaseUser.displayName ?: firebaseUser.email),
                    email = checkNotNull(firebaseUser.email),
                )
                val createdResult = usersRepository.createOrUpdateAppUser(newUserInfo)
                if (createdResult) {
                    AuthResult(user = newUserInfo, isNewUser = true)
                } else {
                    throw FirebaseAuthException()
                }
            }
        }

        override suspend fun registerNewAccount(credentials: AuthCredentials) = eitherWrapper.wrap {
            val firebaseUser = authRepository.registerByEmail(credentials)
            val newUserInfo = AppUser.createNewUser(
                uid = firebaseUser.uid,
                username = credentials.username ?: credentials.email,
                email = credentials.email,
            )
            val createdResult = usersRepository.createOrUpdateAppUser(newUserInfo)
            return@wrap if (createdResult) {
                newUserInfo
            } else {
                throw FirebaseAuthException()
            }
        }

        override suspend fun resetPassword(credentials: ForgotCredentials) = eitherWrapper.wrap {
            manageUserRepository.sendPasswordResetEmail(credentials.email)
        }
    }
}