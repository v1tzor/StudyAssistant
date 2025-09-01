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

package ru.aleshin.studyassistant.schedule.impl.presentation.models.organization

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfoType
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.schedule.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.EmployeeUi

/**
 * @author Stanislav Aleshin on 15.08.2024.
 */
@Immutable
@Serializable
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
    val offices: List<String> = emptyList(),
    val isHide: Boolean = false,
    val updatedAt: Long = 0L,
) {

    fun groupedContactInfo(): Map<ContactInfoUi, ContactInfoType> = buildMap {
        putAll(emails.sortedBy { it.label }.map { Pair(it, ContactInfoType.EMAIL) })
        putAll(phones.sortedBy { it.label }.map { Pair(it, ContactInfoType.PHONE) })
        putAll(locations.sortedBy { it.label }.map { Pair(it, ContactInfoType.LOCATION) })
        putAll(webs.sortedBy { it.label }.map { Pair(it, ContactInfoType.WEBSITE) })
    }
}