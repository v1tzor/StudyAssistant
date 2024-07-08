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

package ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeUi

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Parcelize
internal data class EditOrganizationUi(
    val uid: UID = "",
    val isMain: Boolean = false,
    val shortName: String? = null,
    val fullName: String? = null,
    val type: OrganizationType? = null,
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

    fun isValid() = !shortName.isNullOrBlank() && type != null

    companion object {
        fun createEditModel(uid: UID?) = EditOrganizationUi(uid = uid ?: "")
    }
}

internal fun OrganizationUi.convertToEdit() = EditOrganizationUi(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    fullName = fullName,
    type = type,
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals,
    subjects = subjects,
    employee = employee,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
    offices = offices,
    isHide = isHide,
)

internal fun EditOrganizationUi.convertToBase() = OrganizationUi(
    uid = uid,
    isMain = isMain,
    shortName = checkNotNull(shortName),
    fullName = fullName,
    type = checkNotNull(type),
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals,
    subjects = subjects,
    employee = employee,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
    offices = offices,
    isHide = isHide,
)