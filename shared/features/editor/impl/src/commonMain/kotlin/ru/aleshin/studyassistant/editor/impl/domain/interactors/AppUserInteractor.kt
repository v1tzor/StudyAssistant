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

import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
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
    suspend fun uploadAvatar(uid: UID, file: InputFile): DomainResult<EditorFailures, String>
    suspend fun updatePassword(oldPassword: String, newPassword: String): UnitDomainResult<EditorFailures>
    suspend fun deleteAvatar(uid: UID): UnitDomainResult<EditorFailures>

    class Base(
        private val usersRepository: UsersRepository,
        private val manageUserRepository: ManageUserRepository,
        private val eitherWrapper: EditorEitherWrapper,
    ) : AppUserInteractor {

        private val currentUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchAppUser() = eitherWrapper.wrapFlow {
            usersRepository.fetchUserById(currentUser).map { appUser ->
                checkNotNull(appUser)
            }
        }

        override suspend fun fetchAppUserPaidStatus() = eitherWrapper.wrapFlow {
            usersRepository.fetchCurrentUserPaidStatus()
        }

        override suspend fun updateUser(user: AppUser) = eitherWrapper.wrapUnit {
            usersRepository.addOrUpdateAppUser(user)
        }

        override suspend fun uploadAvatar(uid: UID, file: InputFile) = eitherWrapper.wrap {
            usersRepository.uploadUserAvatar(uid, file)
        }

        override suspend fun updatePassword(oldPassword: String, newPassword: String) = eitherWrapper.wrap {
            manageUserRepository.updatePassword(oldPassword, newPassword)
        }

        override suspend fun deleteAvatar(uid: UID) = eitherWrapper.wrap {
            usersRepository.deleteUserAvatar(uid)
        }
    }
}