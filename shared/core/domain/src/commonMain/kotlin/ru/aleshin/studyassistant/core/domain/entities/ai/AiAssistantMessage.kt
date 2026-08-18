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
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage.Type

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
sealed class AiAssistantMessage {

    abstract val id: String
    abstract val content: String?
    abstract val type: Type
    abstract val time: Instant

    data class UserMessage(
        override val id: String = randomUUID(),
        override val content: String?,
        override val time: Instant,
        val name: String? = null
    ) : AiAssistantMessage() {
        override val type = Type.USER
    }

    data class AssistantMessage(
        override val id: String,
        override val content: String?,
        override val time: Instant,
        val name: String? = null,
        val prefix: Boolean? = null,
        val reasoningContent: String? = null,
        val toolCalls: List<ToolCall>? = null,
    ) : AiAssistantMessage() {
        override val type = Type.ASSISTANT
    }

    data class SystemMessage(
        override val id: String = randomUUID(),
        override val content: String,
        override val time: Instant,
        val name: String? = null
    ) : AiAssistantMessage() {
        override val type = Type.SYSTEM
    }

    data class ToolMessage(
        override val id: String = randomUUID(),
        override val content: String,
        override val time: Instant,
        val toolCallId: String
    ) : AiAssistantMessage() {
        override val type = Type.TOOL_CALL
    }

    enum class Type {
        USER, ASSISTANT, SYSTEM, TOOL_CALL
    }
}

fun List<AiAssistantMessage>.filterNotTools() = filter {
    it.type == Type.USER || it.type == Type.ASSISTANT
}

fun List<AiAssistantMessage>.optimisedMessagesForSend(
    tokenBudget: Int = 6_000,
): List<AiAssistantMessage> {
    val messages = this.sortedBy { it.time }
    val systemMessages = messages.filterIsInstance<AiAssistantMessage.SystemMessage>()
    val conversationTurns = messages.filterNot { it is AiAssistantMessage.SystemMessage }
        .fold(mutableListOf<MutableList<AiAssistantMessage>>()) { turns, message ->
            if (message is AiAssistantMessage.UserMessage || turns.isEmpty()) {
                turns += mutableListOf(message)
            } else {
                turns.last() += message
            }
            turns
        }

    var remainingTokens = tokenBudget - systemMessages.sumOf(AiAssistantMessage::estimatedTokens)
    val selectedTurns = mutableListOf<List<AiAssistantMessage>>()
    for (turn in conversationTurns.asReversed()) {
        val turnTokens = turn.sumOf(AiAssistantMessage::estimatedTokens)
        if (selectedTurns.isNotEmpty() && turnTokens > remainingTokens) break
        selectedTurns += turn
        remainingTokens -= turnTokens
        if (remainingTokens <= 0) break
    }
    return systemMessages + selectedTurns.asReversed().flatten()
}

private fun AiAssistantMessage.estimatedTokens(): Int {
    val toolPayloadSize = (this as? AiAssistantMessage.AssistantMessage)?.toolCalls
        ?.sumOf { call -> call.function.arguments?.toString()?.length ?: 0 }
        ?: 0
    return ((content?.length ?: 0) + toolPayloadSize + 3) / 4 + 8
}

suspend fun List<AiAssistantMessage>.dropUnconfirmedMessages(
    onDrop: suspend (AiAssistantMessage) -> Unit,
): List<AiAssistantMessage> {
    val messages = this.sortedBy(AiAssistantMessage::time)
    val keepUntil = messages.lastConfirmedTurnEndIndex()
    return messages.mapIndexedNotNull { index, message ->
        val keep = keepUntil != null && index <= keepUntil ||
            message is AiAssistantMessage.SystemMessage
        if (!keep) {
            onDrop(message)
            null
        } else {
            message
        }
    }
}

private fun List<AiAssistantMessage>.lastConfirmedTurnEndIndex(): Int? {
    for (index in indices.reversed()) {
        val assistant = this[index] as? AiAssistantMessage.AssistantMessage ?: continue
        val calls = assistant.toolCalls.orEmpty()
        if (calls.isEmpty()) return index

        val callIds = calls.map(ToolCall::id).toSet()
        val laterToolIds = drop(index + 1)
            .filterIsInstance<AiAssistantMessage.ToolMessage>()
            .mapTo(mutableSetOf(), AiAssistantMessage.ToolMessage::toolCallId)
        if (callIds.any { callId -> callId !in laterToolIds }) continue

        return indices.drop(index + 1).lastOrNull { laterIndex ->
            val tool = this[laterIndex] as? AiAssistantMessage.ToolMessage
            tool != null && tool.toolCallId in callIds
        } ?: index
    }
    return null
}

/**
 * Iterates backward through the message list, dropping all non-system messages
 * until the last valid assistant message is found.
 *
 * Useful when needing to isolate the last successful AI response after a failed interaction.
 *
 * @param onDrop Callback triggered for each dropped message.
 * @return The last confirmed assistant message, or null if none is found.
 */
suspend fun List<AiAssistantMessage>.dropUntilConfirmedMessage(
    onDrop: suspend (AiAssistantMessage) -> Unit,
): AiAssistantMessage.AssistantMessage? {
    val messages = this.sortedBy { it.time }
    var lastMessage: AiAssistantMessage.AssistantMessage? = null

    for (i in messages.lastIndex downTo 0 step 1) {
        val message = messages[i]
        if (message is AiAssistantMessage.AssistantMessage) {
            lastMessage = message
            break
        } else if (message !is AiAssistantMessage.SystemMessage) {
            onDrop(message)
        }
    }

    return lastMessage
}
