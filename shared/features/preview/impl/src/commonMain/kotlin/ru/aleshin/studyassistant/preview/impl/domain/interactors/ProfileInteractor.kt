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

package ru.aleshin.studyassistant.preview.impl.domain.interactors

import kotlinx.coroutines.flow.map
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.functional.DeviceInfoProvider
import ru.aleshin.studyassistant.core.common.functional.DomainResult
import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.files.InputFile
import ru.aleshin.studyassistant.core.domain.entities.users.Profile
import ru.aleshin.studyassistant.core.domain.repositories.ProfileRepository
import ru.aleshin.studyassistant.preview.impl.domain.common.PreviewEitherWrapper
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal interface ProfileInteractor {

    suspend fun fetchProfile(): FlowDomainResult<PreviewFailures, Profile>
    suspend fun updateProfile(profile: Profile): UnitDomainResult<PreviewFailures>
    suspend fun uploadAvatar(oldAvatar: String?, file: InputFile): DomainResult<PreviewFailures, String>
    suspend fun deleteAvatar(avatar: String): UnitDomainResult<PreviewFailures>

    class Base(
        private val profileRepository: ProfileRepository,
        private val dateManager: DateManager,
        private val deviceInfoProvider: DeviceInfoProvider,
        private val eitherWrapper: PreviewEitherWrapper,
    ) : ProfileInteractor {

        override suspend fun fetchProfile() = eitherWrapper.wrapFlow {
            profileRepository.fetchProfile().map { profile ->
                profile ?: Profile(
                    uid = randomUUID(),
                    username = if (deviceInfoProvider.fetchDeviceLanguage().startsWith(prefix = "ru", ignoreCase = true)) {
                        DEFAULT_USERNAME_RU
                    } else {
                        DEFAULT_USERNAME_EN
                    },
                    updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds(),
                )
            }
        }

        override suspend fun updateProfile(profile: Profile) = eitherWrapper.wrapUnit {
            val updatedAt = dateManager.fetchCurrentInstant().toEpochMilliseconds()
            profileRepository.updateProfile(profile.copy(updatedAt = updatedAt))
        }

        override suspend fun uploadAvatar(oldAvatar: String?, file: InputFile) =
            eitherWrapper.wrap {
                profileRepository.uploadAvatar(oldAvatar, file)
            }

        override suspend fun deleteAvatar(avatar: String) = eitherWrapper.wrapUnit {
            profileRepository.deleteAvatar(avatar)
        }

        private companion object {
            const val DEFAULT_USERNAME_RU = "Пользователь"
            const val DEFAULT_USERNAME_EN = "User"
        }
    }
}
