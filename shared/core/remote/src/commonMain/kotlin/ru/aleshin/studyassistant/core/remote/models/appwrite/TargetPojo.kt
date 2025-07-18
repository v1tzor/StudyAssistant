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

package ru.aleshin.studyassistant.core.remote.models.appwrite

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 29.06.2025.
 */
@Serializable
data class TargetPojo(
    @SerialName("\$id") val id: String,
    @SerialName("\$createdAt") val createdAt: String,
    @SerialName("\$updatedAt") val updatedAt: String,
    @SerialName("name") val name: String,
    @SerialName("userId") val userId: String,
    @SerialName("providerId") var providerId: String?,
    @SerialName("providerType") val providerType: String,
    @SerialName("identifier") val identifier: String,
    @SerialName("expired") val expired: Boolean,
) {
    fun toMap(): Map<String, Any> = mapOf(
        "\$id" to id as Any,
        "\$createdAt" to createdAt as Any,
        "\$updatedAt" to updatedAt as Any,
        "name" to name as Any,
        "userId" to userId as Any,
        "providerId" to providerId as Any,
        "providerType" to providerType as Any,
        "identifier" to identifier as Any,
        "expired" to expired as Any,
    )

    companion object Companion {

        @Suppress("UNCHECKED_CAST")
        fun from(
            map: Map<String, Any>,
        ) = TargetPojo(
            id = map["\$id"] as String,
            createdAt = map["\$createdAt"] as String,
            updatedAt = map["\$updatedAt"] as String,
            name = map["name"] as String,
            userId = map["userId"] as String,
            providerId = map["providerId"] as? String?,
            providerType = map["providerType"] as String,
            identifier = map["identifier"] as String,
            expired = map["expired"] as Boolean,
        )
    }
}