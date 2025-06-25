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

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
@Serializable
data class ToolPojo(
    val type: ToolCallTypePojo,
    val function: FunctionRequestPojo,
)

@Serializable
data class ToolCallPojo(
    val id: String,
    val type: ToolCallTypePojo,
    val function: FunctionResponsePojo,
)

@Serializable
enum class ToolCallTypePojo {
    @SerialName("function") FUNCTION,
}

@Serializable(with = ToolChoiceSerializer::class)
sealed interface ToolChoicePojo

@Serializable
enum class ChatCompletionToolChoicePojo : ToolChoicePojo {
    @SerialName("none")
    NONE,

    @SerialName("auto")
    AUTO,

    @SerialName("required")
    REQUIRED
}

@Serializable
data class ChatCompletionNamedToolChoicePojo(
    val type: ToolCallTypePojo,
    val function: ToolFunctionPojo,
) : ToolChoicePojo

internal object ToolChoiceSerializer : JsonContentPolymorphicSerializer<ToolChoicePojo>(ToolChoicePojo::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ToolChoicePojo> {
        return when {
            element is JsonObject && "type" in element.jsonObject -> ChatCompletionNamedToolChoicePojo.serializer()
            else -> ChatCompletionToolChoicePojo.serializer()
        }
    }
}