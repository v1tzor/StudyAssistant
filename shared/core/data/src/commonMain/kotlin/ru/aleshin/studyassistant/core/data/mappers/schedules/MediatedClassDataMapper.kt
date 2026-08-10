/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.core.data.mappers.schedules

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteData
import ru.aleshin.studyassistant.core.domain.entities.classes.MediatedClass
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.remote.models.classes.MediatedClassPojo

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
fun MediatedClassPojo.mapToDomain() = MediatedClass(
    uid = uid,
    scheduleId = scheduleId,
    organizationId = organizationId,
    eventType = EventType.valueOf(eventType),
    subjectId = subjectId,
    customData = customData,
    teacherId = teacherId,
    office = office,
    location = location?.mapToDomain(),
    timeRange = TimeRange(startTime.mapEpochTimeToInstant(), endTime.mapEpochTimeToInstant()),
)

fun MediatedClass.mapToRemoteData() = MediatedClassPojo(
    uid = uid,
    scheduleId = scheduleId,
    organizationId = organizationId,
    eventType = eventType.name,
    subjectId = subjectId,
    customData = customData,
    teacherId = teacherId,
    office = office,
    location = location?.mapToRemoteData(),
    startTime = timeRange.from.toEpochMilliseconds(),
    endTime = timeRange.to.toEpochMilliseconds(),
)
