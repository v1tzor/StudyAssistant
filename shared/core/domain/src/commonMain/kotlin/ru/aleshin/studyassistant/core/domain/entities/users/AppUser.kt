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

import ru.aleshin.studyassistant.core.common.extensions.generateDigitCode
import ru.aleshin.studyassistant.core.common.functional.UID

/**
 * @author Stanislav Aleshin on 20.04.2024.
 */
data class AppUser(
    val uid: UID,
    val devices: List<UserDevice>,
    val username: String,
    val email: String,
    val code: String,
    val avatar: String? = null,
    val description: String? = null,
    val city: String? = null,
    val birthday: String? = null,
    val gender: Gender? = null,
    val friends: List<UID> = emptyList(),
    val subscriptionInfo: SubscribeInfo? = null,
    val socialNetworks: List<SocialNetwork> = emptyList(),
    val updatedAt: Long,
) {
    companion object {
        fun createNewUser(
            uid: UID,
            device: UserDevice,
            username: String,
            email: String,
            createdAt: Long,
        ) = AppUser(
            uid = uid,
            devices = listOf(device),
            username = username,
            email = email,
            code = generateDigitCode(),
            updatedAt = createdAt,
        )
    }
}