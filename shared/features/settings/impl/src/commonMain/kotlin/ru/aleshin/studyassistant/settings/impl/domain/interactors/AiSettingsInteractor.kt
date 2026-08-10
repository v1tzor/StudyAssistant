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

package ru.aleshin.studyassistant.settings.impl.domain.interactors

import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.ai.AiServiceType
import ru.aleshin.studyassistant.core.domain.entities.ai.AiSettings
import ru.aleshin.studyassistant.core.domain.repositories.AiAssistantRepository
import ru.aleshin.studyassistant.core.domain.repositories.AiSettingsRepository
import ru.aleshin.studyassistant.settings.impl.domain.common.SettingsEitherWrapper
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
internal interface AiSettingsInteractor {
    suspend fun fetchSettings(): FlowDomainResult<SettingsFailures, AiSettings>
    suspend fun selectSharedService(): UnitDomainResult<SettingsFailures>
    suspend fun selectPersonalService(): UnitDomainResult<SettingsFailures>
    suspend fun testPersonalKey(apiKey: String): UnitDomainResult<SettingsFailures>
    suspend fun savePersonalKey(apiKey: String): UnitDomainResult<SettingsFailures>
    suspend fun deletePersonalKey(): UnitDomainResult<SettingsFailures>

    class Base(
        private val settingsRepository: AiSettingsRepository,
        private val assistantRepository: AiAssistantRepository,
        private val eitherWrapper: SettingsEitherWrapper,
    ) : AiSettingsInteractor {
        override suspend fun fetchSettings() = eitherWrapper.wrapFlow {
            settingsRepository.fetchSettings()
        }

        override suspend fun selectSharedService() = eitherWrapper.wrapUnit {
            settingsRepository.updateServiceType(AiServiceType.SHARED)
        }

        override suspend fun selectPersonalService() = eitherWrapper.wrapUnit {
            settingsRepository.updateServiceType(AiServiceType.PERSONAL)
        }

        override suspend fun testPersonalKey(apiKey: String) = eitherWrapper.wrapUnit {
            assistantRepository.testPersonalKey(apiKey)
        }

        override suspend fun savePersonalKey(apiKey: String) = eitherWrapper.wrapUnit {
            settingsRepository.savePersonalKey(apiKey)
            settingsRepository.updateServiceType(AiServiceType.PERSONAL)
        }

        override suspend fun deletePersonalKey() = eitherWrapper.wrapUnit {
            settingsRepository.deletePersonalKey()
        }
    }
}
