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

package ru.aleshin.studyassistant.core.database.models.users

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.database.utils.BaseLocalEntity

/**
 * @author Stanislav Aleshin on 24.07.2025.
 */
@Serializable
data class BaseAppUserEntity(
    override val uid: String,
    val devices: List<String>,
    val username: String,
    val email: String,
    val code: String,
    val avatar: String?,
    val description: String?,
    val city: String?,
    val birthday: String?,
    val sex: String?,
    val friends: List<String>,
    val subscriptionInfo: String?,
    val socialNetworks: List<String>,
    override val updatedAt: Long,
) : BaseLocalEntity()