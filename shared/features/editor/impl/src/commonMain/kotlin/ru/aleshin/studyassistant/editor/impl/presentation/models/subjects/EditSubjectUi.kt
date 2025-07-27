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

package ru.aleshin.studyassistant.editor.impl.presentation.models.subjects

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeUi

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Parcelize
internal data class EditSubjectUi(
    val uid: UID,
    val organizationId: UID,
    val eventType: EventType? = null,
    val name: String = "",
    val teacher: EmployeeUi? = null,
    val office: String? = null,
    val color: Int? = null,
    val location: ContactInfoUi? = null,
) : Parcelable {

    fun isValid() = eventType != null && name.isNotEmpty() &&
        teacher != null && office != null && color != null

    companion object {
        fun createEditModel(
            uid: UID?,
            organizationId: UID,
        ) = EditSubjectUi(
            uid = uid ?: "",
            organizationId = organizationId,
        )
    }
}

internal fun SubjectUi.convertToEditModel() = EditSubjectUi(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType,
    name = name,
    teacher = teacher,
    office = office,
    color = color,
    location = location,
)

internal fun EditSubjectUi.convertToBase() = SubjectUi(
    uid = uid,
    organizationId = organizationId,
    eventType = checkNotNull(eventType),
    name = name,
    teacher = teacher,
    office = checkNotNull(office),
    color = checkNotNull(color),
    location = location,
)