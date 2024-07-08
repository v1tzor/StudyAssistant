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

package ru.aleshin.studyassistant.domain.interactors

import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.settings.GeneralSettings
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.domain.common.MainEitherWrapper
import ru.aleshin.studyassistant.domain.entities.MainFailures

/**
 * @author Stanislav Aleshin on 25.04.2024.
 */
interface GeneralSettingsInteractor {

    suspend fun fetchSettings(): FlowDomainResult<MainFailures, GeneralSettings>

    suspend fun updateSettings(settings: GeneralSettings): UnitDomainResult<MainFailures>

    class Base(
        private val settingsRepository: GeneralSettingsRepository,
        private val eitherWrapper: MainEitherWrapper,
    ) : GeneralSettingsInteractor {

        override suspend fun fetchSettings() = eitherWrapper.wrapFlow {
            settingsRepository.fetchSettings()
        }

        override suspend fun updateSettings(settings: GeneralSettings) = eitherWrapper.wrap {
            settingsRepository.updateSettings(settings)
        }
    }
}