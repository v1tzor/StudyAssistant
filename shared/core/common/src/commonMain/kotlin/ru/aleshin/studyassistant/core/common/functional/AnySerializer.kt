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

package ru.aleshin.studyassistant.core.common.functional

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.PolymorphicKind
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonEncoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.double
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.long
import kotlinx.serialization.json.longOrNull

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
@OptIn(ExperimentalSerializationApi::class, InternalSerializationApi::class)
object AnySerializer : KSerializer<Any> {

    override val descriptor: SerialDescriptor = buildSerialDescriptor(
        serialName = "kotlin.Any",
        kind = PolymorphicKind.OPEN,
    )

    override fun serialize(encoder: Encoder, value: Any) {
        val jsonEncoder = encoder as? JsonEncoder
            ?: error("This class can be saved only by Json")

        val element = when (value) {
            is Boolean -> JsonPrimitive(value)
            is Number -> JsonPrimitive(value)
            is String -> JsonPrimitive(value)
            is List<*> -> JsonArray(value.map { serializeToElement(it) })
            is Map<*, *> -> JsonObject(value.mapKeys { (k, _) ->
                k.toString()
            }.mapValues { (_, v) -> serializeToElement(v) })
            else -> error("Not supported type: ${value::class.simpleName}")
        }

        jsonEncoder.encodeJsonElement(element)
    }

    override fun deserialize(decoder: Decoder): Any {
        val jsonDecoder = decoder as? JsonDecoder
            ?: error("This class can be loaded only by Json")

        return decodeFromElement(jsonDecoder.decodeJsonElement())
    }

    private fun serializeToElement(value: Any?): JsonElement = when (value) {
        null -> JsonNull
        is Boolean -> JsonPrimitive(value)
        is Number -> JsonPrimitive(value)
        is String -> JsonPrimitive(value)
        is List<*> -> JsonArray(value.map { serializeToElement(it) })
        is Map<*, *> -> JsonObject(value.mapKeys { (k, _) ->
            k.toString()
        }.mapValues { (_, v) -> serializeToElement(v) })
        else -> error("Not supported type: ${value::class.simpleName}")
    }

    private fun decodeFromElement(element: JsonElement): Any = when (element) {
        is JsonPrimitive -> when {
            element.isString -> element.content
            element.booleanOrNull != null -> element.boolean
            element.longOrNull != null -> element.long
            element.doubleOrNull != null -> element.double
            else -> element.content
        }
        is JsonArray -> element.map { decodeFromElement(it) }
        is JsonObject -> element.mapValues { decodeFromElement(it.value) }
    }
}

@OptIn(ExperimentalSerializationApi::class)
object ListAnySerializer : KSerializer<List<Any>> {
    override val descriptor: SerialDescriptor = ListSerializer(AnySerializer).descriptor

    override fun serialize(encoder: Encoder, value: List<Any>) {
        ListSerializer(AnySerializer).serialize(encoder, value)
    }

    override fun deserialize(decoder: Decoder): List<Any> {
        return ListSerializer(AnySerializer).deserialize(decoder)
    }
}