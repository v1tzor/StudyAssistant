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

    fun estimatedTokens(): Int {
        return (estimatedCharacters() + 3) / 4 + 8
    }

    fun estimatedCharacters(): Int {
        val toolPayloadSize = (this as? AssistantMessage)?.toolCalls?.sumOf { call ->
            call.id.length + call.function.name.length + (call.function.arguments?.toString()?.length ?: 0)
        } ?: 0
        return (content?.length ?: 0) + toolPayloadSize
    }

    enum class Type {
        USER, ASSISTANT, SYSTEM, TOOL_CALL
    }
}

fun List<AiAssistantMessage>.filterNotTools() = filter {
    it.type == Type.USER || it.type == Type.ASSISTANT
}

fun List<AiAssistantMessage>.optimisedMessagesForSend(
    tokenBudget: Int = 30_000,
): List<AiAssistantMessage> {
    val messages = this.sortedBy { it.time }
    val systemMessages = messages.filterIsInstance<AiAssistantMessage.SystemMessage>()
    val conversationTurns = messages.conversationTurns()

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

fun List<AiAssistantMessage>.preparedMessagesForCompletion(
    tokenBudget: Int = 25_000,
    maxMessages: Int = 80,
    maxTotalCharacters: Int = 60_000,
): List<AiAssistantMessage> {
    val uniquified = uniquifyToolCallIds()
    val withoutEmptyAssistants = uniquified.filterNot { message ->
        message is AiAssistantMessage.AssistantMessage &&
            message.content.isNullOrBlank() &&
            message.toolCalls.isNullOrEmpty()
    }
    val optimized = withoutEmptyAssistants.optimisedMessagesForSend(tokenBudget = tokenBudget)
    val systemMessages = optimized.filterIsInstance<AiAssistantMessage.SystemMessage>()
    val conversationTurns = optimized.filterNot { message ->
        message is AiAssistantMessage.SystemMessage
    }.conversationTurns()

    val selectedTurns = mutableListOf<List<AiAssistantMessage>>()
    var remainingCharacters = maxTotalCharacters - systemMessages.sumOf { message -> message.content.length }
    var remainingMessages = maxMessages - systemMessages.size
    for (turn in conversationTurns.asReversed()) {
        val turnCharacters = turn.sumOf(AiAssistantMessage::estimatedCharacters)
        if (
            selectedTurns.isNotEmpty() &&
            (turn.size > remainingMessages || turnCharacters > remainingCharacters)
        ) {
            break
        }
        selectedTurns += turn
        remainingCharacters -= turnCharacters
        remainingMessages -= turn.size
        if (remainingCharacters <= 0 || remainingMessages <= 0) break
    }
    return systemMessages + selectedTurns.asReversed().flatten()
}

internal fun List<AiAssistantMessage>.uniquifyToolCallIds(): List<AiAssistantMessage> {
    val messages = sortedBy(AiAssistantMessage::time)
    val seenIds = mutableSetOf<String>()
    val pendingByOriginalId = mutableMapOf<String, ArrayDeque<String>>()

    return messages.map { message ->
        when (message) {
            is AiAssistantMessage.AssistantMessage -> {
                pendingByOriginalId.clear()
                val uniqueCalls = message.toolCalls.orEmpty().mapIndexed { index, call ->
                    val uniqueId = if (seenIds.add(call.id)) {
                        call.id
                    } else {
                        // Use message ID + index to ensure determinism regardless of history truncation
                        val stableId = "${message.id}-$index"
                        seenIds.add(stableId)
                        stableId
                    }
                    pendingByOriginalId.getOrPut(call.id) { ArrayDeque() }.addLast(uniqueId)
                    call.copy(id = uniqueId)
                }
                message.copy(toolCalls = uniqueCalls.takeIf(List<ToolCall>::isNotEmpty))
            }
            is AiAssistantMessage.ToolMessage -> {
                val remappedId = pendingByOriginalId[message.toolCallId]
                    ?.removeFirstOrNull()
                    ?: message.toolCallId
                message.copy(toolCallId = remappedId)
            }
            else -> message
        }
    }
}

private fun List<AiAssistantMessage>.conversationTurns(): List<List<AiAssistantMessage>> {
    return fold(mutableListOf<MutableList<AiAssistantMessage>>()) { turns, message ->
        if (message is AiAssistantMessage.UserMessage || turns.isEmpty()) {
            turns += mutableListOf(message)
        } else {
            turns.last() += message
        }
        turns
    }
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

        val remainingCallIds = calls.map(ToolCall::id).toMutableList()
        var lastConsumedIndex: Int? = null
        for (laterIndex in (index + 1)..lastIndex) {
            when (val later = this[laterIndex]) {
                is AiAssistantMessage.UserMessage,
                is AiAssistantMessage.AssistantMessage -> break
                is AiAssistantMessage.ToolMessage -> {
                    val matchIndex = remainingCallIds.indexOf(later.toolCallId)
                    if (matchIndex >= 0) {
                        remainingCallIds.removeAt(matchIndex)
                        lastConsumedIndex = laterIndex
                    }
                }
                is AiAssistantMessage.SystemMessage -> Unit
            }
            if (remainingCallIds.isEmpty()) break
        }
        if (remainingCallIds.isNotEmpty()) continue

        return lastConsumedIndex ?: index
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
