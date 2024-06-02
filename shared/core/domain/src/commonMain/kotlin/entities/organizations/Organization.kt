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

package entities.organizations

import entities.common.ContactInfo
import entities.employee.Employee
import entities.subject.Subject
import functional.UID

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
data class Organization(
    val uid: UID,
    val isMain: Boolean,
    val shortName: String,
    val fullName: String? = null,
    val type: OrganizationType = OrganizationType.SCHOOL,
    val avatar: String? = null,
    val scheduleTimeIntervals: ScheduleTimeIntervals = ScheduleTimeIntervals(),
    val subjects: List<Subject> = emptyList(),
    val employee: List<Employee> = emptyList(),
    val emails: List<ContactInfo> = emptyList(),
    val phones: List<ContactInfo> = emptyList(),
    val locations: List<ContactInfo> = emptyList(),
    val webs: List<ContactInfo> = emptyList(),
    val offices: List<Int> = emptyList(),
    val isHide: Boolean = false,
)
