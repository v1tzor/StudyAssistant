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

import ru.aleshin.studyassistant.core.domain.entities.organizations.MediatedOrganization
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToDomain as mapIntervalsToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToUi as mapIntervalsToUi
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToDomain as mapContactToDomain
import ru.aleshin.studyassistant.core.presentation.mappers.users.mapToUi as mapContactToUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.MediatedOrganizationUi

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
internal fun MediatedOrganization.mapToUi() = MediatedOrganizationUi(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    fullName = fullName,
    type = type,
    scheduleTimeIntervals = scheduleTimeIntervals.mapIntervalsToUi(),
    subjects = subjects.map { it.mapToUi() },
    employee = employee.map { it.mapToUi() },
    emails = emails.map { it.mapContactToUi() },
    phones = phones.map { it.mapContactToUi() },
    locations = locations.map { it.mapContactToUi() },
    webs = webs.map { it.mapContactToUi() },
    offices = offices,
)

internal fun MediatedOrganizationUi.mapToDomain() = MediatedOrganization(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    fullName = fullName,
    type = type,
    scheduleTimeIntervals = scheduleTimeIntervals.mapIntervalsToDomain(),
    subjects = subjects.map { it.mapToDomain() },
    employee = employee.map { it.mapToDomain() },
    emails = emails.map { it.mapContactToDomain() },
    phones = phones.map { it.mapContactToDomain() },
    locations = locations.map { it.mapContactToDomain() },
    webs = webs.map { it.mapContactToDomain() },
    offices = offices,
)
