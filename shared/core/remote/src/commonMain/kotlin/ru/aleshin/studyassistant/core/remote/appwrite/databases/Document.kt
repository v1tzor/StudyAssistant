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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.remote.mappers.appwrite.mapToSerializable
import kotlin.reflect.KClass

/**
 * @author Stanislav Aleshin on 26.06.2025.
 */
@Serializable
data class Document<T>(
    @SerialName("\$id") val id: String,
    @SerialName("\$collectionId") val collectionId: String,
    @SerialName("\$databaseId") val databaseId: String,
    @SerialName("\$createdAt") val createdAt: String,
    @SerialName("\$updatedAt") val updatedAt: String,
    @SerialName("\$permissions") val permissions: List<String>,
    @SerialName("data") val data: T
) {
    companion object {
        @OptIn(InternalSerializationApi::class)
        fun <T : Any> from(
            map: Map<String, Any>,
            nestedType: KClass<T>,
        ) = Document<T>(
            id = map["\$id"] as String,
            collectionId = map["\$collectionId"] as String,
            databaseId = map["\$databaseId"] as String,
            createdAt = map["\$createdAt"] as String,
            updatedAt = map["\$updatedAt"] as String,
            permissions = map["\$permissions"] as List<String>,
            data = map.mapToSerializable(nestedType),
        )
    }
}

@OptIn(InternalSerializationApi::class)
fun <S : Any> Document<Map<String, Any>>.mapData(nestedType: KClass<S>) = Document(
    id = id,
    collectionId = collectionId,
    databaseId = databaseId,
    createdAt = createdAt,
    updatedAt = updatedAt,
    permissions = permissions,
    data = data.mapToSerializable(nestedType),
)