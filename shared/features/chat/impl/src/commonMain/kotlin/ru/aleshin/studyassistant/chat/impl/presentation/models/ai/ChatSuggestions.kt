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

package ru.aleshin.studyassistant.chat.impl.presentation.models.ai

import androidx.compose.runtime.Composable
import kotlinx.serialization.Serializable
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.chat.impl.resources.Res
import ru.aleshin.studyassistant.chat.impl.resources.functional_chat_suggestion
import ru.aleshin.studyassistant.chat.impl.resources.homework_chat_suggestion

/**
 * @author Stanislav Aleshin on 22.06.2025.
 */
@Serializable
internal enum class ChatSuggestions : ChatSuggestion {
    FUNCTIONAL {
        override val content: String
            @Composable get() = stringResource(Res.string.functional_chat_suggestion)
    },
    HOMEWORK {
        override val content: String
            @Composable get() = stringResource(Res.string.homework_chat_suggestion)
    }
}
