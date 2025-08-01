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

package ru.aleshin.studyassistant.core.remote.models.users

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.utils.BaseRemotePojo

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
@Serializable
data class AppUserPojo(
    @SerialName("\$id")
    override val id: UID,
    val devices: List<String> = emptyList(),
    val username: String = "",
    val email: String = "",
    val code: String = "",
    val avatar: String? = null,
    val description: String? = null,
    val city: String? = null,
    val birthday: String? = null,
    val sex: String? = null,
    val friends: List<UID> = emptyList(),
    val subscriptionInfo: String? = null,
    val socialNetworks: List<String> = emptyList(),
    override val updatedAt: Long = 0L,
) : BaseRemotePojo()