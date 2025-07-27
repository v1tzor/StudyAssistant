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

import kotlinx.coroutines.flow.filterNotNull
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
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
        private val dateManager: DateManager,
        private val eitherWrapper: PreviewEitherWrapper,
    ) : AppUserInteractor {

        override suspend fun fetchAppUser() = eitherWrapper.wrapFlow {
            usersRepository.fetchCurrentUserProfile().filterNotNull()
        }

        override suspend fun updateUser(user: AppUser) = eitherWrapper.wrapUnit {
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val updatedUser = user.copy(updatedAt = updatedAt)
            usersRepository.updateCurrentUserProfile(updatedUser)
        }

        override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile) = eitherWrapper.wrap {
            usersRepository.uploadCurrentUserAvatar(oldAvatarUrl, file)
        }

        override suspend fun deleteAvatar(avatarUrl: String) = eitherWrapper.wrap {
            usersRepository.deleteCurrentUserAvatar(avatarUrl)
        }

        override suspend fun fetchAppUserPaidStatus() = eitherWrapper.wrapFlow {
            usersRepository.fetchCurrentUserPaidStatus()
        }
    }
}