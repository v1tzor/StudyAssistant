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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.decodeFromJsonElement

/**
 * @author Stanislav Aleshin on 20.08.2026.
 */
internal object LenientDraftEntriesSerializer : KSerializer<List<DeepSeekScheduleDraftEntryDto>> {

    private val listSerializer = ListSerializer(DeepSeekScheduleDraftEntryDto.serializer())

    override val descriptor: SerialDescriptor = listSerializer.descriptor

    override fun serialize(encoder: Encoder, value: List<DeepSeekScheduleDraftEntryDto>) {
        encoder.encodeSerializableValue(listSerializer, value)
    }

    override fun deserialize(decoder: Decoder): List<DeepSeekScheduleDraftEntryDto> {
        val jsonDecoder = decoder as? JsonDecoder ?: return decoder.decodeSerializableValue(listSerializer)
        val element = jsonDecoder.decodeJsonElement()
        val array = element as? JsonArray ?: return emptyList()
        return array.mapNotNull { item ->
            runCatching {
                jsonDecoder.json.decodeFromJsonElement(DeepSeekScheduleDraftEntryDto.serializer(), item)
            }.getOrNull()
        }
    }
}
