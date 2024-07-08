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
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.common.functional.UID
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.platform.NullInstantParceler

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Parcelize
internal data class EditEmployeeUi(
    val uid: UID,
    val organizationId: UID,
    val firstName: String? = null,
    val secondName: String? = null,
    val patronymic: String? = null,
    val post: EmployeePost? = null,
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

    fun isValid() = !firstName.isNullOrBlank() && post != null

    companion object {
        fun createEditModel(
            uid: UID?,
            organizationId: UID,
        ) = EditEmployeeUi(
            uid = uid ?: "",
            organizationId = organizationId,
        )
    }
}

internal fun EmployeeUi.convertToEdit() = EditEmployeeUi(
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

internal fun EditEmployeeUi.convertToBase() = EmployeeUi(
    uid = uid,
    organizationId = organizationId,
    firstName = checkNotNull(firstName),
    secondName = secondName,
    patronymic = patronymic,
    post = checkNotNull(post),
    avatar = avatar,
    birthday = birthday,
    workTimeStart = workTimeStart,
    workTimeEnd = workTimeEnd,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
)