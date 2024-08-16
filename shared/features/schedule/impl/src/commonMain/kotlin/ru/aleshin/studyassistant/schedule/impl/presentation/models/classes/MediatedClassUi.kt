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

package ru.aleshin.studyassistant.schedule.impl.presentation.models.classes

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.convertToShort
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.covertToBase
import ru.aleshin.studyassistant.schedule.impl.presentation.models.share.OrganizationLinkData
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.ContactInfoUi

/**
 * @author Stanislav Aleshin on 01.05.2024.
 */
@Parcelize
internal data class MediatedClassUi(
    val uid: UID,
    val scheduleId: UID,
    val organizationId: UID,
    val eventType: EventType,
    val subjectId: UID?,
    val customData: String? = null,
    val teacherId: UID?,
    val office: String,
    val location: ContactInfoUi?,
    val timeRange: TimeRange,
    val notification: Boolean = false,
) : Parcelable

internal fun MediatedClassUi.convertToBase(
    linkData: OrganizationLinkData,
    number: Int,
): ClassUi {
    val sharedOrganization = linkData.sharedOrganization.covertToBase()
    val organization = linkData.linkedOrganization ?: sharedOrganization
    val linkSubject = linkData.linkedSubjects[subjectId]
    val linkTeacher = linkData.linkedTeachers[teacherId]
    val actualSubject = linkSubject ?: sharedOrganization.subjects.find { it.uid == subjectId }
    val teacher = linkTeacher ?: sharedOrganization.employee.find { it.uid == teacherId }

    return ClassUi(
        uid = uid,
        scheduleId = scheduleId,
        organization = organization.convertToShort(),
        eventType = eventType,
        subject = actualSubject,
        customData = customData,
        teacher = teacher,
        office = office,
        location = location,
        timeRange = timeRange,
        number = number,
        notification = notification,
    )
}