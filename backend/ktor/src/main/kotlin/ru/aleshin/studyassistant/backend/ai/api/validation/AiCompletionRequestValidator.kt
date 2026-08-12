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

package ru.aleshin.studyassistant.backend.ai.api.validation

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import ru.aleshin.studyassistant.backend.ai.AiConfig
import ru.aleshin.studyassistant.backend.ai.api.dto.AiCompletionRequestDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiMessageDto
import ru.aleshin.studyassistant.backend.ai.api.dto.AiMessageRoleDto
import ru.aleshin.studyassistant.backend.ai.domain.tools.AiToolCatalog
import ru.aleshin.studyassistant.backend.common.api.InvalidRequestException
import java.time.ZoneId
import java.util.UUID

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class AiCompletionRequestValidator(
    private val config: AiConfig,
    private val json: Json,
    private val toolCatalog: AiToolCatalog,
) {

    fun validate(request: AiCompletionRequestDto) {
        if (runCatching { UUID.fromString(request.messageId) }.isFailure) {
            throw InvalidRequestException()
        }
        if (request.messages.isEmpty() || request.messages.size > config.maxMessages) {
            throw InvalidRequestException()
        }
        if (
            request.toolProtocolVersion != TOOL_PROTOCOL_VERSION ||
            !LOCALE_PATTERN.matches(request.locale) ||
            runCatching { ZoneId.of(request.timeZone) }.isFailure ||
            request.toolNames.size > config.maxTools
        ) {
            throw InvalidRequestException()
        }

        if (
            request.toolNames.distinct().size != request.toolNames.size ||
            request.toolNames.any { name -> !TOOL_NAME_PATTERN.matches(name) } ||
            toolCatalog.resolve(names = request.toolNames) == null
        ) {
            throw InvalidRequestException()
        }

        val pendingToolCalls = mutableSetOf<String>()
        val seenToolCallIds = mutableSetOf<String>()
        var hasUserMessage = false
        var totalContentCharacters = 0

        request.messages.forEachIndexed { index, message ->
            if (pendingToolCalls.isNotEmpty() && message.role != AiMessageRoleDto.TOOL) {
                throw InvalidRequestException()
            }

            totalContentCharacters += message.content?.length ?: 0

            when (message.role) {
                AiMessageRoleDto.SYSTEM -> {
                    throw InvalidRequestException()
                }

                AiMessageRoleDto.USER -> {
                    hasUserMessage = true
                    validateContentMessage(message = message)
                }

                AiMessageRoleDto.ASSISTANT -> {
                    validateAssistantMessage(
                        message = message,
                        toolNames = request.toolNames,
                        pendingToolCalls = pendingToolCalls,
                        seenToolCallIds = seenToolCallIds,
                    )
                    totalContentCharacters += message.toolCalls.sumOf { toolCall ->
                        toolCall.id.length + toolCall.name.length + toolCall.arguments.length
                    }
                }

                AiMessageRoleDto.TOOL -> validateToolMessage(
                    message = message,
                    pendingToolCalls = pendingToolCalls,
                )
            }

            if (totalContentCharacters > config.maxTotalContentCharacters) {
                throw InvalidRequestException()
            }
        }

        if (
            !hasUserMessage ||
            pendingToolCalls.isNotEmpty() ||
            request.messages.last().role !in FINAL_MESSAGE_ROLES
        ) {
            throw InvalidRequestException()
        }
    }

    private fun validateContentMessage(message: AiMessageDto) {
        if (
            message.content.isNullOrBlank() ||
            message.content.length > config.maxMessageCharacters ||
            message.toolCalls.isNotEmpty() ||
            message.toolCallId != null
        ) {
            throw InvalidRequestException()
        }
    }

    private fun validateAssistantMessage(
        message: AiMessageDto,
        toolNames: List<String>,
        pendingToolCalls: MutableSet<String>,
        seenToolCallIds: MutableSet<String>,
    ) {
        if (
            message.toolCallId != null ||
            message.content != null && message.content.length > config.maxMessageCharacters ||
            message.content.isNullOrBlank() && message.toolCalls.isEmpty() ||
            message.toolCalls.size > config.maxToolCallsPerMessage
        ) {
            throw InvalidRequestException()
        }

        message.toolCalls.forEach { toolCall ->
            val validArguments = runCatching {
                json.parseToJsonElement(toolCall.arguments) is JsonObject
            }.getOrDefault(false)

            if (
                toolCall.id.isBlank() ||
                toolCall.id.length > MAX_TOOL_CALL_ID_CHARACTERS ||
                toolCall.name !in toolNames ||
                toolCall.arguments.length > config.maxToolArgumentsCharacters ||
                !validArguments ||
                !seenToolCallIds.add(toolCall.id) ||
                !pendingToolCalls.add(toolCall.id)
            ) {
                throw InvalidRequestException()
            }
        }
    }

    private fun validateToolMessage(
        message: AiMessageDto,
        pendingToolCalls: MutableSet<String>,
    ) {
        val toolCallId = message.toolCallId

        if (
            message.content.isNullOrBlank() ||
            message.content.length > config.maxMessageCharacters ||
            message.toolCalls.isNotEmpty() ||
            toolCallId.isNullOrBlank() ||
            toolCallId.length > MAX_TOOL_CALL_ID_CHARACTERS ||
            !pendingToolCalls.remove(toolCallId)
        ) {
            throw InvalidRequestException()
        }
    }

    private companion object {

        const val TOOL_PROTOCOL_VERSION = 1
        const val MAX_TOOL_CALL_ID_CHARACTERS = 128

        val TOOL_NAME_PATTERN = Regex("^[A-Za-z0-9_-]{1,64}$")
        val LOCALE_PATTERN = Regex("^[A-Za-z]{2,3}(?:-[A-Za-z0-9]{2,8})*$")

        val FINAL_MESSAGE_ROLES = setOf(
            AiMessageRoleDto.USER,
            AiMessageRoleDto.TOOL,
        )

    }
}
