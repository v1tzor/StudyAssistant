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
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.settings.CalendarSettings
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.settings.impl.domain.common.SettingsEitherWrapper
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
internal interface CalendarSettingsInteractor {

    suspend fun fetchSettings(): FlowWorkResult<SettingsFailures, CalendarSettings>
    suspend fun updateSettings(settings: CalendarSettings): UnitDomainResult<SettingsFailures>

    class Base(
        private val settingsRepository: CalendarSettingsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: SettingsEitherWrapper,
    ) : CalendarSettingsInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchSettings() = eitherWrapper.wrapFlow {
            settingsRepository.fetchSettings(targetUser)
        }

        override suspend fun updateSettings(settings: CalendarSettings) = eitherWrapper.wrapUnit {
            settingsRepository.updateSettings(settings, targetUser)
        }
    }
}