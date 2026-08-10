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

package ru.aleshin.studyassistant.core.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 30.06.2025.
 */
@Serializable
data class FilePojo(
    @SerialName("\$id") val id: String,
    @SerialName("bucketId") val bucketId: String,
    @SerialName("\$createdAt") val createdAt: String = "",
    @SerialName("\$updatedAt") val updatedAt: String = "",
    @SerialName("\$permissions") val permissions: List<String> = emptyList(),
    @SerialName("name") val name: String = "",
    @SerialName("signature") val signature: String = "",
    @SerialName("mimeType") val mimeType: String = "",
    @SerialName("sizeOriginal") val sizeOriginal: Long = 0L,
    @SerialName("chunksTotal") val chunksTotal: Long = 0L,
    @SerialName("chunksUploaded") val chunksUploaded: Long = 0L,
)
