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

package ru.aleshin.studyassistant.core.api.auth

import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.atomicfu.atomic
import ru.aleshin.studyassistant.core.api.models.AuthUserPojo
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.extensions.tryFromJson

/**
 * @author Stanislav Aleshin on 19.07.2025.
 */
interface AuthUserStorage {

    fun isUpdated(): Boolean

    fun addUserInfo(user: AuthUserPojo)

    fun fetchUserInfo(): AuthUserPojo?

    fun updateUserInfo(user: AuthUserPojo)

    fun deleteUserInfo()

    class Base(private val settings: Settings) : AuthUserStorage {

        private val isUpdated = atomic(false)

        private companion object Companion {
            const val CURRENT_USER_KEY = "CURRENT_USER"
        }

        override fun isUpdated(): Boolean {
            return isUpdated.value
        }

        override fun addUserInfo(user: AuthUserPojo) {
            settings[CURRENT_USER_KEY] = user.toJson(AuthUserPojo.serializer())
            isUpdated.value = true
        }

        override fun fetchUserInfo(): AuthUserPojo? {
            return settings.getStringOrNull(CURRENT_USER_KEY)?.tryFromJson(
                deserializer = AuthUserPojo.serializer(),
            )
        }

        override fun updateUserInfo(user: AuthUserPojo) {
            settings[CURRENT_USER_KEY] = user.toJson(AuthUserPojo.serializer())
            isUpdated.value = true
        }

        override fun deleteUserInfo() {
            settings[CURRENT_USER_KEY] = null
            isUpdated.value = true
        }
    }
}