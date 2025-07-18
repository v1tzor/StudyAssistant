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

package ru.aleshin.studyassistant.core.data.mappers.settings

import ru.aleshin.studyassistant.core.domain.entities.settings.GeneralSettings
import ru.aleshin.studyassistant.core.domain.entities.settings.LanguageType
import ru.aleshin.studyassistant.core.domain.entities.settings.ThemeType
import ru.aleshin.studyassistant.sqldelight.settings.GeneralSettingsEntity

/**
 * @author Stanislav Aleshin on 25.04.2024.
 */
fun GeneralSettingsEntity.mapToDomain() = GeneralSettings(
    isFirstStart = is_first_start == 1L,
    isUnfinishedSetup = is_unfinished_setup,
    themeType = ThemeType.valueOf(theme),
    languageType = LanguageType.valueOf(language),
)

fun GeneralSettings.mapToLocalData() = GeneralSettingsEntity(
    id = 1L,
    is_first_start = if (isFirstStart) 1L else 0L,
    is_unfinished_setup = isUnfinishedSetup,
    theme = themeType.name,
    language = languageType.name,
)