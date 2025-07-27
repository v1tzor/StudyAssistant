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

package ru.aleshin.studyassistant.core.remote.models.requests

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.remote.models.users.AppUserPojoDetails
import ru.aleshin.studyassistant.core.remote.utils.BaseRemotePojo

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
@Serializable
data class FriendRequestsDetailsPojo(
    @SerialName("\$id")
    override val id: String,
    val received: Map<AppUserPojoDetails, Long> = emptyMap(),
    val send: Map<AppUserPojoDetails, Long> = emptyMap(),
    val lastActions: Map<AppUserPojoDetails, Boolean> = emptyMap(),
    override val updatedAt: Long,
) : BaseRemotePojo()