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

package models.organizations

import entities.organizations.OrganizationType
import functional.UID
import kotlinx.serialization.Serializable
import models.subjects.SubjectPojo
import models.users.ContactInfoPojo
import models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
@Serializable
data class OrganizationPojo(
    val uid: UID,
    val isMain: Boolean,
    val shortName: String,
    val fullName: String? = null,
    val type: String = OrganizationType.SCHOOL.name,
    val avatar: String? = null,
    val subjects: List<SubjectPojo> = emptyList(),
    val employee: List<EmployeePojo> = emptyList(),
    val emails: List<ContactInfoPojo> = emptyList(),
    val phones: List<ContactInfoPojo> = emptyList(),
    val locations: List<ContactInfoPojo> = emptyList(),
    val webs: List<ContactInfoPojo> = emptyList(),
    val isHide: Boolean = false,
)