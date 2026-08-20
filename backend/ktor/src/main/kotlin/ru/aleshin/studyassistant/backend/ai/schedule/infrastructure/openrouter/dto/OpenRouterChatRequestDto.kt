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

package ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.openrouter.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Serializable
data class OpenRouterChatRequestDto(
    val model: String,
    val messages: List<OpenRouterMessageDto>,
    @SerialName("response_format")
    val responseFormat: OpenRouterResponseFormatDto? = null,
    val temperature: Double,
    @SerialName("max_tokens")
    val maxTokens: Int,
    val stream: Boolean = false,
    val provider: OpenRouterProviderPreferencesDto? = null,
    val reasoning: OpenRouterReasoningDto? = null,
)
