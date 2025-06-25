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

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
data class AiAssistantResponse(
    val id: String,
    val choices: List<AiAssistantResponseChoice>,
    val created: Long,
    val model: String,
    val systemFingerprint: String? = null,
    val usage: AiChatUsage,
)

data class AiAssistantResponseChoice(
    val index: Int,
    val message: AiAssistantMessage,
    val finishReason: AiAssistantFinishReason?,
)

enum class AiAssistantFinishReason(val reason: String) {
    STOP("stop"),
    LENGTH("length"),
    CONTENT_FILTER("content_filter"),
    TOOL_CALLS("tool_calls"),
    INSUFFICIENT_SYSTEM_RESOURCE("insufficient_system_resource");

    companion object {
        fun fromString(reason: String): AiAssistantFinishReason? {
            return entries.find { it.reason == reason }
        }
    }
}

data class AiChatUsage(
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int
)