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
data class TokenPojo(
    /**
     * Token ID.
     */
    @SerialName("\$id")
    val id: String,
    /**
     * Token creation date in ISO 8601 format.
     */
    @SerialName("\$createdAt")
    val createdAt: String,
    /**
     * User ID.
     */
    @SerialName("userId")
    val userId: String,
    /**
     * Token secret key. This will return an empty string unless the response is returned using an API key or as part of a webhook payload.
     */
    @SerialName("secret")
    val secret: String,
    /**
     * Token expiration date in ISO 8601 format.
     */
    @SerialName("expire")
    val expire: String,
    /**
     * Security phrase of a token. Empty if security phrase was not requested when creating a token. It includes randomly generated phrase which is also sent in the external resource such as email.
     */
    @SerialName("phrase")
    val phrase: String,
)