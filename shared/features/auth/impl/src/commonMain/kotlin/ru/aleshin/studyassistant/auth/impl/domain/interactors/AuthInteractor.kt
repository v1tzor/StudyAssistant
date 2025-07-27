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
import ru.aleshin.studyassistant.auth.impl.domain.common.AuthEitherWrapper
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthFailures
import ru.aleshin.studyassistant.auth.impl.domain.entites.AuthResult
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.common.platform.services.CrashlyticsService
import ru.aleshin.studyassistant.core.domain.entities.auth.AuthCredentials
import ru.aleshin.studyassistant.core.domain.entities.auth.ForgotCredentials
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.entities.users.AuthUser
import ru.aleshin.studyassistant.core.domain.entities.users.UserDevice
import ru.aleshin.studyassistant.core.domain.entities.users.UserSession
import ru.aleshin.studyassistant.core.domain.managers.sync.SourceSyncFacade
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.ManageUserRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
internal interface AuthInteractor {

    suspend fun loginWithEmail(credentials: AuthCredentials, device: UserDevice): DomainResult<AuthFailures, AuthUser>
    suspend fun registerNewAccount(credentials: AuthCredentials, device: UserDevice): DomainResult<AuthFailures, AppUser>
    suspend fun resetPassword(credentials: ForgotCredentials): UnitDomainResult<AuthFailures>
    suspend fun confirmOAuthLogin(session: UserSession, device: UserDevice): DomainResult<AuthFailures, AuthResult>
    suspend fun sendEmailVerification(): UnitDomainResult<AuthFailures>
    suspend fun signOut(deviceId: UID): UnitDomainResult<AuthFailures>

    class Base(
        private val authRepository: AuthRepository,
        private val usersRepository: UsersRepository,
        private val messageRepository: MessageRepository,
        private val manageUserRepository: ManageUserRepository,
        private val generalSettingsRepository: GeneralSettingsRepository,
        private val dateManager: DateManager,
        private val crashlyticsService: CrashlyticsService,
        private val sourceSyncFacade: SourceSyncFacade,
        private val eitherWrapper: AuthEitherWrapper,
    ) : AuthInteractor {

        override suspend fun loginWithEmail(credentials: AuthCredentials, device: UserDevice) = eitherWrapper.wrap {
            val userSession = authRepository.signInWithEmail(credentials)
            val authUser = usersRepository.fetchCurrentAuthUser() ?: throw AppwriteUserException()
            val userInfo = usersRepository.fetchRealtimeUserById(userSession.userId) ?: throw AppwriteUserException()

            if (userInfo.devices.find { it.deviceId == device.deviceId } == null) {
                val updatedDevices = buildList {
                    addAll(userInfo.devices)
                    add(device)
                }
                usersRepository.updateCurrentUserProfile(userInfo.copy(devices = updatedDevices))
            }

            crashlyticsService.setupUser(userSession.id)

            return@wrap authUser
        }

        override suspend fun confirmOAuthLogin(session: UserSession, device: UserDevice) = eitherWrapper.wrap {
            val authUser = usersRepository.fetchCurrentAuthUser() ?: throw AppwriteUserException()
            val userInfo = usersRepository.fetchRealtimeUserById(authUser.uid)

            crashlyticsService.setupUser(authUser.uid)

            return@wrap if (userInfo != null) {
                AuthResult(authUser = authUser, isNewUser = false)
            } else {
                val newUserInfo = AppUser.createNewUser(
                    uid = session.userId,
                    device = device,
                    username = checkNotNull(authUser.name.takeIf { it.isNotEmpty() } ?: authUser.email),
                    email = checkNotNull(authUser.email),
                    createdAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
                )
                usersRepository.createNewUserProfile(newUserInfo)
                manageUserRepository.sendVerifyEmail()

                AuthResult(authUser = authUser, isNewUser = true)
            }
        }

        override suspend fun registerNewAccount(credentials: AuthCredentials, device: UserDevice) = eitherWrapper.wrap {
            val authUser = authRepository.registerByEmail(credentials)
            val newUserInfo = AppUser.createNewUser(
                uid = authUser.uid,
                device = device,
                username = credentials.username ?: credentials.email,
                email = credentials.email,
                createdAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
            )
            usersRepository.createNewUserProfile(newUserInfo)
            manageUserRepository.sendVerifyEmail()

            val settings = generalSettingsRepository.fetchSettings().first()
            val updatedSettings = settings.copy(isUnfinishedSetup = authUser.uid)
            generalSettingsRepository.updateSettings(updatedSettings)

            crashlyticsService.setupUser(authUser.uid)

            return@wrap newUserInfo
        }

        override suspend fun resetPassword(credentials: ForgotCredentials) = eitherWrapper.wrap {
            manageUserRepository.sendPasswordResetEmail(credentials.email)
        }

        override suspend fun sendEmailVerification() = eitherWrapper.wrapUnit {
            manageUserRepository.sendVerifyEmail()
        }

        override suspend fun signOut(deviceId: UID) = eitherWrapper.wrap {
            val userInfo = usersRepository.fetchCurrentUserProfile().first() ?: throw AppwriteUserException()
            val deviceInfo = userInfo.devices.find { it.deviceId == deviceId }
            if (deviceInfo != null) {
                val updatedDevices = buildList {
                    addAll(userInfo.devices)
                    remove(deviceInfo)
                }
                val updatedUserInfo = userInfo.copy(devices = updatedDevices)
                usersRepository.updateAnotherUserProfile(updatedUserInfo, userInfo.uid)
            }
            crashlyticsService.setupUser(null)
            sourceSyncFacade.stopAllSourceSync()
            authRepository.signOut()
            messageRepository.deleteToken()
        }
    }
}