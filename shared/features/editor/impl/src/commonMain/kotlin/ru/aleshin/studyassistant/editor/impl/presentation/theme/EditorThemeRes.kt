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
import ru.aleshin.studyassistant.editor.impl.presentation.theme.tokens.EditorIcons
import ru.aleshin.studyassistant.editor.impl.presentation.theme.tokens.EditorStrings
import ru.aleshin.studyassistant.editor.impl.presentation.theme.tokens.LocalEditorIcons
import ru.aleshin.studyassistant.editor.impl.presentation.theme.tokens.LocalEditorStrings

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal object EditorThemeRes {

    val icons: EditorIcons
        @Composable get() = LocalEditorIcons.current

    val strings: EditorStrings
        @Composable get() = LocalEditorStrings.current
}