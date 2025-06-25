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
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

/**
 * @author Stanislav Aleshin on 21.06.2025.
 */
@Serializable(with = ToolFunctionSerializer::class)
sealed interface ToolFunctionPojo {
    val name: String
}

@Serializable
data class FunctionRequestPojo(
    override val name: String,
    val description: String?,
    val parameters: JsonObject?,
) : ToolFunctionPojo

@Serializable
data class FunctionResponsePojo(
    override val name: String,
    val arguments: String?,
) : ToolFunctionPojo

internal object ToolFunctionSerializer : JsonContentPolymorphicSerializer<ToolFunctionPojo>(ToolFunctionPojo::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<ToolFunctionPojo> {
        return when {
            "parameters" in element.jsonObject -> FunctionRequestPojo.serializer()
            "arguments" in element.jsonObject -> FunctionResponsePojo.serializer()
            else -> throw Exception("Unknown ToolFunction type")
        }
    }
}