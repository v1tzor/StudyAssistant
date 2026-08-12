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

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.FunctionResponse
import ru.aleshin.studyassistant.core.domain.entities.ai.ToolCall
import ru.aleshin.studyassistant.core.domain.entities.ai.ToolCallType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AiToolCallStateResolverTest {

    private val resolver = AiToolCallStateResolver.Base()

    @Test
    fun `returns every unresolved call from latest tool batch`() {
        val createCall = toolCall("create", "create_todo")
        val readCall = toolCall("read", "get_organizations")
        val messages = listOf(
            assistantMessage(createCall, readCall),
            toolMessage(callId = "read", timestamp = 2L),
        )

        val result = resolver.activeCalls(messages)

        assertEquals(listOf(createCall), result)
    }

    @Test
    fun `returns empty list when latest batch is fully resolved`() {
        val createCall = toolCall("create", "create_todo")
        val messages = listOf(
            assistantMessage(createCall),
            toolMessage(callId = "create", timestamp = 2L),
        )

        assertTrue(resolver.activeCalls(messages).isEmpty())
    }

    @Test
    fun `does not revive an older resolved batch`() {
        val oldCall = toolCall("old", "create_todo")
        val currentCall = toolCall("current", "create_homework")
        val messages = listOf(
            assistantMessage(oldCall),
            toolMessage(callId = "old", timestamp = 2L),
            assistantMessage(currentCall, timestamp = 3L),
        )

        assertEquals(listOf(currentCall), resolver.activeCalls(messages))
    }

    private fun assistantMessage(
        vararg calls: ToolCall,
        timestamp: Long = 1L,
    ) = AiAssistantMessage.AssistantMessage(
        id = "assistant-$timestamp",
        content = null,
        time = Instant.fromEpochMilliseconds(timestamp),
        toolCalls = calls.toList(),
    )

    private fun toolMessage(
        callId: String,
        timestamp: Long,
    ) = AiAssistantMessage.ToolMessage(
        id = "tool-$callId",
        content = "{}",
        time = Instant.fromEpochMilliseconds(timestamp),
        toolCallId = callId,
    )

    private fun toolCall(id: String, name: String) = ToolCall(
        id = id,
        type = ToolCallType.FUNCTION,
        function = FunctionResponse(name = name, arguments = emptyMap()),
    )
}
