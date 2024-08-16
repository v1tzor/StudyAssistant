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

package ru.aleshin.studyassistant.core.domain.entities.classes

import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType

/**
 * @author Stanislav Aleshin on 01.05.2024.
 */
data class MediatedClass(
    val uid: UID,
    val scheduleId: UID,
    val organizationId: UID,
    val eventType: EventType,
    val subjectId: UID?,
    val customData: String? = null,
    val teacherId: UID?,
    val office: String,
    val location: ContactInfo?,
    val timeRange: TimeRange,
    val notification: Boolean = false,
)

fun Class.convertToMediate() = MediatedClass(
    uid = uid,
    scheduleId = scheduleId,
    organizationId = organization.uid,
    eventType = eventType,
    subjectId = subject?.uid,
    customData = customData,
    teacherId = teacher?.uid,
    office = office,
    location = location,
    timeRange = timeRange,
    notification = notification,
)