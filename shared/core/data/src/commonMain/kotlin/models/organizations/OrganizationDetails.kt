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

import functional.UID
import kotlinx.serialization.Serializable
import models.subjects.SubjectDetails
import models.users.ContactInfoData
import models.users.EmployeeDetails

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
@Serializable
data class OrganizationDetails(
    val uid: UID,
    val isMain: Boolean,
    val shortName: String,
    val fullName: String? = null,
    val type: String,
    val avatar: String? = null,
    val subjects: List<SubjectDetails> = emptyList(),
    val employee: List<EmployeeDetails> = emptyList(),
    val emails: List<ContactInfoData> = emptyList(),
    val phones: List<ContactInfoData> = emptyList(),
    val locations: List<ContactInfoData> = emptyList(),
    val webs: List<ContactInfoData> = emptyList(),
    val isHide: Boolean = false,
)