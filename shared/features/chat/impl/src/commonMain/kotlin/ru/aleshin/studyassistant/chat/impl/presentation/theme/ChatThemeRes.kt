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

package ru.aleshin.studyassistant.chat.impl.presentation.theme

import androidx.compose.runtime.Composable
import ru.aleshin.studyassistant.chat.impl.presentation.theme.tokens.ChatIcons
import ru.aleshin.studyassistant.chat.impl.presentation.theme.tokens.ChatStrings
import ru.aleshin.studyassistant.chat.impl.presentation.theme.tokens.LocalChatIcons
import ru.aleshin.studyassistant.chat.impl.presentation.theme.tokens.LocalChatStrings

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal object ChatThemeRes {

    val icons: ChatIcons
        @Composable get() = LocalChatIcons.current

    val strings: ChatStrings
        @Composable get() = LocalChatStrings.current
}