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

package ru.aleshin.studyassistant.chat.impl.presentation.theme.tokens

import androidx.compose.runtime.staticCompositionLocalOf

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal data class ChatIcons(
    val icon: Int,
) {
    companion object Companion {
        val LIGHT = ChatIcons(
            icon = 0,
        )
        val DARK = ChatIcons(
            icon = 0,
        )
    }
}

internal val LocalChatIcons = staticCompositionLocalOf<ChatIcons> {
    error("Chat Icons is not provided")
}

internal fun fetchChatIcons(isDark: Boolean) = when (isDark) {
    true -> ChatIcons.DARK
    false -> ChatIcons.LIGHT
}