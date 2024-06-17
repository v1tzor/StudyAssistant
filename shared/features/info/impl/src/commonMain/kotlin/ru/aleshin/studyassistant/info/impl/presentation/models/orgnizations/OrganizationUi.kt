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

package ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import entities.common.ContactInfoType
import entities.organizations.OrganizationType
import functional.UID
import ru.aleshin.studyassistant.info.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.info.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.info.impl.presentation.models.users.EmployeeUi

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Parcelize
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
) : Parcelable {

    fun groupedContactInfo() = mutableMapOf<ContactInfoType, ContactInfoUi>().apply {
        putAll(emails.sortedBy { it.label }.map { Pair(ContactInfoType.EMAIL, it) })
        putAll(phones.sortedBy { it.label }.map { Pair(ContactInfoType.PHONE, it) })
        putAll(locations.sortedBy { it.label }.map { Pair(ContactInfoType.LOCATION, it) })
        putAll(webs.sortedBy { it.label }.map { Pair(ContactInfoType.WEBSITE, it) })
    }.toMap()
}