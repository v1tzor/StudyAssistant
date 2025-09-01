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

package ru.aleshin.studyassistant.editor.impl.presentation.models.classes

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeUi

/**
 * @author Stanislav Aleshin on 01.06.2024.
 */
@Immutable
@Serializable
internal data class EditClassUi(
    val uid: UID,
    val scheduleId: UID?,
    val organization: OrganizationShortUi? = null,
    val eventType: EventType? = null,
    val subject: SubjectUi? = null,
    val customData: String? = null,
    val teacher: EmployeeUi? = null,
    val office: String? = null,
    val location: ContactInfoUi? = null,
    val startTime: Instant? = null,
    val endTime: Instant? = null,
) {

    fun isValid() = organization != null && eventType != null && subject != null &&
        teacher != null && office != null && startTime != null && endTime != null

    companion object {
        fun createEditModel(
            uid: UID?,
            scheduleId: UID?,
            organization: OrganizationShortUi? = null,
            timeRange: TimeRange? = null,
        ) = EditClassUi(
            uid = uid ?: "",
            scheduleId = scheduleId,
            organization = organization,
            startTime = timeRange?.from,
            endTime = timeRange?.to,
        )
    }
}

internal fun ClassUi.convertToEditModel() = EditClassUi(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization,
    eventType = eventType,
    subject = subject,
    customData = customData,
    teacher = teacher,
    office = office,
    location = location,
    startTime = timeRange.from,
    endTime = timeRange.to,
)

internal fun EditClassUi.convertToBase(number: Int = 0) = ClassUi(
    uid = uid,
    scheduleId = scheduleId ?: "",
    organization = checkNotNull(organization),
    eventType = checkNotNull(eventType),
    subject = subject,
    customData = customData,
    teacher = teacher,
    office = checkNotNull(office),
    location = location,
    timeRange = TimeRange(
        from = checkNotNull(startTime),
        to = checkNotNull(endTime),
    ),
    number = number,
)