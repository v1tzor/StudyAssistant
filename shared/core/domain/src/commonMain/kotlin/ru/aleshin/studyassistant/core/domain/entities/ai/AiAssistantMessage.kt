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