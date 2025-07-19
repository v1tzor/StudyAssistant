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

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 09.07.2025.
 */
@Serializable
data class IdentityPojo(
    /**
     * Identity ID.
     */
    @SerialName("\$id")
    val id: String,
    /**
     * Identity creation date in ISO 8601 format.
     */
    @SerialName("\$createdAt")
    val createdAt: String,
    /**
     * Identity update date in ISO 8601 format.
     */
    @SerialName("\$updatedAt")
    val updatedAt: String,
    /**
     * User ID.
     */
    @SerialName("userId")
    val userId: String,
    /**
     * Identity Provider.
     */
    @SerialName("provider")
    val provider: String,
    /**
     * ID of the User in the Identity Provider.
     */
    @SerialName("providerUid")
    val providerUid: String,
    /**
     * Email of the User in the Identity Provider.
     */
    @SerialName("providerEmail")
    val providerEmail: String,
    /**
     * Identity Provider Access Token.
     */
    @SerialName("providerAccessToken")
    val providerAccessToken: String,
    /**
     * The date of when the access token expires in ISO 8601 format.
     */
    @SerialName("providerAccessTokenExpiry")
    val providerAccessTokenExpiry: String,
    /**
     * Identity Provider Refresh Token.
     */
    @SerialName("providerRefreshToken")
    val providerRefreshToken: String,
)