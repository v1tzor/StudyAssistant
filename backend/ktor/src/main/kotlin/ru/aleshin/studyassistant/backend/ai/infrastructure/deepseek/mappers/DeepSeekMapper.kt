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

package ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.mappers

import kotlinx.serialization.json.JsonObject
import ru.aleshin.studyassistant.backend.ai.AiConfig
import ru.aleshin.studyassistant.backend.ai.domain.model.AiCompletion
import ru.aleshin.studyassistant.backend.ai.domain.model.AiCompletionRequest
import ru.aleshin.studyassistant.backend.ai.domain.model.AiFinishReason
import ru.aleshin.studyassistant.backend.ai.domain.model.AiMessageRole
import ru.aleshin.studyassistant.backend.ai.domain.model.AiResponseFormat
import ru.aleshin.studyassistant.backend.ai.domain.model.AiTokenUsage
import ru.aleshin.studyassistant.backend.ai.domain.model.AiToolCall
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.DeepSeekConfig
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.DeepSeekJson
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekChatRequestDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekChatResponseDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekFunctionDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekMessageDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekResponseFormatDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekThinkingDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekToolCallDto
import ru.aleshin.studyassistant.backend.ai.infrastructure.deepseek.dto.DeepSeekToolDto

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
class DeepSeekMapper(
    private val deepSeekConfig: DeepSeekConfig,
    private val aiConfig: AiConfig,
) {

    fun mapRequest(request: AiCompletionRequest): DeepSeekChatRequestDto {
        return DeepSeekChatRequestDto(
            model = DeepSeekConfig.MODEL,
            messages = request.messages.map { message ->
                DeepSeekMessageDto(
                    role = when (message.role) {
                        AiMessageRole.SYSTEM -> "system"
                        AiMessageRole.USER -> "user"
                        AiMessageRole.ASSISTANT -> "assistant"
                        AiMessageRole.TOOL -> "tool"
                    },
                    content = when (message.role) {
                        AiMessageRole.ASSISTANT -> message.content.orEmpty()
                        else -> message.content
                    },
                    toolCalls = message.toolCalls
                        .map { toolCall ->
                            DeepSeekToolCallDto(
                                id = toolCall.id,
                                function = DeepSeekFunctionDto(
                                    name = toolCall.name,
                                    arguments = toolCall.arguments,
                                ),
                            )
                        }
                        .takeIf(List<DeepSeekToolCallDto>::isNotEmpty),
                    toolCallId = message.toolCallId,
                )
            },
            thinking = DeepSeekThinkingDto(type = "disabled"),
            tools = request.tools
                .map { tool ->
                    DeepSeekToolDto(
                        function = DeepSeekFunctionDto(
                            name = tool.name,
                            description = tool.description,
                            parameters = tool.parameters,
                        ),
                    )
                }
                .takeIf(List<DeepSeekToolDto>::isNotEmpty),
            toolChoice = AUTO_TOOL_CHOICE.takeIf { request.tools.isNotEmpty() },
            responseFormat = when (request.responseFormat) {
                AiResponseFormat.TEXT -> null
                AiResponseFormat.JSON_OBJECT -> DeepSeekResponseFormatDto(type = JSON_OBJECT_RESPONSE_FORMAT)
            },
            temperature = request.temperature,
            maxTokens = deepSeekConfig.maxTokens,
            stream = false,
        )
    }

    fun mapResponse(
        response: DeepSeekChatResponseDto,
        request: AiCompletionRequest,
    ): AiCompletion? {
        val choice = response.choices.singleOrNull() ?: return null
        val message = choice.message
        val allowedToolNames = request.tools
            .mapTo(mutableSetOf()) { tool -> tool.name }
        val toolCallIds = request.messages
            .flatMapTo(mutableSetOf()) { message -> message.toolCalls.map(AiToolCall::id) }

        if (message.role != ASSISTANT_ROLE) {
            return null
        }

        if (
            message.content != null && message.content.length > aiConfig.maxMessageCharacters ||
            message.toolCalls.orEmpty().size > aiConfig.maxToolCallsPerMessage
        ) {
            return null
        }

        val toolCalls = message.toolCalls
            .orEmpty()
            .map { toolCall ->
                val arguments = toolCall.function.arguments ?: return null
                val validArguments = runCatching {
                    DeepSeekJson.parseToJsonElement(arguments) is JsonObject
                }.getOrDefault(false)
                if (
                    toolCall.id.isBlank() ||
                    toolCall.id.length > MAX_TOOL_CALL_ID_CHARACTERS ||
                    !toolCallIds.add(toolCall.id) ||
                    toolCall.type != FUNCTION_TYPE ||
                    toolCall.function.name !in allowedToolNames ||
                    arguments.length > aiConfig.maxToolArgumentsCharacters ||
                    !validArguments
                ) {
                    return null
                }
                AiToolCall(
                    id = toolCall.id,
                    name = toolCall.function.name,
                    arguments = arguments,
                )
            }

        val finishReason = when (choice.finishReason) {
            "stop" -> AiFinishReason.STOP
            "length" -> AiFinishReason.LENGTH
            "content_filter" -> AiFinishReason.CONTENT_FILTER
            "tool_calls" -> AiFinishReason.TOOL_CALLS
            else -> AiFinishReason.UNKNOWN
        }

        if (
            finishReason == AiFinishReason.TOOL_CALLS && toolCalls.isEmpty() ||
            finishReason != AiFinishReason.TOOL_CALLS && toolCalls.isNotEmpty() ||
            message.content.isNullOrBlank() && toolCalls.isEmpty()
        ) {
            return null
        }

        val usage = response.usage?.let { usage ->
            if (
                usage.promptTokens < 0 ||
                usage.completionTokens < 0 ||
                usage.totalTokens < 0
            ) {
                return null
            }
            AiTokenUsage(
                promptTokens = usage.promptTokens,
                completionTokens = usage.completionTokens,
                totalTokens = usage.totalTokens,
            )
        }

        return AiCompletion(
            content = message.content,
            toolCalls = toolCalls,
            finishReason = finishReason,
            usage = usage,
        )
    }

    private companion object {

        const val ASSISTANT_ROLE = "assistant"
        const val FUNCTION_TYPE = "function"
        const val AUTO_TOOL_CHOICE = "auto"
        const val JSON_OBJECT_RESPONSE_FORMAT = "json_object"
        const val MAX_TOOL_CALL_ID_CHARACTERS = 128
    }
}
