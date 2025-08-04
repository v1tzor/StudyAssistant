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

/**
 * Optimizes a message history for sending to the AI assistant.
 *
 * If the total number of messages exceeds [defaultMaxMessages], this function returns:
 * - The last [defaultMaxMessages] messages,
 * - Any preceding system messages,
 * - One additional user message (if available), to provide context for the assistant.
 *
 * This ensures that the assistant receives the most recent messages,
 * necessary system context, and a triggering user prompt.
 *
 * @param defaultMaxMessages The number of recent messages to retain (default is 15).
 * @return A list of messages optimized for assistant input.
 *
 * @author Stanislav Aleshin on 21.06.2025.
 */
fun List<AiAssistantMessage>.optimisedMessagesForSend(
    defaultMaxMessages: Int = 15
): List<AiAssistantMessage> {
    val messages = this.sortedBy { it.time }
    return if (size <= 15) {
        messages
    } else {
        val lastMessages = takeLast(defaultMaxMessages)
        val firstLastMessageIndex = size - defaultMaxMessages
        val requiredMessages = buildList {
            var isUserMessageAdded = false
            for (i in (firstLastMessageIndex - 1) downTo 0 step 1) {
                if (messages[i] is AiAssistantMessage.SystemMessage) {
                    add(messages[i])
                } else if (!isUserMessageAdded) {
                    add(messages[i])
                    if (messages[i] is AiAssistantMessage.UserMessage) isUserMessageAdded = true
                }
            }
        }
        return requiredMessages.reversed() + lastMessages
    }
}

/**
 * Drops all unconfirmed messages at the end of the message list,
 * up to and including the most recent confirmed assistant response.
 *
 * This is useful when recovering from a failed or interrupted request (e.g., due to network loss),
 * so that incomplete or invalid messages are discarded and the chat is rolled back
 * to the last known good assistant message.
 *
 * @param onDrop Callback triggered for each dropped message.
 * @return A cleaned list of confirmed messages.
 *
 * @author Stanislav Aleshin on 21.06.2025.
 */
suspend fun List<AiAssistantMessage>.dropUnconfirmedMessages(
    onDrop: suspend (AiAssistantMessage) -> Unit,
): List<AiAssistantMessage> {
    val messages = this.sortedBy { it.time }
    if (size < 2) return messages
    val confirmedMessages = buildList {
        var isCleared = false
        for (i in messages.lastIndex downTo 0 step 1) {
            if (!isCleared) {
                if (
                    messages[i] is AiAssistantMessage.AssistantMessage &&
                    !messages[i].content.isNullOrEmpty()
                ) {
                    isCleared = true
                    add(messages[i])
                } else if (messages[i] is AiAssistantMessage.SystemMessage) {
                    add(messages[i])
                } else {
                    onDrop(messages[i])
                }
            } else {
                add(messages[i])
            }
        }
    }
    return confirmedMessages.reversed()
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