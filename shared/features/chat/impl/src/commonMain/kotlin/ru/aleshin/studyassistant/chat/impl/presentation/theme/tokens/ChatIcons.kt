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

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import org.jetbrains.compose.resources.DrawableResource
import studyassistant.shared.features.chat.impl.generated.resources.Res
import studyassistant.shared.features.chat.impl.generated.resources.ic_ai_sender

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Immutable
internal data class ChatIcons(
    val aiAssistant: DrawableResource,
) {
    companion object Companion {
        val LIGHT = ChatIcons(
            aiAssistant = Res.drawable.ic_ai_sender,
        )
        val DARK = ChatIcons(
            aiAssistant = Res.drawable.ic_ai_sender,
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