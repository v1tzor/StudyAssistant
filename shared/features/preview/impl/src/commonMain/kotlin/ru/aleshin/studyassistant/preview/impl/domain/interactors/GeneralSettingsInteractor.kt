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

import kotlinx.coroutines.flow.first
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.preview.impl.domain.common.PreviewEitherWrapper
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures

/**
 * @author Stanislav Aleshin on 25.04.2024.
 */
internal interface GeneralSettingsInteractor {

    suspend fun updateSetupStatus(setupUser: UID?): UnitDomainResult<PreviewFailures>

    class Base(
        private val settingsRepository: GeneralSettingsRepository,
        private val eitherWrapper: PreviewEitherWrapper,
    ) : GeneralSettingsInteractor {

        override suspend fun updateSetupStatus(setupUser: UID?) = eitherWrapper.wrapUnit {
            val settings = settingsRepository.fetchSettings().first()
            val updatedSettings = settings.copy(isUnfinishedSetup = setupUser)
            settingsRepository.updateSettings(updatedSettings)
        }
    }
}