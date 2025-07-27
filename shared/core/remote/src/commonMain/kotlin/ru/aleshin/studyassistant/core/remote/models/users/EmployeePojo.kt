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
import ru.aleshin.studyassistant.core.remote.utils.BaseMultipleRemotePojo

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
@Serializable
data class EmployeePojo(
    @SerialName("\$id")
    override val id: UID,
    override val userId: UID,
    val organizationId: UID,
    val firstName: String,
    val secondName: String? = null,
    val patronymic: String? = null,
    val post: String,
    val avatar: String? = null,
    val birthday: String? = null,
    val workTimeStart: Long? = null,
    val workTimeEnd: Long? = null,
    val emails: List<String> = emptyList(),
    val phones: List<String> = emptyList(),
    val locations: List<String> = emptyList(),
    val webs: List<String> = emptyList(),
    override val updatedAt: Long = 0L,
) : BaseMultipleRemotePojo()