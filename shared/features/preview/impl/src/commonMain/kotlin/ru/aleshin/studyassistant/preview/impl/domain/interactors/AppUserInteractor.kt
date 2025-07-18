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

package ru.aleshin.studyassistant.preview.impl.domain.interactors

import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.preview.impl.domain.common.PreviewEitherWrapper
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
internal interface AppUserInteractor {

    suspend fun fetchAppUser(): FlowDomainResult<PreviewFailures, AppUser>
    suspend fun updateUser(user: AppUser): UnitDomainResult<PreviewFailures>
    suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile): DomainResult<PreviewFailures, String>
    suspend fun deleteAvatar(avatarUrl: String): UnitDomainResult<PreviewFailures>
    suspend fun fetchAppUserPaidStatus(): FlowDomainResult<PreviewFailures, Boolean>

    class Base(
        private val usersRepository: UsersRepository,
        private val eitherWrapper: PreviewEitherWrapper,
    ) : AppUserInteractor {

        override suspend fun fetchAppUser() = eitherWrapper.wrapFlow {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            usersRepository.fetchUserById(targetUser).map { appUser ->
                checkNotNull(appUser)
            }
        }

        override suspend fun updateUser(user: AppUser) = eitherWrapper.wrapUnit {
            usersRepository.updateAppUser(user)
        }

        override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile) = eitherWrapper.wrap {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            usersRepository.uploadUserAvatar(oldAvatarUrl, file, targetUser)
        }

        override suspend fun deleteAvatar(avatarUrl: String) = eitherWrapper.wrap {
            val targetUser = usersRepository.fetchCurrentUserOrError().uid
            usersRepository.deleteUserAvatar(avatarUrl, targetUser)
        }

        override suspend fun fetchAppUserPaidStatus() = eitherWrapper.wrapFlow {
            usersRepository.fetchCurrentUserPaidStatus()
        }
    }
}