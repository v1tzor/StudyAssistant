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

package ru.aleshin.studyassistant.core.data.mappers.ai

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.data.handlers.FunctionArgumentsHandler
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantFinishReason
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantResponse
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantResponseChoice
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChatUsage
import ru.aleshin.studyassistant.core.domain.entities.ai.FunctionResponse
import ru.aleshin.studyassistant.core.domain.entities.ai.ToolCall
import ru.aleshin.studyassistant.core.domain.entities.ai.ToolCallType
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionFinishReasonPojo
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionMessagePojo
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionMessageRolePojo
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionResponsePojo
import ru.aleshin.studyassistant.core.remote.models.ai.backend.AiCompletionToolCallPojo

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
fun AiAssistantMessage.mapToBackend(): AiCompletionMessagePojo? = when (this) {
    is AiAssistantMessage.SystemMessage -> null
    is AiAssistantMessage.UserMessage -> AiCompletionMessagePojo(
        role = AiCompletionMessageRolePojo.USER,
        content = content,
    )
    is AiAssistantMessage.AssistantMessage -> AiCompletionMessagePojo(
        role = AiCompletionMessageRolePojo.ASSISTANT,
        content = content,
        toolCalls = toolCalls.orEmpty().map { call ->
            AiCompletionToolCallPojo(
                id = call.id,
                name = call.function.name,
                arguments = FunctionArgumentsHandler.encode(call.function.arguments),
            )
        },
    )
    is AiAssistantMessage.ToolMessage -> AiCompletionMessagePojo(
        role = AiCompletionMessageRolePojo.TOOL,
        content = content,
        toolCallId = toolCallId,
    )
}

fun AiCompletionResponsePojo.mapToDomain(time: Instant): AiAssistantResponse {
    val message = AiAssistantMessage.AssistantMessage(
        id = randomUUID(),
        content = message.content,
        time = time,
        toolCalls = message.toolCalls.map { call ->
            ToolCall(
                id = call.id,
                type = ToolCallType.FUNCTION,
                function = FunctionResponse(
                    name = call.name,
                    arguments = FunctionArgumentsHandler.decode(call.arguments),
                ),
            )
        }.takeIf(List<ToolCall>::isNotEmpty),
    )
    val usage = usage

    return AiAssistantResponse(
        id = randomUUID(),
        choices = listOf(
            AiAssistantResponseChoice(
                index = 0,
                message = message,
                finishReason = when (finishReason) {
                    AiCompletionFinishReasonPojo.STOP -> AiAssistantFinishReason.STOP
                    AiCompletionFinishReasonPojo.LENGTH -> AiAssistantFinishReason.LENGTH
                    AiCompletionFinishReasonPojo.CONTENT_FILTER -> AiAssistantFinishReason.CONTENT_FILTER
                    AiCompletionFinishReasonPojo.TOOL_CALLS -> AiAssistantFinishReason.TOOL_CALLS
                    AiCompletionFinishReasonPojo.UNKNOWN -> null
                },
            ),
        ),
        created = time.toEpochMilliseconds() / MILLIS_IN_SECOND,
        model = BACKEND_MODEL,
        usage = AiChatUsage(
            promptTokens = usage?.promptTokens ?: 0,
            completionTokens = usage?.completionTokens ?: 0,
            totalTokens = usage?.totalTokens ?: 0,
        ),
    )
}

private const val MILLIS_IN_SECOND = 1_000L
private const val BACKEND_MODEL = "studyassistant-ai"
