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

package ru.aleshin.studyassistant.core.api.models

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import ru.aleshin.studyassistant.core.common.extensions.fromJson

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
@Serializable
open class RealtimeResponse<T>(
    val type: String,
    val data: T,
)

@Serializable
data class RealtimeResponseEvent<T>(
    val events: Collection<String>,
    val channels: Collection<String>,
    val timestamp: String,
    var payload: T,
)

enum class RealtimeCode(val value: Int) {
    POLICY_VIOLATION(1008),
    UNKNOWN_ERROR(-1),
}

@Suppress("UNCHECKED_CAST")
@OptIn(InternalSerializationApi::class)
fun <S> RealtimeResponseEvent<JsonElement>.mapData(
    payloadType: DeserializationStrategy<S>,
) = RealtimeResponseEvent<S>(
    events = events,
    channels = channels,
    timestamp = timestamp,
    payload = payload.fromJson(payloadType),
)