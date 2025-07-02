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

package ru.aleshin.studyassistant.core.remote.appwrite.databases

import kotlinx.serialization.InternalSerializationApi
import ru.aleshin.studyassistant.core.remote.mappers.appwrite.mapToSerializable
import kotlin.reflect.KClass

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
data class RealtimeSubscription(
    private val close: () -> Unit
) {
    fun close() = close.invoke()
}

data class RealtimeCallback(
    val channels: List<String>,
    val payloadClass: KClass<*>,
    val callback: (RealtimeResponseEvent<*>) -> Unit
)

open class RealtimeResponse(
    val type: String,
    val data: Any?
)

data class RealtimeResponseEvent<T>(
    val events: List<String>,
    val channels: List<String>,
    val timestamp: String,
    var payload: T
)

@Suppress("UNCHECKED_CAST")
@OptIn(InternalSerializationApi::class)
fun <S : Any> RealtimeResponseEvent<Any>.mapData(nestedType: KClass<S>) = RealtimeResponseEvent<S>(
    events = events,
    channels = channels,
    timestamp = timestamp,
    payload = (payload as Map<String, Any>).mapToSerializable(nestedType),
)

enum class RealtimeCode(val value: Int) {
    POLICY_VIOLATION(1008),
    UNKNOWN_ERROR(-1)
}