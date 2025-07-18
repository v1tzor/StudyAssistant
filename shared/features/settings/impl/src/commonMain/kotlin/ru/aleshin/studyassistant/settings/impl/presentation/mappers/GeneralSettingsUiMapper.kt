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

package ru.aleshin.studyassistant.settings.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.settings.GeneralSettings
import ru.aleshin.studyassistant.core.ui.mappers.mapToDomain
import ru.aleshin.studyassistant.core.ui.mappers.mapToUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.GeneralSettingsUi

/**
 * @author Stanislav Aleshin on 10.07.2024.
 */
internal fun GeneralSettings.mapToUi() = GeneralSettingsUi(
    isFirstStart = isFirstStart,
    isUnfinishedSetup = isUnfinishedSetup,
    languageType = languageType.mapToUi(),
    themeType = themeType.mapToUi(),
)

internal fun GeneralSettingsUi.mapToDomain() = GeneralSettings(
    isFirstStart = isFirstStart,
    isUnfinishedSetup = isUnfinishedSetup,
    languageType = languageType.mapToDomain(),
    themeType = themeType.mapToDomain(),
)