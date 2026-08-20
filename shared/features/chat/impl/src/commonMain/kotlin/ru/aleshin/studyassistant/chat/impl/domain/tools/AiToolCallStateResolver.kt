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

package ru.aleshin.studyassistant.chat.impl.domain.tools

import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.ToolCall

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
internal interface AiToolCallStateResolver {

    fun activeCalls(messages: List<AiAssistantMessage>): List<ToolCall>

    class Base : AiToolCallStateResolver {

        override fun activeCalls(messages: List<AiAssistantMessage>): List<ToolCall> {
            val ordered = messages.sortedBy(AiAssistantMessage::time)

            for (index in ordered.indices.reversed()) {
                val assistant = ordered[index] as? AiAssistantMessage.AssistantMessage ?: continue
                val calls = assistant.toolCalls.orEmpty()
                if (calls.isEmpty()) continue

                val remaining = calls.toMutableList()
                for (later in ordered.drop(index + 1)) {
                    val tool = later as? AiAssistantMessage.ToolMessage ?: continue
                    val matchIndex = remaining.indexOfFirst { call -> call.id == tool.toolCallId }
                    if (matchIndex >= 0) remaining.removeAt(matchIndex)
                }
                if (remaining.isNotEmpty()) return remaining
            }
            return emptyList()
        }
    }
}
