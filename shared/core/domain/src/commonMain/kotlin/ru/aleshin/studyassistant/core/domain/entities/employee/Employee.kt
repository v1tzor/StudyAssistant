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

package ru.aleshin.studyassistant.core.domain.entities.employee

import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
data class Employee(
    val uid: UID,
    val organizationId: UID,
    val firstName: String,
    val secondName: String?,
    val patronymic: String?,
    val post: EmployeePost,
    val avatar: String? = null,
    val birthday: String? = null,
    val workTime: TimeRange? = null,
    val emails: List<ContactInfo> = emptyList(),
    val phones: List<ContactInfo> = emptyList(),
    val locations: List<ContactInfo> = emptyList(),
    val webs: List<ContactInfo> = emptyList(),
    val updatedAt: Long,
)