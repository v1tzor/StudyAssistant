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

package ru.aleshin.studyassistant.core.remote.appwrite.storage

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Client.ENDPOINT
import ru.aleshin.studyassistant.core.remote.datasources.StudyAssistantAppwrite.Client.PROJECT_ID

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
@Serializable
data class File(
    @SerialName("\$id") val id: String,
    @SerialName("bucketId") val bucketId: String,
    @SerialName("\$createdAt") val createdAt: String,
    @SerialName("\$updatedAt") val updatedAt: String,
    @SerialName("\$permissions") val permissions: List<String>,
    @SerialName("name") val name: String,
    @SerialName("signature") val signature: String,
    @SerialName("mimeType") val mimeType: String,
    @SerialName("sizeOriginal") val sizeOriginal: Long,
    @SerialName("chunksTotal") val chunksTotal: Long,
    @SerialName("chunksUploaded") val chunksUploaded: Long,
) {
    fun toMap(): Map<String, Any> = mapOf(
        "\$id" to id as Any,
        "bucketId" to bucketId as Any,
        "\$createdAt" to createdAt as Any,
        "\$updatedAt" to updatedAt as Any,
        "\$permissions" to permissions as Any,
        "name" to name as Any,
        "signature" to signature as Any,
        "mimeType" to mimeType as Any,
        "sizeOriginal" to sizeOriginal as Any,
        "chunksTotal" to chunksTotal as Any,
        "chunksUploaded" to chunksUploaded as Any,
    )

    fun getDownloadUrl() = buildString {
        append(ENDPOINT)
        append('/')
        append("storage")
        append('/')
        append("buckets")
        append('/')
        append(bucketId)
        append('/')
        append("files")
        append(id)
        append('/')
        append("view?project=")
        append(PROJECT_ID)
        append("&mode=admin")
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun from(
            map: Map<String, Any>,
        ) = File(
            id = map["\$id"] as String,
            bucketId = map["bucketId"] as String,
            createdAt = map["\$createdAt"] as String,
            updatedAt = map["\$updatedAt"] as String,
            permissions = map["\$permissions"] as List<String>,
            name = map["name"] as String,
            signature = map["signature"] as String,
            mimeType = map["mimeType"] as String,
            sizeOriginal = (map["sizeOriginal"] as Number).toLong(),
            chunksTotal = (map["chunksTotal"] as Number).toLong(),
            chunksUploaded = (map["chunksUploaded"] as Number).toLong(),
        )
    }
}

@Serializable
enum class ImageGravity(val value: String) {
    @SerialName("center")
    CENTER("center"),

    @SerialName("top-left")
    TOP_LEFT("top-left"),

    @SerialName("top")
    TOP("top"),

    @SerialName("top-right")
    TOP_RIGHT("top-right"),

    @SerialName("left")
    LEFT("left"),

    @SerialName("right")
    RIGHT("right"),

    @SerialName("bottom-left")
    BOTTOM_LEFT("bottom-left"),

    @SerialName("bottom")
    BOTTOM("bottom"),

    @SerialName("bottom-right")
    BOTTOM_RIGHT("bottom-right");

    override fun toString() = value
}

@Serializable
enum class ImageFormat(val value: String) {
    @SerialName("jpg")
    JPG("jpg"),

    @SerialName("jpeg")
    JPEG("jpeg"),

    @SerialName("png")
    PNG("png"),

    @SerialName("webp")
    WEBP("webp"),

    @SerialName("heic")
    HEIC("heic"),

    @SerialName("avif")
    AVIF("avif");

    override fun toString() = value
}