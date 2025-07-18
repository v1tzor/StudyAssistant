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

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.AnySerializer
import ru.aleshin.studyassistant.core.common.functional.ListAnySerializer
import ru.aleshin.studyassistant.core.common.functional.MapAnySerializer

/**
 * @author Stanislav Aleshin on 29.06.2025.
 */
@Serializable
data class AuthUserPojo(
    /**
     * User ID.
     */
    @SerialName("\$id")
    val id: String,

    /**
     * User creation date in ISO 8601 format.
     */
    @SerialName("\$createdAt")
    val createdAt: String,

    /**
     * User update date in ISO 8601 format.
     */
    @SerialName("\$updatedAt")
    val updatedAt: String,

    /**
     * User name.
     */
    @SerialName("name")
    val name: String,

    /**
     * Hashed user password.
     */
    @SerialName("password")
    var password: String? = null,

    /**
     * Password hashing algorithm.
     */
    @SerialName("hash")
    var hash: String? = null,

    /**
     * Password hashing algorithm configuration.
     */
    @Serializable(with = AnySerializer::class)
    @SerialName("hashOptions")
    var hashOptions: Any? = null,

    /**
     * User registration date in ISO 8601 format.
     */
    @SerialName("registration")
    val registration: String,

    /**
     * User status. Pass `true` for enabled and `false` for disabled.
     */
    @SerialName("status")
    val status: Boolean,

    /**
     * Labels for the user.
     */
    @Serializable(with = ListAnySerializer::class)
    @SerialName("labels")
    val labels: List<@Contextual Any?>? = null,

    /**
     * Password update time in ISO 8601 format.
     */
    @SerialName("passwordUpdate")
    val passwordUpdate: String,

    /**
     * User email address.
     */
    @SerialName("email")
    val email: String,

    /**
     * User phone number in E.164 format.
     */
    @SerialName("phone")
    val phone: String,

    /**
     * Email verification status.
     */
    @SerialName("emailVerification")
    val emailVerification: Boolean,

    /**
     * Phone verification status.
     */
    @SerialName("phoneVerification")
    val phoneVerification: Boolean,

    /**
     * User preferences as a key-value object
     */
    @Serializable(with = MapAnySerializer::class)
    @SerialName("prefs")
    val prefs: Map<String, Any>? = null,

    @SerialName("targets")
    val targets: List<TargetPojo> = emptyList(),

    @SerialName("mfa")
    val mfa: Boolean = false,

    /**
     * Most recent access date in ISO 8601 format. This attribute is only updated again after 24 hours.
     */
    @SerialName("accessedAt")
    val accessedAt: String,
)