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

package ru.aleshin.studyassistant.editor.impl.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import ru.aleshin.studyassistant.editor.impl.presentation.theme.tokens.LocalEditorIcons
import ru.aleshin.studyassistant.editor.impl.presentation.theme.tokens.LocalEditorStrings
import ru.aleshin.studyassistant.editor.impl.presentation.theme.tokens.fetchEditorIcons
import ru.aleshin.studyassistant.editor.impl.presentation.theme.tokens.fetchEditorStrings
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Composable
internal fun EditorTheme(content: @Composable () -> Unit) {
    val icons = fetchEditorIcons(StudyAssistantRes.colors.isDark)
    val strings = fetchEditorStrings(StudyAssistantRes.language)

    CompositionLocalProvider(
        LocalEditorIcons provides icons,
        LocalEditorStrings provides strings,
        content = content,
    )
}
