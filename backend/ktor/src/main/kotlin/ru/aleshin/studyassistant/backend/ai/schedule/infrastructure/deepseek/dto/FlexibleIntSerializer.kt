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

package ru.aleshin.studyassistant.backend.ai.schedule.infrastructure.deepseek.dto

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
internal object FlexibleIntSerializer : KSerializer<Int> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleInt", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int) {
        encoder.encodeInt(value)
    }

    override fun deserialize(decoder: Decoder): Int {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            return parseInt(jsonDecoder.decodeJsonElement() as? JsonPrimitive)
                ?: throw SerializationException("Expected int")
        }
        return decoder.decodeInt()
    }
}

@OptIn(ExperimentalSerializationApi::class)
internal object FlexibleNullableIntSerializer : KSerializer<Int?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("FlexibleNullableInt", PrimitiveKind.INT)

    override fun serialize(encoder: Encoder, value: Int?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            encoder.encodeInt(value)
        }
    }

    override fun deserialize(decoder: Decoder): Int? {
        val jsonDecoder = decoder as? JsonDecoder
        if (jsonDecoder != null) {
            return when (val element = jsonDecoder.decodeJsonElement()) {
                JsonNull -> null
                is JsonPrimitive -> parseInt(element)
                else -> null
            }
        }
        return if (decoder.decodeNotNullMark()) {
            decoder.decodeInt()
        } else {
            decoder.decodeNull()
        }
    }
}

private fun parseInt(primitive: JsonPrimitive?): Int? {
    if (primitive == null) return null
    return primitive.intOrNull
        ?: primitive.longOrNull?.toInt()
        ?: primitive.doubleOrNull?.toInt()
        ?: primitive.content.trim().toIntOrNull()
        ?: primitive.content.trim().toDoubleOrNull()?.toInt()
}
