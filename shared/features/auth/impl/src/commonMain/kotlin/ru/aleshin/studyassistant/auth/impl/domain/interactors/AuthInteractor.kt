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

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import ru.aleshin.studyassistant.auth.impl.domain.common.AuthEitherWrapper
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthResult
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseDataAuthException
import ru.aleshin.studyassistant.core.common.exceptions.FirebaseUserException
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.auth.AuthCredentials
import ru.aleshin.studyassistant.core.domain.entities.auth.ForgotCredentials
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.entities.users.UserDevice
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.domain.repositories.ManageUserRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface AuthInteractor {

    suspend fun loginWithEmail(credentials: AuthCredentials, device: UserDevice): DomainResult<AuthFailures, AppUser>
    suspend fun loginViaGoogle(idToken: String?, device: UserDevice): DomainResult<AuthFailures, AuthResult>
    suspend fun registerNewAccount(credentials: AuthCredentials, device: UserDevice): DomainResult<AuthFailures, AppUser>
    suspend fun resetPassword(credentials: ForgotCredentials): UnitDomainResult<AuthFailures>
    suspend fun sendEmailVerification(): UnitDomainResult<AuthFailures>
    suspend fun signOut(deviceId: UID): UnitDomainResult<AuthFailures>

    class Base(
        private val authRepository: AuthRepository,
        private val usersRepository: UsersRepository,
        private val messageRepository: MessageRepository,
        private val manageUserRepository: ManageUserRepository,
        private val eitherWrapper: AuthEitherWrapper,
    ) : AuthInteractor {

        override suspend fun loginWithEmail(credentials: AuthCredentials, device: UserDevice) = eitherWrapper.wrap {
            val firebaseUser = authRepository.signInWithEmail(credentials)
            val userInfo = usersRepository.fetchUserById(firebaseUser.uid).first() ?: throw FirebaseUserException()

            if (userInfo.devices.find { it.deviceId == device.deviceId } == null) {
                val updatedDevices = buildList {
                    addAll(userInfo.devices)
                    add(device)
                }
                val updatedUserInfo = userInfo.copy(devices = updatedDevices).apply {
                    usersRepository.addOrUpdateAppUser(this)
                }
                return@wrap updatedUserInfo
            } else {
                return@wrap userInfo
            }
        }

        override suspend fun loginViaGoogle(idToken: String?, device: UserDevice) = eitherWrapper.wrap {
            val firebaseUser = authRepository.signInViaGoogle(idToken)
            val userInfo = usersRepository.fetchUserById(firebaseUser.uid).firstOrNull()

            return@wrap if (userInfo != null) {
                AuthResult(firebaseUser = firebaseUser, isNewUser = false)
            } else {
                val newUserInfo = AppUser.createNewUser(
                    uid = firebaseUser.uid,
                    device = device,
                    username = checkNotNull(firebaseUser.displayName ?: firebaseUser.email),
                    email = checkNotNull(firebaseUser.email),
                )
                val createdResult = usersRepository.addOrUpdateAppUser(newUserInfo)
                if (createdResult) {
                    manageUserRepository.sendVerifyEmail()
                    AuthResult(firebaseUser = firebaseUser, isNewUser = true)
                } else {
                    throw FirebaseDataAuthException()
                }
            }
        }

        override suspend fun registerNewAccount(credentials: AuthCredentials, device: UserDevice) = eitherWrapper.wrap {
            val firebaseUser = authRepository.registerByEmail(credentials)
            val newUserInfo = AppUser.createNewUser(
                uid = firebaseUser.uid,
                device = device,
                username = credentials.username ?: credentials.email,
                email = credentials.email,
            )
            val createdResult = usersRepository.addOrUpdateAppUser(newUserInfo)

            if (createdResult) {
                manageUserRepository.sendVerifyEmail()
                return@wrap newUserInfo
            } else {
                throw FirebaseDataAuthException()
            }
        }

        override suspend fun resetPassword(credentials: ForgotCredentials) = eitherWrapper.wrap {
            manageUserRepository.sendPasswordResetEmail(credentials.email)
        }

        override suspend fun sendEmailVerification() = eitherWrapper.wrapUnit {
            manageUserRepository.sendVerifyEmail()
        }

        override suspend fun signOut(deviceId: UID) = eitherWrapper.wrap {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            val userInfo = usersRepository.fetchUserById(targetUser).first() ?: throw FirebaseUserException()
            val deviceInfo = userInfo.devices.find { it.deviceId == deviceId }
            if (deviceInfo != null) {
                val updatedDevices = buildList {
                    addAll(userInfo.devices)
                    remove(deviceInfo)
                }
                val updatedUserInfo = userInfo.copy(devices = updatedDevices)
                usersRepository.addOrUpdateAppUser(updatedUserInfo)
            }
            authRepository.signOut()
            messageRepository.deleteToken()
        }
    }
}