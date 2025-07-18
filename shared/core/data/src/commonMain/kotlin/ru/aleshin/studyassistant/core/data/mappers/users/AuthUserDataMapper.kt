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

package ru.aleshin.studyassistant.core.data.mappers.users

import ru.aleshin.studyassistant.core.domain.entities.users.AuthUser
import ru.aleshin.studyassistant.core.domain.entities.users.UserSession
import ru.aleshin.studyassistant.core.remote.models.appwrite.AuthUserPojo
import ru.aleshin.studyassistant.core.remote.models.appwrite.SessionPojo

/**
 * @author Stanislav Aleshin on 01.07.2025.
 */
fun AuthUserPojo.mapToDomain(): AuthUser {
    return AuthUser(
        uid = id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        name = name,
        password = password,
        hash = hash,
        hashOptions = hashOptions,
        registration = registration,
        status = status,
        labels = labels ?: emptyList(),
        passwordUpdate = passwordUpdate,
        email = email,
        phone = phone,
        emailVerification = emailVerification,
        phoneVerification = phoneVerification,
        preferences = prefs ?: emptyMap(),
        mfa = mfa,
        accessedAt = accessedAt
    )
}

fun SessionPojo.mapToDomain(): UserSession {
    return UserSession(
        id = id,
        createdAt = createdAt,
        updatedAt = updatedAt,
        userId = userId,
        expire = expire,
        provider = provider,
        providerUid = providerUid,
        providerAccessToken = providerAccessToken,
        providerAccessTokenExpiry = providerAccessTokenExpiry,
        providerRefreshToken = providerRefreshToken,
        ip = ip,
        osCode = osCode,
        osName = osName,
        osVersion = osVersion,
        clientType = clientType,
        clientCode = clientCode,
        clientName = clientName,
        clientVersion = clientVersion,
        clientEngine = clientEngine,
        clientEngineVersion = clientEngineVersion,
        deviceName = deviceName,
        deviceBrand = deviceBrand,
        deviceModel = deviceModel,
        countryCode = countryCode,
        countryName = countryName,
        current = current,
        factors = factors,
        secret = secret,
        mfaUpdatedAt = mfaUpdatedAt
    )
}

fun AuthUser.mapToData(): AuthUserPojo {
    return AuthUserPojo(
        id = uid,
        createdAt = createdAt,
        updatedAt = updatedAt,
        name = name,
        password = password,
        hash = hash,
        hashOptions = hashOptions,
        registration = registration,
        status = status,
        labels = labels,
        passwordUpdate = passwordUpdate,
        email = email,
        phone = phone,
        emailVerification = emailVerification,
        phoneVerification = phoneVerification,
        prefs = preferences,
        targets = emptyList(),
        mfa = mfa,
        accessedAt = accessedAt
    )
}