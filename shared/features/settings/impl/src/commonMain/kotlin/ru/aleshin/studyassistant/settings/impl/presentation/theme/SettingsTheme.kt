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

package ru.aleshin.studyassistant.settings.impl.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.settings.impl.presentation.theme.tokens.LocalSettingsIcons
import ru.aleshin.studyassistant.settings.impl.presentation.theme.tokens.LocalSettingsStrings
import ru.aleshin.studyassistant.settings.impl.presentation.theme.tokens.fetchSettingsIcons
import ru.aleshin.studyassistant.settings.impl.presentation.theme.tokens.fetchSettingsStrings

/**
 * @author Stanislav Aleshin on 07.04.2024.
 */
@Composable
internal fun SettingsTheme(content: @Composable () -> Unit) {
    val icons = fetchSettingsIcons(StudyAssistantRes.colors.isDark)
    val strings = fetchSettingsStrings(StudyAssistantRes.language)

    CompositionLocalProvider(
        LocalSettingsIcons provides icons,
        LocalSettingsStrings provides strings,
        content = content,
    )
}