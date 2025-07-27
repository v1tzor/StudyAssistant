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

package ru.aleshin.studyassistant.schedule.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.organizations.MediatedOrganization
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.MediatedOrganizationUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.OrganizationUi

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
internal fun OrganizationShort.mapToUi() = OrganizationShortUi(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    type = type,
    locations = locations.map { it.mapToUi() },
    offices = offices,
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToUi(),
    updatedAt = updatedAt,
)

internal fun Organization.mapToUi() = OrganizationUi(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    fullName = fullName,
    type = type,
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToUi(),
    subjects = subjects.map { it.mapToUi() },
    employee = employee.map { it.mapToUi() },
    emails = emails.map { it.mapToUi() },
    phones = phones.map { it.mapToUi() },
    locations = locations.map { it.mapToUi() },
    webs = webs.map { it.mapToUi() },
    offices = offices,
    isHide = isHide,
    updatedAt = updatedAt,
)

internal fun MediatedOrganization.mapToUi() = MediatedOrganizationUi(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    fullName = fullName,
    type = type,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToUi(),
    subjects = subjects.map { it.mapToUi() },
    employee = employee.map { it.mapToUi() },
    emails = emails.map { it.mapToUi() },
    phones = phones.map { it.mapToUi() },
    locations = locations.map { it.mapToUi() },
    webs = webs.map { it.mapToUi() },
    offices = offices,
)

internal fun OrganizationUi.mapToDomain() = Organization(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    fullName = fullName,
    type = type,
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToDomain(),
    subjects = subjects.map { it.mapToDomain() },
    employee = employee.map { it.mapToDomain() },
    emails = emails.map { it.mapToDomain() },
    phones = phones.map { it.mapToDomain() },
    locations = locations.map { it.mapToDomain() },
    webs = webs.map { it.mapToDomain() },
    offices = offices,
    isHide = isHide,
    updatedAt = updatedAt,
)

internal fun OrganizationShortUi.mapToDomain() = OrganizationShort(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    type = type,
    locations = locations.map { it.mapToDomain() },
    offices = offices,
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToDomain(),
    updatedAt = updatedAt,
)

internal fun MediatedOrganizationUi.mapToDomain() = MediatedOrganization(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    fullName = fullName,
    type = type,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToDomain(),
    subjects = subjects.map { it.mapToDomain() },
    employee = employee.map { it.mapToDomain() },
    emails = emails.map { it.mapToDomain() },
    phones = phones.map { it.mapToDomain() },
    locations = locations.map { it.mapToDomain() },
    webs = webs.map { it.mapToDomain() },
    offices = offices,
)