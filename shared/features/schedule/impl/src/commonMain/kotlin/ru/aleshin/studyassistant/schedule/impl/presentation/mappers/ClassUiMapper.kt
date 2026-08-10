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

import ru.aleshin.studyassistant.core.domain.entities.classes.Class
import ru.aleshin.studyassistant.core.presentation.models.schedules.ClassUi
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToDomain as mapOrganizationToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToUi as mapOrganizationToUi
import ru.aleshin.studyassistant.core.presentation.mappers.subjects.mapToDomain as mapSubjectToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.subjects.mapToUi as mapSubjectToUi
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToDomain as mapUserToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToUi as mapUserToUi

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal fun Class.mapToUi() = ClassUi(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization.mapOrganizationToUi(),
    eventType = eventType,
    subject = subject?.mapSubjectToUi(),
    customData = customData,
    teacher = teacher?.mapUserToUi(),
    office = office,
    location = location?.mapUserToUi(),
    timeRange = timeRange,
    number = number,
)

internal fun ClassUi.mapToDomain() = Class(
    uid = uid,
    scheduleId = scheduleId,
    organization = organization.mapOrganizationToDomain(),
    eventType = eventType,
    subject = subject?.mapSubjectToDomain(),
    customData = customData,
    teacher = teacher?.mapUserToDomain(),
    office = office,
    location = location?.mapUserToDomain(),
    timeRange = timeRange,
    number = number,
)
