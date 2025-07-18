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

import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.exceptions.AppwriteUserException
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.repositories.AuthRepository
import ru.aleshin.studyassistant.core.domain.repositories.MessageRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.profile.impl.domain.common.ProfileEitherWrapper
import ru.aleshin.studyassistant.profile.impl.domain.entities.ProfileFailures

/**
 * @author Stanislav Aleshin on 25.04.2024.
 */
internal interface AuthInteractor {

    suspend fun signOut(deviceId: UID): UnitDomainResult<ProfileFailures>

    class Base(
        private val authRepository: AuthRepository,
        private val messageRepository: MessageRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: ProfileEitherWrapper,
    ) : AuthInteractor {

        override suspend fun signOut(deviceId: UID) = eitherWrapper.wrap {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            val userInfo = usersRepository.fetchUserById(targetUser).first() ?: throw AppwriteUserException()

            val updatedDevices = buildList {
                addAll(userInfo.devices.filter { it.deviceId != deviceId })
            }
            val updatedUserInfo = userInfo.copy(devices = updatedDevices)
            usersRepository.updateAppUser(updatedUserInfo)

            authRepository.signOut()
            messageRepository.deleteToken()
        }
    }
}