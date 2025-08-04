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

package ru.aleshin.studyassistant.chat.impl.presentation.mappers

import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.AiChatHistoryUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.AssistantMessageUi
import ru.aleshin.studyassistant.chat.impl.presentation.models.ai.UserMessageUi
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChatHistory

/**
 * @author Stanislav Aleshin on 22.06.2025.
 */
internal suspend fun AiChatHistory.mapToUi() = AiChatHistoryUi(
    uid = uid,
    messages = messages.map { it.mapToUi() },
    lastMessage = lastMessage?.mapToUi()
)

internal suspend fun AiAssistantMessage.mapToUi() = when (this) {
    is AiAssistantMessage.UserMessage -> UserMessageUi(
        id = id,
        content = content,
        time = time,
        name = name,
    )
    is AiAssistantMessage.AssistantMessage -> {
        AssistantMessageUi(
            id = id,
            content = content,
            time = time,
        )
    }
    else -> throw IllegalArgumentException("Not supported message type $this")
}