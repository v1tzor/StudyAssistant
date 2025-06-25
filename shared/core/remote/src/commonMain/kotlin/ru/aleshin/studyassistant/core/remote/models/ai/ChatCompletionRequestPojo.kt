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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
@Serializable
data class ChatCompletionRequest(
    val messages: List<ChatMessagePojo>,
    @SerialName("model") val model: String,
    val frequencyPenalty: Double? = null,
    val maxTokens: Int? = null,
    val presencePenalty: Double? = null,
    val stream: Boolean? = null,
    val temperature: Double? = null,
    val topP: Double? = null,
    val tools: List<ToolPojo>? = null,
    val toolChoice: ToolChoicePojo? = null,
    val logprobs: Boolean? = null,
    val topLogprobs: Int? = null,
)

enum class ChatModel(val model: String) {
    DEEPSEEK_CHAT("deepseek-chat"),

    DEEPSEEK_REASONER("deepseek-reasoner"),
}