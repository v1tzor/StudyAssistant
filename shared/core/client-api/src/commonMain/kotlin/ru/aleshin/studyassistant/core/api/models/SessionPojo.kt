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
 * @author Stanislav Aleshin on 29.06.2025.
 */
@Serializable
data class SessionPojo(
    /**
     * Session ID.
     */
    @SerialName("\$id")
    val id: String,

    /**
     * Session creation date in ISO 8601 format.
     */
    @SerialName("\$createdAt")
    val createdAt: String,

    /**
     * Session update date in ISO 8601 format.
     */
    @SerialName("\$updatedAt")
    val updatedAt: String,

    /**
     * User ID.
     */
    @SerialName("userId")
    val userId: String,

    /**
     * Session expiration date in ISO 8601 format.
     */
    @SerialName("expire")
    val expire: String,

    /**
     * Session Provider.
     */
    @SerialName("provider")
    val provider: String,

    /**
     * Session Provider User ID.
     */
    @SerialName("providerUid")
    val providerUid: String,

    /**
     * Session Provider Access Token.
     */
    @SerialName("providerAccessToken")
    val providerAccessToken: String,

    /**
     * The date of when the access token expires in ISO 8601 format.
     */
    @SerialName("providerAccessTokenExpiry")
    val providerAccessTokenExpiry: String,

    /**
     * Session Provider Refresh Token.
     */
    @SerialName("providerRefreshToken")
    val providerRefreshToken: String,

    /**
     * IP in use when the session was created.
     */
    @SerialName("ip")
    val ip: String,

    /**
     * Operating system code name. View list of [available options](https://github.com/appwrite/appwrite/blob/master/docs/lists/os.json).
     */
    @SerialName("osCode")
    val osCode: String,

    /**
     * Operating system name.
     */
    @SerialName("osName")
    val osName: String,

    /**
     * Operating system version.
     */
    @SerialName("osVersion")
    val osVersion: String,

    /**
     * Client type.
     */
    @SerialName("clientType")
    val clientType: String,

    /**
     * Client code name. View list of [available options](https://github.com/appwrite/appwrite/blob/master/docs/lists/clients.json).
     */
    @SerialName("clientCode")
    val clientCode: String,

    /**
     * Client name.
     */
    @SerialName("clientName")
    val clientName: String,

    /**
     * Client version.
     */
    @SerialName("clientVersion")
    val clientVersion: String,

    /**
     * Client engine name.
     */
    @SerialName("clientEngine")
    val clientEngine: String,

    /**
     * Client engine name.
     */
    @SerialName("clientEngineVersion")
    val clientEngineVersion: String,

    /**
     * Device name.
     */
    @SerialName("deviceName")
    val deviceName: String,

    /**
     * Device brand name.
     */
    @SerialName("deviceBrand")
    val deviceBrand: String,

    /**
     * Device model name.
     */
    @SerialName("deviceModel")
    val deviceModel: String,

    /**
     * Country two-character ISO 3166-1 alpha code.
     */
    @SerialName("countryCode")
    val countryCode: String,

    /**
     * Country name.
     */
    @SerialName("countryName")
    val countryName: String,

    /**
     * Returns true if this the current user session.
     */
    @SerialName("current")
    val current: Boolean,

    /**
     * Returns a list of active session factors.
     */
    @SerialName("factors")
    val factors: List<String>,

    /**
     * Secret used to authenticate the user. Only included if the request was made with an API key
     */
    @SerialName("secret")
    val secret: String,

    /**
     * Most recent date in ISO 8601 format when the session successfully passed MFA challenge.
     */
    @SerialName("mfaUpdatedAt")
    val mfaUpdatedAt: String,

)