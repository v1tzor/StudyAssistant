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

package ru.aleshin.studyassistant.core.remote.mappers.appwrite

import co.touchlab.kermit.Logger
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
@OptIn(InternalSerializationApi::class)
fun <T : Any> Map<String, Any?>.mapToSerializable(kclass: KClass<T>): T {
    val jsonObject = this.toJsonObject()
    val json = Json { ignoreUnknownKeys = true }
    return json.decodeFromJsonElement(kclass.serializer(), jsonObject)
}

fun Map<String, Any?>.toJsonObject(): JsonObject {
    return buildJsonObject {
        this@toJsonObject.forEach { (key, value) ->
            put(key, value.toJsonElement())
        }
    }
}

fun Any?.toJsonElement(): JsonElement = when (this) {
    null -> JsonNull
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is Map<*, *> -> {
        val map = this as? Map<String, Any?> ?: error("Only Map<String, Any?> is supported")
        map.toJsonObject()
    }
    is List<*> -> buildJsonArray {
        this@toJsonElement.forEach { add(it.toJsonElement()) }
    }
    is String -> JsonPrimitive(this)
    else -> {
        Logger.i("test") { "error handle -> $this | ${this::class.simpleName}" }
        JsonPrimitive(this.toString())
    }
}