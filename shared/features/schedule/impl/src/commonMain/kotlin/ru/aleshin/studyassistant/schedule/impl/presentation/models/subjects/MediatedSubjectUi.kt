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

package ru.aleshin.studyassistant.schedule.impl.presentation.models.subjects

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.EmployeeUi

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
@Parcelize
internal data class MediatedSubjectUi(
    val uid: UID,
    val organizationId: UID,
    val eventType: EventType,
    val name: String,
    val teacherId: UID?,
    val office: String,
    val color: Int,
    val location: ContactInfoUi?,
) : Parcelable

internal fun MediatedSubjectUi.convertToBase(
    teacherMapper: (UID?) -> EmployeeUi?,
) = SubjectUi(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType,
    name = name,
    teacher = teacherMapper(teacherId),
    office = office,
    color = color,
    location = location,
)