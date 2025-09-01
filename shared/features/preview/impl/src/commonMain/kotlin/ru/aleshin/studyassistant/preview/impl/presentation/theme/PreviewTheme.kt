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

package ru.aleshin.studyassistant.preview.impl.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.preview.impl.presentation.theme.tokens.LocalPreviewIcons
import ru.aleshin.studyassistant.preview.impl.presentation.theme.tokens.LocalPreviewStrings
import ru.aleshin.studyassistant.preview.impl.presentation.theme.tokens.fetchPreviewIcons
import ru.aleshin.studyassistant.preview.impl.presentation.theme.tokens.fetchPreviewStrings

/**
 * @author Stanislav Aleshin on 07.04.2024.
 */
@Composable
internal fun PreviewTheme(content: @Composable () -> Unit) {
    val isDark = StudyAssistantRes.colors.isDark
    val language = StudyAssistantRes.language

    val icons = remember(isDark) { fetchPreviewIcons(isDark) }
    val strings = remember { fetchPreviewStrings(language) }

    CompositionLocalProvider(
        LocalPreviewIcons provides icons,
        LocalPreviewStrings provides strings,
        content = content,
    )
}