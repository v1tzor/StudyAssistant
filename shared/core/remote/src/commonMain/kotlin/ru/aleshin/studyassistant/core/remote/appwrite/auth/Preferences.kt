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

package ru.aleshin.studyassistant.core.remote.appwrite.auth

import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlin.reflect.KClass

/**
 * @author Stanislav Aleshin on 29.06.2025.
 */
@Serializable
data class Preferences<T>(
    @SerialName("data") val data: T,
) {
    companion object {
        @OptIn(InternalSerializationApi::class)
        fun <T : Any> from(
            map: Map<String, Any>,
            nestedType: KClass<T>
        ) = Preferences<T>(
            data = Json.decodeFromString(
                deserializer = nestedType.serializer(),
                string = Json.encodeToString(map)
            )
        )
    }
}