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

package ru.aleshin.studyassistant.core.data.mappers.organizations

import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.subjects.mapToRemoteData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteData
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationDetailsEntity
import ru.aleshin.studyassistant.core.domain.entities.organizations.MediatedOrganization
import ru.aleshin.studyassistant.core.domain.entities.organizations.Organization
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.remote.models.organizations.MediatedOrganizationPojo
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationPojo
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationEntity

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
fun OrganizationDetailsPojo.mapToDomain() = Organization(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    fullName = fullName,
    type = OrganizationType.valueOf(type),
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
)

fun MediatedOrganizationPojo.mapToDomain() = MediatedOrganization(
    uid = uid,
    isMain = main,
    shortName = shortName,
    fullName = fullName,
    type = OrganizationType.valueOf(type),
    scheduleTimeIntervals = scheduleTimeIntervals.mapToDomain(),
    subjects = subjects.map { it.mapToDomain() },
    employee = employee.map { it.mapToDomain() },
    emails = emails.map { it.mapToDomain() },
    phones = phones.map { it.mapToDomain() },
    locations = locations.map { it.mapToDomain() },
    webs = webs.map { it.mapToDomain() },
    offices = offices,
)

fun OrganizationDetailsEntity.mapToDomain() = Organization(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    fullName = fullName,
    type = OrganizationType.valueOf(type),
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
)

fun Organization.mapToRemoteData(userId: UID) = OrganizationPojo(
    uid = uid,
    userId = userId,
    main = isMain,
    shortName = shortName,
    fullName = fullName,
    type = type.name,
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToRemoteDate().toJson(),
    emails = emails.map { it.mapToRemoteData().toJson() },
    phones = phones.map { it.mapToRemoteData().toJson() },
    locations = locations.map { it.mapToRemoteData().toJson() },
    webs = webs.map { it.mapToRemoteData().toJson() },
    offices = offices,
    hide = isHide,
)

fun MediatedOrganization.mapToRemoteData() = MediatedOrganizationPojo(
    uid = uid,
    main = isMain,
    shortName = shortName,
    fullName = fullName,
    type = type.name,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToRemoteDate(),
    subjects = subjects.map { it.mapToRemoteData() },
    employee = employee.map { it.mapToRemoteData() },
    emails = emails.map { it.mapToRemoteData() },
    phones = phones.map { it.mapToRemoteData() },
    locations = locations.map { it.mapToRemoteData() },
    webs = webs.map { it.mapToRemoteData() },
    offices = offices,
)

fun Organization.mapToLocalData() = OrganizationEntity(
    uid = uid,
    is_main = if (isMain) 1L else 0L,
    short_name = shortName,
    full_name = fullName,
    type = type.toString(),
    avatar = avatar,
    schedule_time_intervals = scheduleTimeIntervals.mapToLocalDate().toJson(),
    emails = emails.map { it.mapToLocalData().toJson() },
    phones = phones.map { it.mapToLocalData().toJson() },
    locations = locations.map { it.mapToLocalData().toJson() },
    webs = webs.map { it.mapToLocalData().toJson() },
    offices = offices,
    is_hide = if (isHide) 1L else 0L,
)