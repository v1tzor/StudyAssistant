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

import kotlinx.coroutines.runBlocking
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

    @Test
    fun preparedHistory_uniquifiesDuplicateToolCallIds() {
        val user = AiAssistantMessage.UserMessage(
            id = "user",
            content = "next chemistry homework",
            time = instant(1),
        )
        val firstCall = AiAssistantMessage.AssistantMessage(
            id = "assistant-1",
            content = null,
            time = instant(2),
            toolCalls = listOf(
                ToolCall(
                    id = "call-1",
                    type = ToolCallType.FUNCTION,
                    function = FunctionResponse(name = "get_subjects", arguments = mapOf("organizationId" to "org")),
                ),
            ),
        )
        val firstTool = AiAssistantMessage.ToolMessage(
            id = "tool-1",
            content = "[]",
            time = instant(3),
            toolCallId = "call-1",
        )
        val secondCall = AiAssistantMessage.AssistantMessage(
            id = "assistant-2",
            content = null,
            time = instant(4),
            toolCalls = listOf(
                ToolCall(
                    id = "call-1",
                    type = ToolCallType.FUNCTION,
                    function = FunctionResponse(name = "get_near_class", arguments = mapOf("subjectId" to "subj")),
                ),
            ),
        )
        val secondTool = AiAssistantMessage.ToolMessage(
            id = "tool-2",
            content = "{}",
            time = instant(5),
            toolCallId = "call-1",
        )

        val prepared = listOf(user, firstCall, firstTool, secondCall, secondTool)
            .preparedMessagesForCompletion()

        val toolIds = prepared.filterIsInstance<AiAssistantMessage.AssistantMessage>()
            .flatMap { message -> message.toolCalls.orEmpty() }
            .map(ToolCall::id)
        assertEquals(toolIds.toSet().size, toolIds.size)

        val firstAssistant = prepared.filterIsInstance<AiAssistantMessage.AssistantMessage>().first()
        val secondAssistant = prepared.filterIsInstance<AiAssistantMessage.AssistantMessage>().last()
        val remappedTools = prepared.filterIsInstance<AiAssistantMessage.ToolMessage>()
        assertEquals(firstAssistant.toolCalls?.single()?.id, remappedTools[0].toolCallId)
        assertEquals(secondAssistant.toolCalls?.single()?.id, remappedTools[1].toolCallId)
    }

    @Test
    fun preparedHistory_uniquifiesDuplicateToolCallIdsInOneAssistantMessage() {
        val user = AiAssistantMessage.UserMessage(
            id = "user",
            content = "create homework for the next chemistry lesson",
            time = instant(1),
        )
        val parallelCalls = AiAssistantMessage.AssistantMessage(
            id = "assistant",
            content = null,
            time = instant(2),
            toolCalls = listOf(
                ToolCall(
                    id = "call_1",
                    type = ToolCallType.FUNCTION,
                    function = FunctionResponse(name = "get_organizations", arguments = emptyMap()),
                ),
                ToolCall(
                    id = "call_1",
                    type = ToolCallType.FUNCTION,
                    function = FunctionResponse(name = "get_subjects", arguments = emptyMap()),
                ),
            ),
        )
        val organizationsResult = AiAssistantMessage.ToolMessage(
            id = "tool-organizations",
            content = "[]",
            time = instant(3),
            toolCallId = "call_1",
        )
        val subjectsResult = AiAssistantMessage.ToolMessage(
            id = "tool-subjects",
            content = "[]",
            time = instant(4),
            toolCallId = "call_1",
        )

        val prepared = listOf(user, parallelCalls, organizationsResult, subjectsResult)
            .preparedMessagesForCompletion()

        val assistant = prepared.filterIsInstance<AiAssistantMessage.AssistantMessage>().single()
        val remappedCalls = assistant.toolCalls.orEmpty()
        val remappedTools = prepared.filterIsInstance<AiAssistantMessage.ToolMessage>()
        val remappedIds = remappedCalls.map(ToolCall::id)

        assertEquals(listOf("get_organizations", "get_subjects"), remappedCalls.map { it.function.name })
        assertEquals(remappedIds.toSet().size, remappedIds.size)
        assertEquals(2, remappedIds.size)
        assertEquals(remappedCalls[0].id, remappedTools[0].toolCallId)
        assertEquals(remappedCalls[1].id, remappedTools[1].toolCallId)
        assertEquals("call_1", remappedCalls[0].id)
        assertEquals("call_1-1", remappedCalls[1].id)
    }

    @Test
    fun preparedHistory_dropsEmptyAssistantMessages() {
        val user = AiAssistantMessage.UserMessage(
            id = "user",
            content = "hello",
            time = instant(1),
        )
        val emptyAssistant = AiAssistantMessage.AssistantMessage(
            id = "empty",
            content = null,
            time = instant(2),
        )
        val finalAssistant = AiAssistantMessage.AssistantMessage(
            id = "final",
            content = "hi",
            time = instant(3),
        )

        val prepared = listOf(user, emptyAssistant, finalAssistant).preparedMessagesForCompletion()

        assertEquals(listOf("user", "final"), prepared.map(AiAssistantMessage::id))
    }

    @Test
    fun dropUnconfirmedMessages_dropsContentAssistantWithUnresolvedTools() = runBlocking {
        val system = AiAssistantMessage.SystemMessage(
            id = "system",
            content = "system",
            time = instant(0),
        )
        val user = AiAssistantMessage.UserMessage(
            id = "user",
            content = "create a todo",
            time = instant(1),
        )
        val assistant = AiAssistantMessage.AssistantMessage(
            id = "assistant",
            content = "I will create it",
            time = instant(2),
            toolCalls = listOf(
                ToolCall(
                    id = "call-1",
                    type = ToolCallType.FUNCTION,
                    function = FunctionResponse(name = "create_todo", arguments = mapOf("name" to "Read")),
                ),
            ),
        )
        val dropped = mutableListOf<String>()

        val kept = listOf(system, user, assistant).dropUnconfirmedMessages { message ->
            dropped += message.id
        }

        assertEquals(listOf("system"), kept.map(AiAssistantMessage::id))
        assertEquals(listOf("user", "assistant"), dropped)
    }

    @Test
    fun dropUnconfirmedMessages_keepsResolvedToolTurn() = runBlocking {
        val user = AiAssistantMessage.UserMessage(
            id = "user",
            content = "classes",
            time = instant(1),
        )
        val assistant = AiAssistantMessage.AssistantMessage(
            id = "assistant",
            content = null,
            time = instant(2),
            toolCalls = listOf(
                ToolCall(
                    id = "call-1",
                    type = ToolCallType.FUNCTION,
                    function = FunctionResponse(name = "get_classes_by_date", arguments = emptyMap()),
                ),
            ),
        )
        val tool = AiAssistantMessage.ToolMessage(
            id = "tool",
            content = "[]",
            time = instant(3),
            toolCallId = "call-1",
        )

        val kept = listOf(user, assistant, tool).dropUnconfirmedMessages { }

        assertEquals(listOf("user", "assistant", "tool"), kept.map(AiAssistantMessage::id))
    }

    @Test
    fun dropUnconfirmedMessages_dropsDuplicateIdCallsWhenOneToolResultIsMissing() = runBlocking {
        val system = AiAssistantMessage.SystemMessage(
            id = "system",
            content = "system",
            time = instant(0),
        )
        val user = AiAssistantMessage.UserMessage(
            id = "user",
            content = "next chemistry homework",
            time = instant(1),
        )
        val assistant = AiAssistantMessage.AssistantMessage(
            id = "assistant",
            content = null,
            time = instant(2),
            toolCalls = listOf(
                ToolCall(
                    id = "call_1",
                    type = ToolCallType.FUNCTION,
                    function = FunctionResponse(name = "get_organizations", arguments = emptyMap()),
                ),
                ToolCall(
                    id = "call_1",
                    type = ToolCallType.FUNCTION,
                    function = FunctionResponse(name = "get_subjects", arguments = emptyMap()),
                ),
            ),
        )
        val onlyOneResult = AiAssistantMessage.ToolMessage(
            id = "tool",
            content = "[]",
            time = instant(3),
            toolCallId = "call_1",
        )
        val dropped = mutableListOf<String>()

        val kept = listOf(system, user, assistant, onlyOneResult).dropUnconfirmedMessages { message ->
            dropped += message.id
        }

        assertEquals(listOf("system"), kept.map(AiAssistantMessage::id))
        assertEquals(listOf("user", "assistant", "tool"), dropped)
    }

    @Test
    fun dropUnconfirmedMessages_keepsDuplicateIdCallsWhenEachHasAToolResult() = runBlocking {
        val user = AiAssistantMessage.UserMessage(
            id = "user",
            content = "next chemistry homework",
            time = instant(1),
        )
        val assistant = AiAssistantMessage.AssistantMessage(
            id = "assistant",
            content = null,
            time = instant(2),
            toolCalls = listOf(
                ToolCall(
                    id = "call_1",
                    type = ToolCallType.FUNCTION,
                    function = FunctionResponse(name = "get_organizations", arguments = emptyMap()),
                ),
                ToolCall(
                    id = "call_1",
                    type = ToolCallType.FUNCTION,
                    function = FunctionResponse(name = "get_subjects", arguments = emptyMap()),
                ),
            ),
        )
        val organizationsResult = AiAssistantMessage.ToolMessage(
            id = "tool-organizations",
            content = "[]",
            time = instant(3),
            toolCallId = "call_1",
        )
        val subjectsResult = AiAssistantMessage.ToolMessage(
            id = "tool-subjects",
            content = "[]",
            time = instant(4),
            toolCallId = "call_1",
        )

        val kept = listOf(user, assistant, organizationsResult, subjectsResult)
            .dropUnconfirmedMessages { }

        assertEquals(
            listOf("user", "assistant", "tool-organizations", "tool-subjects"),
            kept.map(AiAssistantMessage::id),
        )
    }

    private fun instant(epochSeconds: Long) = Instant.fromEpochSeconds(epochSeconds)
}
