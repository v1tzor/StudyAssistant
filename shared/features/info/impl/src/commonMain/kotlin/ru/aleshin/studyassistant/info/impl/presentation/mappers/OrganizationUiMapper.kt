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

package ru.aleshin.studyassistant.info.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.info.impl.domain.entities.OrganizationClassesInfo
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationClassesInfoUi
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations.OrganizationUi

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
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

internal fun OrganizationShort.mapToUi() = OrganizationShortUi(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    type = type,
    locations = locations.map { it.mapToUi() },
    avatar = avatar,
    offices = offices,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToUi(),
    updatedAt = updatedAt,
)

internal fun OrganizationClassesInfo.mapToUi() = OrganizationClassesInfoUi(
    numberOfClassesInWeek = numberOfClassesInWeek,
    classesDurationInWeek = classesDurationInWeek,
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