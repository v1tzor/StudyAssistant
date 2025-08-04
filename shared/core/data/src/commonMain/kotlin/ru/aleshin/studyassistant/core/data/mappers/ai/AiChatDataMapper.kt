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

package ru.aleshin.studyassistant.core.data.mappers.ai

import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.database.models.ai.AiChatEntity
import ru.aleshin.studyassistant.core.database.models.ai.AiChatHistoryEntityDetails
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantFinishReason
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantResponse
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantResponseChoice
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChat
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChatHistory
import ru.aleshin.studyassistant.core.domain.entities.ai.AiChatUsage
import ru.aleshin.studyassistant.core.domain.entities.ai.FunctionResponse
import ru.aleshin.studyassistant.core.domain.entities.ai.ToolCall
import ru.aleshin.studyassistant.core.domain.entities.ai.ToolCallType
import ru.aleshin.studyassistant.core.remote.models.ai.AssistantMessagePojo
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionChoicePojo
import ru.aleshin.studyassistant.core.remote.models.ai.ChatCompletionResponsePojo
import ru.aleshin.studyassistant.core.remote.models.ai.ChatMessagePojo
import ru.aleshin.studyassistant.core.remote.models.ai.ChatUsagePojo
import ru.aleshin.studyassistant.core.remote.models.ai.FunctionResponsePojo
import ru.aleshin.studyassistant.core.remote.models.ai.SystemMessagePojo
import ru.aleshin.studyassistant.core.remote.models.ai.ToolCallPojo
import ru.aleshin.studyassistant.core.remote.models.ai.ToolCallTypePojo
import ru.aleshin.studyassistant.core.remote.models.ai.ToolMessagePojo
import ru.aleshin.studyassistant.core.remote.models.ai.UserMessagePojo
import ru.aleshin.studyassistant.sqldelight.ai.AiChatMessageEntity

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
fun AiChatEntity.mapToDomain() = AiChat(
    uid = uid,
    lastMessage = lastMessage?.mapToDomain(),
)

fun AiChatHistoryEntityDetails.mapToDomain() = AiChatHistory(
    uid = uid,
    messages = messages.map { it.mapToDomain() },
    lastMessage = lastMessage?.mapToDomain(),
)

fun AiChatHistory.mapToLocal() = AiChatHistoryEntityDetails(
    uid = uid,
    messages = messages.map { it.mapToLocal(uid) },
    lastMessage = lastMessage?.mapToLocal(uid),
)

fun AiAssistantMessage.mapToRemote() = when (this) {
    is AiAssistantMessage.UserMessage -> UserMessagePojo(
        content = content,
        name = name,
    )
    is AiAssistantMessage.AssistantMessage -> AssistantMessagePojo(
        content = content,
        name = name,
        prefix = prefix,
        reasoningContent = reasoningContent,
        toolCalls = toolCalls?.map { it.mapToRemote() },
    )
    is AiAssistantMessage.SystemMessage -> SystemMessagePojo(
        content = content,
        name = name,
    )
    is AiAssistantMessage.ToolMessage -> ToolMessagePojo(
        content = content,
        toolCallId = toolCallId,
    )
}

fun AiAssistantMessage.mapToLocal(chatId: String) = when (this) {
    is AiAssistantMessage.UserMessage -> AiChatMessageEntity(
        uid = id,
        chat_id = chatId,
        type = type.toString(),
        name = name,
        content = content,
        prefix = null,
        reasoning_content = null,
        tool_calls = null,
        tool_call_id = null,
        time = time.toEpochMilliseconds(),
    )
    is AiAssistantMessage.AssistantMessage -> AiChatMessageEntity(
        uid = id,
        chat_id = chatId,
        type = type.toString(),
        name = name,
        content = content,
        prefix = prefix?.let { if (it) 1L else 0L },
        reasoning_content = reasoningContent,
        tool_calls = toolCalls?.map { Json.encodeToString(it.mapToRemote()) },
        tool_call_id = null,
        time = time.toEpochMilliseconds(),
    )
    is AiAssistantMessage.SystemMessage -> AiChatMessageEntity(
        uid = id,
        chat_id = chatId,
        type = type.toString(),
        name = name,
        content = content,
        prefix = null,
        reasoning_content = null,
        tool_calls = null,
        tool_call_id = null,
        time = time.toEpochMilliseconds(),
    )
    is AiAssistantMessage.ToolMessage -> AiChatMessageEntity(
        uid = id,
        chat_id = chatId,
        type = type.toString(),
        name = null,
        content = content,
        prefix = null,
        reasoning_content = null,
        tool_calls = null,
        tool_call_id = toolCallId,
        time = time.toEpochMilliseconds(),
    )
}

