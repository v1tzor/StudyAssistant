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

package ru.aleshin.studyassistant.core.data.handlers

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal object FunctionArgumentsHandler {

    fun encode(arguments: Map<String, String>?): String = try {
        val jsonObject = buildJsonObject {
            arguments?.forEach { (key, value) -> put(key, JsonPrimitive(value)) }
        }
        Json.encodeToString(jsonObject)
    } catch (_: Exception) {
        "{}"
    }

    fun decode(arguments: String?): Map<String, String> = try {
        val json = arguments?.let { Json.parseToJsonElement(it) as? JsonObject }
        json?.mapValues { entry ->
            if (entry.value is JsonPrimitive) {
                entry.value.jsonPrimitive.content
            } else {
                Json.encodeToString(entry.value)
            }
        } ?: emptyMap()
    } catch (_: Exception) {
        emptyMap()
    }
}
