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

import dev.gitlive.firebase.storage.File
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.users.AppUser
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.preview.impl.domain.common.PreviewEitherWrapper
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
internal interface UsersInteractor {

    suspend fun updateUser(user: AppUser): UnitDomainResult<PreviewFailures>
    suspend fun uploadAvatar(uid: UID, file: File): DomainResult<PreviewFailures, String>
    suspend fun fetchUserById(uid: UID): FlowDomainResult<PreviewFailures, AppUser?>
    suspend fun deleteAvatar(uid: UID): UnitDomainResult<PreviewFailures>

    class Base(
        private val usersRepository: UsersRepository,
        private val eitherWrapper: PreviewEitherWrapper,
    ) : UsersInteractor {

        override suspend fun fetchUserById(uid: UID) = eitherWrapper.wrapFlow {
            usersRepository.fetchUserById(uid)
        }

        override suspend fun updateUser(user: AppUser) = eitherWrapper.wrapUnit {
            usersRepository.addOrUpdateAppUser(user)
        }

        override suspend fun uploadAvatar(uid: UID, file: File) = eitherWrapper.wrap {
            usersRepository.uploadUserAvatar(uid, file)
        }
        override suspend fun deleteAvatar(uid: UID) = eitherWrapper.wrap {
            usersRepository.deleteUserAvatar(uid)
        }
    }
}