fun AiChatMessageEntity.mapToDomain() = when (AiAssistantMessage.Type.valueOf(type)) {
    AiAssistantMessage.Type.USER -> AiAssistantMessage.UserMessage(
        id = uid,
        content = content,
        time = time.mapEpochTimeToInstant(),
        name = name,
    )
    AiAssistantMessage.Type.ASSISTANT -> AiAssistantMessage.AssistantMessage(
        id = uid,
        content = content,
        name = name,
        time = time.mapEpochTimeToInstant(),
        prefix = prefix == 1L,
        reasoningContent = reasoning_content,
        toolCalls = tool_calls?.map {
            Json.decodeFromString<ToolCallPojo>(it).mapToDomain()
        },
    )
    AiAssistantMessage.Type.SYSTEM -> AiAssistantMessage.SystemMessage(
        id = uid,
        content = checkNotNull(content),
        name = name,
        time = time.mapEpochTimeToInstant(),
    )
    AiAssistantMessage.Type.TOOL_CALL -> AiAssistantMessage.ToolMessage(
        id = uid,
        content = checkNotNull(content),
        toolCallId = tool_call_id ?: "",
        time = time.mapEpochTimeToInstant(),
    )
}

fun ChatMessagePojo.mapToDomain(
    id: String = randomUUID(),
    time: Instant,
) = when (this) {
    is UserMessagePojo -> AiAssistantMessage.UserMessage(
        id = id,
        content = content,
        name = name,
        time = time,
    )
    is AssistantMessagePojo -> AiAssistantMessage.AssistantMessage(
        id = id,
        content = content,
        name = name,
        prefix = prefix,
        reasoningContent = reasoningContent,
        toolCalls = toolCalls?.map { it.mapToDomain() },
        time = time,
    )
    is SystemMessagePojo -> AiAssistantMessage.SystemMessage(
        id = id,
        content = content,
        name = name,
        time = time,
    )
    is ToolMessagePojo -> AiAssistantMessage.ToolMessage(
        id = id,
        content = content,
        toolCallId = toolCallId,
        time = time,
    )
}

fun ToolCall.mapToRemote() = ToolCallPojo(
    id = id,
    type = when (type) {
        ToolCallType.FUNCTION -> ToolCallTypePojo.FUNCTION
    },
    function = function.mapToRemote()
)

fun ToolCallPojo.mapToDomain() = ToolCall(
    id = id,
    type = when (type) {
        ToolCallTypePojo.FUNCTION -> ToolCallType.FUNCTION
    },
    function = function.mapToDomain()
)

fun FunctionResponse.mapToRemote() = FunctionResponsePojo(
    name = name,
    arguments = try {
        val jsonObject = buildJsonObject {
            arguments?.forEach { (key, value) -> put(key, JsonPrimitive(value)) }
        }
        Json.encodeToString(jsonObject)
    } catch (e: Exception) {
        "{}"
    }
)

fun FunctionResponsePojo.mapToDomain() = FunctionResponse(
    name = name,
    arguments = try {
        val json = arguments?.let { Json.parseToJsonElement(it) as? JsonObject }
        json?.mapValues { it.value.jsonPrimitive.content } ?: emptyMap()
    } catch (e: Exception) {
        emptyMap()
    },
)

fun ChatCompletionResponsePojo.mapToDomain(time: Instant) = AiAssistantResponse(
    id = id,
    choices = choices.map { it.mapToDomain(time) },
    created = created,
    model = model,
    systemFingerprint = systemFingerprint,
    usage = usage.mapToDomain(),
)

fun ChatUsagePojo.mapToDomain() = AiChatUsage(
    promptTokens = promptTokens,
    completionTokens = completionTokens,
    totalTokens = totalTokens,
)

fun ChatCompletionChoicePojo.mapToDomain(time: Instant) = AiAssistantResponseChoice(
    index = index,
    message = message.mapToDomain(time = time),
    finishReason = finishReason?.let { AiAssistantFinishReason.fromString(it) }
)