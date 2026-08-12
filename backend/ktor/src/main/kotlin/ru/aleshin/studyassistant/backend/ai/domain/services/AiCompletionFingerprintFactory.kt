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

package ru.aleshin.studyassistant.backend.ai.domain.services

import ru.aleshin.studyassistant.backend.ai.domain.model.AiCompletionCommand
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessage
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessageRole

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class AiCompletionFingerprintFactory {

    fun conversation(command: AiCompletionCommand): String {
        val lastUserIndex = command.messages.indexOfLast { message ->
            message.role == AiMessageRole.USER
        }
        return fingerprint(
            command = command,
            messages = command.messages.take(lastUserIndex + 1),
        )
    }

    fun execution(command: AiCompletionCommand): String {
        return fingerprint(
            command = command,
            messages = command.messages,
        )
    }

    private fun fingerprint(
        command: AiCompletionCommand,
        messages: List<AiMessage>,
    ): String = buildString {
        appendSegment(FINGERPRINT_VERSION)
        appendSegment(command.locale)
        appendSegment(command.timeZone)
        append(command.toolNames.size).append(FIELD_SEPARATOR)
        command.toolNames.forEach { toolName -> appendSegment(toolName) }
        append(messages.size).append(FIELD_SEPARATOR)
        messages.forEach { message ->
            appendSegment(message.role.name)
            appendNullableSegment(message.content)
            appendNullableSegment(message.toolCallId)
            append(message.toolCalls.size).append(FIELD_SEPARATOR)
            message.toolCalls.forEach { toolCall ->
                appendSegment(toolCall.id)
                appendSegment(toolCall.name)
                appendSegment(toolCall.arguments)
            }
        }
    }

    private fun StringBuilder.appendNullableSegment(value: String?) {
        if (value == null) {
            append(NULL_SEGMENT)
        } else {
            appendSegment(value)
        }
    }

    private fun StringBuilder.appendSegment(value: String) {
        append(value.length)
        append(LENGTH_SEPARATOR)
        append(value)
        append(FIELD_SEPARATOR)
    }

    private companion object {

        const val FINGERPRINT_VERSION = "chat-v2"
        const val NULL_SEGMENT = "-1;"
        const val LENGTH_SEPARATOR = ':'
        const val FIELD_SEPARATOR = ';'
    }
}
