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

package ru.aleshin.studyassistant.core.ui.mappers

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.settings.ThemeType
import ru.aleshin.studyassistant.core.ui.models.ThemeUiType
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.dark_theme_title as core_dark_theme_title
import ru.aleshin.studyassistant.core.ui.resources.default_title as core_default_title
import ru.aleshin.studyassistant.core.ui.resources.light_theme_title as core_light_theme_title

/**
 * @author Stanislav Aleshin on 25.04.2024.
 */
fun ThemeType.mapToUi() = when (this) {
    ThemeType.DEFAULT -> ThemeUiType.DEFAULT
    ThemeType.LIGHT -> ThemeUiType.LIGHT
    ThemeType.DARK -> ThemeUiType.DARK
}

fun ThemeUiType.mapToDomain() = when (this) {
    ThemeUiType.DEFAULT -> ThemeType.DEFAULT
    ThemeUiType.LIGHT -> ThemeType.LIGHT
    ThemeUiType.DARK -> ThemeType.DARK
}

@Composable
fun ThemeUiType.mapToString() = when (this) {
    ThemeUiType.DEFAULT -> stringResource(CoreRes.string.core_default_title)
    ThemeUiType.LIGHT -> stringResource(CoreRes.string.core_light_theme_title)
    ThemeUiType.DARK -> stringResource(CoreRes.string.core_dark_theme_title)
}