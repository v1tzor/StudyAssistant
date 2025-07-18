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
 * @author Stanislav Aleshin on 09.07.2025.
 */
@Serializable
data class MfaFactorsPojo(
    /**
     * Can TOTP be used for MFA challenge for this account.
     */
    @SerialName("totp")
    val totp: Boolean,
    /**
     * Can phone (SMS) be used for MFA challenge for this account.
     */
    @SerialName("phone")
    val phone: Boolean,
    /**
     * Can email be used for MFA challenge for this account.
     */
    @SerialName("email")
    val email: Boolean,
    /**
     * Can recovery code be used for MFA challenge for this account.
     */
    @SerialName("recoveryCode")
    val recoveryCode: Boolean,
)