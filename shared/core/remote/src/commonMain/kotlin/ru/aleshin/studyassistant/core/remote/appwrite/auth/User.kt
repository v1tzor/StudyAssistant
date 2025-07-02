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

import kotlinx.serialization.SerialName
import kotlin.reflect.KClass

/**
 * @author Stanislav Aleshin on 29.06.2025.
 */
data class User<T>(
    @SerialName("\$id") val id: String,
    @SerialName("\$createdAt") val createdAt: String,
    @SerialName("\$updatedAt") val updatedAt: String,
    @SerialName("name") val name: String,
    @SerialName("password") var password: String?,
    @SerialName("hash") var hash: String?,
    @SerialName("hashOptions") var hashOptions: Any?,
    @SerialName("registration") val registration: String,
    @SerialName("status") val status: Boolean,
    @SerialName("labels") val labels: List<String>,
    @SerialName("passwordUpdate") val passwordUpdate: String,
    @SerialName("email") val email: String,
    @SerialName("phone") val phone: String,
    @SerialName("emailVerification") val emailVerification: Boolean,
    @SerialName("phoneVerification") val phoneVerification: Boolean,
    @SerialName("mfa") val mfa: Boolean,
    @SerialName("prefs") val prefs: Preferences<T>,
    @SerialName("targets") val targets: List<Target>,
    @SerialName("accessedAt") val accessedAt: String,
) {
    fun toMap(): Map<String, Any> = mapOf(
        "\$id" to id as Any,
        "\$createdAt" to createdAt as Any,
        "\$updatedAt" to updatedAt as Any,
        "name" to name as Any,
        "password" to password as Any,
        "hash" to hash as Any,
        "hashOptions" to hashOptions as Any,
        "registration" to registration as Any,
        "status" to status as Any,
        "labels" to labels as Any,
        "passwordUpdate" to passwordUpdate as Any,
        "email" to email as Any,
        "phone" to phone as Any,
        "emailVerification" to emailVerification as Any,
        "phoneVerification" to phoneVerification as Any,
        "mfa" to mfa as Any,
        // "prefs" to prefs.toMap() as Any,
        "targets" to targets.map { it.toMap() } as Any,
        "accessedAt" to accessedAt as Any,
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun <T : Any> from(
            map: Map<String, Any>,
            nestedType: KClass<T>
        ) = User<T>(
            id = map["\$id"] as String,
            createdAt = map["\$createdAt"] as String,
            updatedAt = map["\$updatedAt"] as String,
            name = map["name"] as String,
            password = map["password"] as? String?,
            hash = map["hash"] as? String?,
            hashOptions = map["hashOptions"] as? Any?,
            registration = map["registration"] as String,
            status = map["status"] as Boolean,
            labels = map["labels"] as List<String>,
            passwordUpdate = map["passwordUpdate"] as String,
            email = map["email"] as String,
            phone = map["phone"] as String,
            emailVerification = map["emailVerification"] as Boolean,
            phoneVerification = map["phoneVerification"] as Boolean,
            mfa = map["mfa"] as Boolean,
            prefs = Preferences.from(map = map["prefs"] as Map<String, Any>, nestedType),
            targets = (map["targets"] as List<Map<String, Any>>).map {
                Target.from(map = it)
            },
            accessedAt = map["accessedAt"] as String,
        )
    }
}

typealias AuthUserPojo = User<Map<String, Any>>