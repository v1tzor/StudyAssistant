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

package ru.aleshin.studyassistant.schedule.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.classes.MediatedClass
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToDomain as mapUserToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToUi as mapUserToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.MediatedClassUi

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal fun MediatedClass.mapToUi() = MediatedClassUi(
    uid = uid,
    scheduleId = scheduleId,
    organizationId = organizationId,
    eventType = eventType,
    subjectId = subjectId,
    customData = customData,
    teacherId = teacherId,
    office = office,
    location = location?.mapUserToUi(),
    timeRange = timeRange,
)

internal fun MediatedClassUi.mapToDomain() = MediatedClass(
    uid = uid,
    scheduleId = scheduleId,
    organizationId = organizationId,
    eventType = eventType,
    subjectId = subjectId,
    customData = customData,
    teacherId = teacherId,
    office = office,
    location = location?.mapUserToDomain(),
    timeRange = timeRange,
)
