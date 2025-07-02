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
import ru.aleshin.studyassistant.core.remote.appwrite.auth.AuthUserPojo
import ru.aleshin.studyassistant.core.remote.appwrite.auth.Preferences

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
        registration = registration,
        status = status,
        labels = labels,
        passwordUpdate = passwordUpdate,
        email = email,
        phone = phone,
        emailVerification = emailVerification,
        phoneVerification = phoneVerification,
        mfa = mfa,
        accessedAt = accessedAt
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
        hashOptions = null,
        registration = registration,
        status = status,
        labels = labels,
        passwordUpdate = passwordUpdate,
        email = email,
        phone = phone,
        emailVerification = emailVerification,
        phoneVerification = phoneVerification,
        mfa = mfa,
        prefs = Preferences(emptyMap()),
        targets = emptyList(),
        accessedAt = accessedAt,
    )
}