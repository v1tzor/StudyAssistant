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

package ru.aleshin.studyassistant.core.domain.entities.ai

import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
class AiAssistantMessageTest {

    @Test
    fun optimizedHistory_keepsLatestToolChainInChronologicalOrder() {
        val system = AiAssistantMessage.SystemMessage(
            id = "system",
            content = "system",
            time = instant(0),
        )
        val oldUser = AiAssistantMessage.UserMessage(
            id = "old-user",
            content = "u".repeat(200),
            time = instant(1),
        )
        val oldAssistant = AiAssistantMessage.AssistantMessage(
            id = "old-assistant",
            content = "a".repeat(200),
            time = instant(2),
        )
        val newUser = AiAssistantMessage.UserMessage(
            id = "new-user",
            content = "new",
            time = instant(3),
        )
        val toolCall = AiAssistantMessage.AssistantMessage(
            id = "tool-call",
            content = null,
            time = instant(4),
            toolCalls = listOf(
                ToolCall(
                    id = "call-id",
                    type = ToolCallType.FUNCTION,
                    function = FunctionResponse(
                        name = "get_classes_by_date",
                        arguments = mapOf("date" to "2026-08-04"),
                    ),
                )
            ),
        )
        val toolResult = AiAssistantMessage.ToolMessage(
            id = "tool-result",
            content = "[]",
            time = instant(5),
            toolCallId = "call-id",
        )
        val finalAssistant = AiAssistantMessage.AssistantMessage(
            id = "final-assistant",
            content = "done",
            time = instant(6),
        )

        val optimized = listOf(
            finalAssistant,
            oldAssistant,
            toolResult,
            system,
            newUser,
            oldUser,
            toolCall,
        ).optimisedMessagesForSend(tokenBudget = 50)

        assertEquals(
            listOf("system", "new-user", "tool-call", "tool-result", "final-assistant"),
            optimized.map(AiAssistantMessage::id),
        )
    }

    private fun instant(epochSeconds: Long) = Instant.fromEpochSeconds(epochSeconds)
}
