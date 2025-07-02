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

package ru.aleshin.studyassistant.core.remote.models.organizations

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.users.ContactInfoPojo
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
@Serializable
data class OrganizationDetailsPojo(
    val uid: UID,
    val userId: UID,
    val isMain: Boolean,
    val shortName: String,
    val fullName: String? = null,
    val type: String,
    val avatar: String? = null,
    val scheduleTimeIntervals: ScheduleTimeIntervalsPojo,
    val subjects: List<SubjectDetailsPojo> = emptyList(),
    val employee: List<EmployeePojo> = emptyList(),
    val emails: List<ContactInfoPojo> = emptyList(),
    val phones: List<ContactInfoPojo> = emptyList(),
    val locations: List<ContactInfoPojo> = emptyList(),
    val webs: List<ContactInfoPojo> = emptyList(),
    val offices: List<String> = emptyList(),
    val isHide: Boolean = false,
)