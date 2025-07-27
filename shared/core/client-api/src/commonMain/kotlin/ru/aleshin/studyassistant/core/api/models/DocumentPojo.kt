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
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import ru.aleshin.studyassistant.core.common.extensions.getString
import ru.aleshin.studyassistant.core.common.extensions.getStringList
import ru.aleshin.studyassistant.core.common.extensions.jsonCast

/**
 * @author Stanislav Aleshin on 26.06.2025.
 */
@Serializable
data class DocumentPojo<T>(
    /**
     * Document ID.
     */
    @SerialName("\$id")
    val id: String,
    /**
     * Collection ID.
     */
    @SerialName("\$collectionId")
    val collectionId: String,
    /**
     * Database ID.
     */
    @SerialName("\$databaseId")
    val databaseId: String,
    /**
     * Document creation date in ISO 8601 format.
     */
    @SerialName("\$createdAt")
    val createdAt: String,
    /**
     * Document update date in ISO 8601 format.
     */
    @SerialName("\$updatedAt")
    val updatedAt: String,
    /**
     * Document permissions. [Learn more about permissions](https://appwrite.io/docs/permissions).
     */
    @SerialName("\$permissions")
    val permissions: List<String>,
    /**
     * Additional properties
     */
    @SerialName("data")
    val data: T,
)

fun <T> JsonElement.asDocument(deserializer: DeserializationStrategy<T>): DocumentPojo<T> {
    val keys = listOf("\$collectionId", "\$databaseId", "\$createdAt", "\$updatedAt", "\$permissions")
    val dataObject = buildJsonObject {
        jsonObject.entries.forEach {
            if (!keys.contains(it.key)) {
                put(it.key, it.value)
            }
        }
    }
    return DocumentPojo(
        id = getString("\$id"),
        collectionId = getString("\$collectionId"),
        databaseId = getString("\$databaseId"),
        createdAt = getString("\$createdAt"),
        updatedAt = getString("\$updatedAt"),
        permissions = getStringList("\$permissions"),
        data = dataObject.jsonCast(JsonElement.serializer(), deserializer),
    )
}