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

package ru.aleshin.studyassistant.preview.impl.presentation.models

import entities.organizations.OrganizationType
import functional.UID
import platform.JavaSerializable

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
internal data class OrganizationUi(
    val uid: UID,
    val isMain: Boolean,
    val shortName: String,
    val fullName: String? = null,
    val type: OrganizationType = OrganizationType.SCHOOL,
    val scheduleTimeIntervals: ScheduleTimeIntervalsUi = ScheduleTimeIntervalsUi(),
    val avatar: String? = null,
    val subjects: List<SubjectUi> = emptyList(),
    val employee: List<EmployeeUi> = emptyList(),
    val emails: List<ContactInfoUi> = emptyList(),
    val phones: List<ContactInfoUi> = emptyList(),
    val locations: List<ContactInfoUi> = emptyList(),
    val webs: List<ContactInfoUi> = emptyList(),
    val offices: List<Int> = emptyList(),
    val isHide: Boolean = false,
) : JavaSerializable {
    companion object {
        fun createMainOrganization() = OrganizationUi("", true, "")
    }
}
