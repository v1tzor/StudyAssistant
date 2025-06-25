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
data class ChatCompletionResponsePojo(
    val id: String,
    val choices: List<ChatCompletionChoicePojo>,
    val created: Long,
    val model: String,
    val systemFingerprint: String? = null,
    val `object`: String,
    val usage: ChatUsagePojo,
)

@Serializable
data class ChatCompletionChoicePojo(
    @SerialName("index")
    val index: Int,

    @SerialName("message")
    val message: ChatMessagePojo,

    @SerialName("finish_reason")
    val finishReason: String? // "stop", "length", "content_filter", etc.
)

@Serializable
data class ChatUsagePojo(
    @SerialName("prompt_tokens")
    val promptTokens: Int,

    @SerialName("completion_tokens")
    val completionTokens: Int,

    @SerialName("total_tokens")
    val totalTokens: Int
)