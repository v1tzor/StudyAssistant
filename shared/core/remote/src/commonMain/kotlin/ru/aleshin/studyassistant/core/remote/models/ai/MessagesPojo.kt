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

package ru.aleshin.studyassistant.core.remote.models.ai

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonClassDiscriminator

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
@Serializable
@JsonClassDiscriminator("role")
@OptIn(ExperimentalSerializationApi::class)
sealed interface ChatMessagePojo {
    val content: String?
}

@Serializable
@SerialName("system")
data class SystemMessagePojo(
    override val content: String,
    val name: String? = null
) : ChatMessagePojo

@Serializable
@SerialName("user")
data class UserMessagePojo(
    override val content: String?,
    val name: String? = null
) : ChatMessagePojo

@Serializable
@SerialName("assistant")
data class AssistantMessagePojo(
    override val content: String?,
    val name: String? = null,
    val prefix: Boolean? = null,
    val reasoningContent: String? = null,
    val toolCalls: List<ToolCallPojo>? = null,
) : ChatMessagePojo

@Serializable
@SerialName("tool")
data class ToolMessagePojo(
    override val content: String,
    val toolCallId: String
) : ChatMessagePojo