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

package ru.aleshin.studyassistant.core.data.mappers.subjects

import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteData
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.domain.entities.subject.MediatedSubject
import ru.aleshin.studyassistant.core.remote.models.subjects.MediatedSubjectPojo

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
fun MediatedSubject.mapToRemoteData() = MediatedSubjectPojo(
    uid = uid,
    organizationId = organizationId,
    eventType = eventType.name,
    name = name,
    teacherId = teacherId,
    office = office,
    color = color,
    location = location?.mapToRemoteData(),
)

fun MediatedSubjectPojo.mapToDomain() = MediatedSubject(
    uid = uid,
    organizationId = organizationId,
    eventType = EventType.valueOf(eventType),
    name = name,
    teacherId = teacherId,
    office = office,
    color = color,
    location = location?.mapToDomain(),
)
