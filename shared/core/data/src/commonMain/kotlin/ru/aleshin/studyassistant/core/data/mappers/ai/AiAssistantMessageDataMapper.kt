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
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.domain.entities.ai.AiAssistantMessage
import ru.aleshin.studyassistant.core.remote.models.ai.AssistantMessagePojo
import ru.aleshin.studyassistant.core.remote.models.ai.ChatMessagePojo
import ru.aleshin.studyassistant.core.remote.models.ai.SystemMessagePojo
import ru.aleshin.studyassistant.core.remote.models.ai.ToolCallPojo
import ru.aleshin.studyassistant.core.remote.models.ai.ToolMessagePojo
import ru.aleshin.studyassistant.core.remote.models.ai.UserMessagePojo
import ru.aleshin.studyassistant.sqldelight.ai.AiChatMessageEntity

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
fun AiAssistantMessage.mapToRemote() = when (this) {
    is AiAssistantMessage.UserMessage -> UserMessagePojo(content = content, name = name)
    is AiAssistantMessage.AssistantMessage -> AssistantMessagePojo(
        content = content,
        name = name,
        prefix = prefix,
        reasoningContent = reasoningContent,
        toolCalls = toolCalls?.map { it.mapToRemote() },
    )
    is AiAssistantMessage.SystemMessage -> SystemMessagePojo(content = content, name = name)
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
        toolCalls = tool_calls?.map { Json.decodeFromString<ToolCallPojo>(it).mapToDomain() },
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
    is UserMessagePojo -> AiAssistantMessage.UserMessage(id, content, time, name)
    is AssistantMessagePojo -> AiAssistantMessage.AssistantMessage(
        id = id,
        content = content,
        name = name,
        prefix = prefix,
        reasoningContent = reasoningContent,
        toolCalls = toolCalls?.map { it.mapToDomain() },
        time = time,
    )
    is SystemMessagePojo -> AiAssistantMessage.SystemMessage(id, content, time, name)
    is ToolMessagePojo -> AiAssistantMessage.ToolMessage(id, content, time, toolCallId)
}
