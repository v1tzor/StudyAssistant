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
import ru.aleshin.studyassistant.core.common.functional.UID
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
    suspend fun uploadAvatar(file: InputFile): DomainResult<PreviewFailures, String>
    suspend fun deleteAvatar(): UnitDomainResult<PreviewFailures>

    class Base(
        private val usersRepository: UsersRepository,
        private val eitherWrapper: PreviewEitherWrapper,
    ) : AppUserInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchAppUser() = eitherWrapper.wrapFlow {
            usersRepository.fetchUserById(targetUser).map { appUser ->
                checkNotNull(appUser)
            }
        }

        override suspend fun updateUser(user: AppUser) = eitherWrapper.wrapUnit {
            usersRepository.addOrUpdateAppUser(user)
        }

        override suspend fun uploadAvatar(file: InputFile) = eitherWrapper.wrap {
            usersRepository.uploadUserAvatar(targetUser, file)
        }
        override suspend fun deleteAvatar() = eitherWrapper.wrap {
            usersRepository.deleteUserAvatar(targetUser)
        }
    }
}