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

package ru.aleshin.studyassistant.auth.impl.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import ru.aleshin.studyassistant.auth.impl.presentation.theme.tokens.LocalAuthIcons
import ru.aleshin.studyassistant.auth.impl.presentation.theme.tokens.LocalAuthStrings
import ru.aleshin.studyassistant.auth.impl.presentation.theme.tokens.fetchAuthIcons
import ru.aleshin.studyassistant.auth.impl.presentation.theme.tokens.fetchAuthStrings
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 07.04.2024.
 */
@Composable
internal fun AuthTheme(content: @Composable () -> Unit) {
    val icons = fetchAuthIcons(StudyAssistantRes.colors.isDark)
    val strings = fetchAuthStrings(StudyAssistantRes.language)

    CompositionLocalProvider(
        LocalAuthIcons provides icons,
        LocalAuthStrings provides strings,
        content = content,
    )
}