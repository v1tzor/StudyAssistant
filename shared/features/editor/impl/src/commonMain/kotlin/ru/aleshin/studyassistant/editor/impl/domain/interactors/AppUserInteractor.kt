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

package ru.aleshin.studyassistant.editor.impl.domain.interactors

import kotlinx.coroutines.flow.filterNotNull
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.repositories.ManageUserRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
internal interface AppUserInteractor {

    suspend fun fetchAppUser(): FlowDomainResult<EditorFailures, AppUser>
    suspend fun fetchAppUserPaidStatus(): FlowDomainResult<EditorFailures, Boolean>
    suspend fun updateUser(user: AppUser): UnitDomainResult<EditorFailures>
    suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile): DomainResult<EditorFailures, String>
    suspend fun updatePassword(oldPassword: String, newPassword: String): UnitDomainResult<EditorFailures>
    suspend fun deleteAvatar(avatarUrl: String): UnitDomainResult<EditorFailures>

    class Base(
        private val usersRepository: UsersRepository,
        private val manageUserRepository: ManageUserRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: EditorEitherWrapper,
    ) : AppUserInteractor {

        override suspend fun fetchAppUser() = eitherWrapper.wrapFlow {
            usersRepository.fetchCurrentUserProfile().filterNotNull()
        }

        override suspend fun fetchAppUserPaidStatus() = eitherWrapper.wrapFlow {
            usersRepository.fetchCurrentUserPaidStatus()
        }

        override suspend fun updateUser(user: AppUser) = eitherWrapper.wrapUnit {
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            val updatedUser = user.copy(updatedAt = updatedAt)
            usersRepository.updateCurrentUserProfile(updatedUser)
        }

        override suspend fun uploadAvatar(oldAvatarUrl: String?, file: InputFile) = eitherWrapper.wrap {
            usersRepository.uploadCurrentUserAvatar(oldAvatarUrl, file)
        }

        override suspend fun updatePassword(oldPassword: String, newPassword: String) = eitherWrapper.wrap {
            manageUserRepository.updatePassword(oldPassword, newPassword)
        }

        override suspend fun deleteAvatar(avatarUrl: UID) = eitherWrapper.wrap {
            usersRepository.deleteCurrentUserAvatar(avatarUrl)
        }
    }
}