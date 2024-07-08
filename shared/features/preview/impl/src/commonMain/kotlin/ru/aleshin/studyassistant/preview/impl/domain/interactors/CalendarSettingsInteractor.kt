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

import ru.aleshin.studyassistant.core.common.functional.FlowDomainResult
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.UnitDomainResult
import ru.aleshin.studyassistant.core.domain.entities.settings.CalendarSettings
import ru.aleshin.studyassistant.core.domain.repositories.CalendarSettingsRepository
import ru.aleshin.studyassistant.core.domain.repositories.UsersRepository
import ru.aleshin.studyassistant.preview.impl.domain.common.PreviewEitherWrapper
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
internal interface CalendarSettingsInteractor {

    suspend fun fetchCalendarSettings(): FlowDomainResult<PreviewFailures, CalendarSettings>

    suspend fun updateCalendarSettings(settings: CalendarSettings): UnitDomainResult<PreviewFailures>

    class Base(
        private val settingsRepository: CalendarSettingsRepository,
        private val usersRepository: UsersRepository,
        private val eitherWrapper: PreviewEitherWrapper,
    ) : CalendarSettingsInteractor {

        private val targetUser: UID
            get() = usersRepository.fetchCurrentUserOrError().uid

        override suspend fun fetchCalendarSettings() = eitherWrapper.wrapFlow {
            settingsRepository.fetchSettings(targetUser)
        }

        override suspend fun updateCalendarSettings(settings: CalendarSettings) = eitherWrapper.wrap {
            settingsRepository.updateSettings(settings, targetUser)
        }
    }
}