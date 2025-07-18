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

package ru.aleshin.studyassistant.core.common.extensions

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

val Json.Encode: Json
    get() = Json {
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = true
        useAlternativeNames = false
    }

val Json.Decode: Json
    get() = Json {
        isLenient = true
        ignoreUnknownKeys = true
        encodeDefaults = true
        explicitNulls = false
        useAlternativeNames = false
    }

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
@OptIn(InternalSerializationApi::class)
fun <T : Any> Map<String, Any?>.mapToSerializable(kclass: KClass<T>): T {
    val jsonObject = this.toJsonObject()
    return Json.Decode.decodeFromJsonElement(kclass.serializer(), jsonObject)
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
        JsonPrimitive(this.toString())
    }
}

inline fun <reified T> Map<String, T>.encodeToString(): List<String> {
    return map { entry ->
        val jsonObject = buildJsonObject {
            put(entry.key, entry.value.toJson())
        }
        jsonObject.toString()
    }
}

inline fun <reified T> List<String>.decodeFromString(): Map<String, T> {
    val json = Json.Decode
    return associate { raw ->
        json.decodeFromString<JsonObject>(raw).let { jsonObject ->
            val (key, element) = jsonObject.entries.first()
            key to json.decodeFromString<T>(element.jsonPrimitive.content)
        }
    }
}

inline fun <reified T> String.fromJson(): T = Json.Decode.decodeFromString<T>(this)

fun <T> String.fromJson(deserializer: DeserializationStrategy<T>): T {
    return Json.Decode.decodeFromString(deserializer, this)
}

fun <T> JsonElement.fromJson(deserializer: DeserializationStrategy<T>): T {
    return jsonCast(JsonElement.serializer(), deserializer)
}

fun <T> String.tryFromJson(deserializer: DeserializationStrategy<T>): T? = try {
    fromJson(deserializer)
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun <T> JsonElement.tryFromJson(deserializer: DeserializationStrategy<T>): T? = try {
    fromJson(deserializer)
} catch (e: Exception) {
    e.printStackTrace()
    null
}

inline fun <reified T> T.toJson(): String = Json.Encode.encodeToString(this)

fun <T> T.toJson(serializer: SerializationStrategy<T>): String {
    return Json.Encode.encodeToString(serializer, this)
}

fun <T, R> T.jsonCast(
    serializer: SerializationStrategy<T>,
    deserializer: DeserializationStrategy<R>,
): R = toJson(serializer).fromJson(deserializer)

inline fun <reified T> T.tryJsonCast(): T? = try {
    toJson().fromJson<T>()
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun JsonElement.getString(key: String): String {
    return jsonObject[key]?.jsonPrimitive?.contentOrNull ?: ""
}

fun JsonElement.getStringList(key: String): List<String> {
    return jsonObject[key]?.jsonArray?.map { it.jsonPrimitive.content } ?: emptyList()
}

fun JsonElement.getLocalDateTimeOrNUll(key: String): LocalDateTime? {
    return try {
        getLocalDateTime(key)
    } catch (_: Exception) {
        null
    }
}

fun JsonElement.getLocalDateTime(key: String): LocalDateTime {
    val time = getString(key)
    return DateTimeComponents.Formats.ISO_DATE_TIME_OFFSET.parse(time).toLocalDateTime()
}