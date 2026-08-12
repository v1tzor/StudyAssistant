/*
 * Copyright 2026 Stanislav Aleshin
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
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.entities.users.Profile
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository
import ru.aleshin.studyassistant.editor.impl.domain.common.EditorEitherWrapper
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures

internal interface ProfileInteractor {

    suspend fun fetchProfile(): FlowDomainResult<EditorFailures, Profile>
    suspend fun updateProfile(profile: Profile): UnitDomainResult<EditorFailures>
    suspend fun uploadAvatar(oldAvatar: String?, file: InputFile): DomainResult<EditorFailures, String>
    suspend fun deleteAvatar(avatar: String): UnitDomainResult<EditorFailures>

    class Base(
        private val profileRepository: ProfileRepository,
        private val dateManager: DateManager,
        private val eitherWrapper: EditorEitherWrapper,
    ) : ProfileInteractor {

        override suspend fun fetchProfile() = eitherWrapper.wrapFlow {
            profileRepository.fetchProfile().filterNotNull()
        }

        override suspend fun updateProfile(profile: Profile) = eitherWrapper.wrapUnit {
            val updatedProfile = profile.copy(updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds())
            profileRepository.updateProfile(updatedProfile)
        }

        override suspend fun uploadAvatar(oldAvatar: String?, file: InputFile) = eitherWrapper.wrap {
            profileRepository.uploadAvatar(oldAvatar, file)
        }

        override suspend fun deleteAvatar(avatar: String) = eitherWrapper.wrapUnit {
            profileRepository.deleteAvatar(avatar)
        }
    }
}
