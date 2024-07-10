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

package ru.aleshin.studyassistant.settings.impl.domain.interactors

import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.FlowWorkResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.settings.GeneralSettings
import ru.aleshin.studyassistant.core.domain.repositories.GeneralSettingsRepository
import ru.aleshin.studyassistant.settings.impl.domain.common.SettingsEitherWrapper
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
internal interface GeneralSettingsInteractor {

    suspend fun fetchSettings(): FlowWorkResult<SettingsFailures, GeneralSettings>
    suspend fun updateSettings(settings: GeneralSettings): UnitDomainResult<SettingsFailures>

    class Base(
        private val settingsRepository: GeneralSettingsRepository,
        private val eitherWrapper: SettingsEitherWrapper,
    ) : GeneralSettingsInteractor {

        override suspend fun fetchSettings() = eitherWrapper.wrapFlow {
            settingsRepository.fetchSettings()
        }

        override suspend fun updateSettings(settings: GeneralSettings) = eitherWrapper.wrapUnit {
            settingsRepository.updateSettings(settings)
        }
    }
}