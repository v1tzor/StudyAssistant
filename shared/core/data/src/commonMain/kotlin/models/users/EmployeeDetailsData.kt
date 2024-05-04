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

package models.users

import functional.UID
import kotlinx.serialization.Serializable

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
@Serializable
data class EmployeeDetailsData(
    val uid: UID,
    val organizationId: UID,
    val firstName: String,
    val secondName: String?,
    val patronymic: String?,
    val post: String,
    val avatar: String? = null,
    val birthday: String? = null,
    val workTimeStart: Long? = null,
    val workTimeEnd: Long? = null,
    val emails: List<ContactInfoData> = emptyList(),
    val phones: List<ContactInfoData> = emptyList(),
    val locations: List<ContactInfoData> = emptyList(),
    val webs: List<ContactInfoData> = emptyList(),
)