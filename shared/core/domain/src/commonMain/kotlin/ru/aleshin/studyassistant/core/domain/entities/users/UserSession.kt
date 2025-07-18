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

package ru.aleshin.studyassistant.core.domain.entities.users

/**
 * @author Stanislav Aleshin on 05.07.2025.
 */
data class UserSession(
    val id: String,
    val createdAt: String,
    val updatedAt: String,
    val userId: String,
    val expire: String,
    val provider: String,
    val providerUid: String,
    val providerAccessToken: String,
    val providerAccessTokenExpiry: String,
    val providerRefreshToken: String,
    val ip: String,
    val osCode: String,
    val osName: String,
    val osVersion: String,
    val clientType: String,
    val clientCode: String,
    val clientName: String,
    val clientVersion: String,
    val clientEngine: String,
    val clientEngineVersion: String,
    val deviceName: String,
    val deviceBrand: String,
    val deviceModel: String,
    val countryCode: String,
    val countryName: String,
    val current: Boolean,
    val factors: List<String>,
    val secret: String,
    val mfaUpdatedAt: String,
)