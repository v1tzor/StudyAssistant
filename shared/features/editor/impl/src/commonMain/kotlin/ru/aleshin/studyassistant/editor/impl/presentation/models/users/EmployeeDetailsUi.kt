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

package ru.aleshin.studyassistant.editor.impl.presentation.models.users

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.NullInstantParceler
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Parcelize
internal data class EmployeeDetailsUi(
    val uid: UID,
    val organizationId: UID,
    val firstName: String,
    val secondName: String?,
    val patronymic: String?,
    val post: EmployeePost,
    val subjects: List<SubjectUi>,
    val avatar: String? = null,
    val birthday: String? = null,
    @TypeParceler<Instant?, NullInstantParceler>
    val workTimeStart: Instant? = null,
    @TypeParceler<Instant?, NullInstantParceler>
    val workTimeEnd: Instant? = null,
    val emails: List<ContactInfoUi> = emptyList(),
    val phones: List<ContactInfoUi> = emptyList(),
    val locations: List<ContactInfoUi> = emptyList(),
    val webs: List<ContactInfoUi> = emptyList(),
) : Parcelable {
    fun fullName(): String {
        return buildString {
            append(firstName)
            if (patronymic != null) append(" ", patronymic)
            if (secondName != null) append(" ", secondName)
        }
    }
}

internal fun EmployeeDetailsUi.convertToBase() = EmployeeUi(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post,
    avatar = avatar,
    birthday = birthday,
    workTimeStart = workTimeStart,
    workTimeEnd = workTimeEnd,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
)