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

package ru.aleshin.studyassistant.core.database.mappers.organizations

import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.database.models.employee.BaseEmployeeEntity
import ru.aleshin.studyassistant.core.database.models.organizations.BaseOrganizationEntity
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationDetailsEntity
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationEntity

/**
 * @author Stanislav Aleshin on 08.07.2024.
 */
fun BaseOrganizationEntity.mapToEntity() = OrganizationEntity(
    uid = uid,
    is_main = isMain,
    short_name = shortName,
    full_name = fullName,
    type = type,
    avatar = avatar,
    schedule_time_intervals = scheduleTimeIntervals,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
    offices = offices,
    is_hide = isHide,
    updated_at = updatedAt,
    is_cache_data = isCacheData,
)

fun OrganizationEntity.mapToBase() = BaseOrganizationEntity(
    uid = uid,
    isMain = is_main,
    shortName = short_name,
    fullName = full_name,
    type = type,
    avatar = avatar,
    scheduleTimeIntervals = schedule_time_intervals,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
    offices = offices,
    isHide = is_hide,
    updatedAt = updated_at,
    isCacheData = is_cache_data,
)

fun OrganizationDetailsEntity.mapToBase() = BaseOrganizationEntity(
    uid = uid,
    isMain = if (isMain) 1L else 0L,
    shortName = shortName,
    fullName = fullName,
    type = type,
    avatar = avatar,
    scheduleTimeIntervals = Json.encodeToString(scheduleTimeIntervals),
    emails = emails.map { it.toJson() },
    phones = phones.map { it.toJson() },
    locations = locations.map { it.toJson() },
    webs = webs.map { it.toJson() },
    offices = offices,
    isHide = if (isHide) 1L else 0L,
    updatedAt = updatedAt,
    isCacheData = 0L,
)

fun BaseOrganizationEntity.mapToDetails(
    subjects: List<SubjectDetailsEntity>,
    employee: List<BaseEmployeeEntity>,
) = OrganizationDetailsEntity(
    uid = uid,
    isMain = isMain == 1L,
    shortName = shortName,
    fullName = fullName,
    type = type,
    avatar = avatar,
    scheduleTimeIntervals = Json.decodeFromString(scheduleTimeIntervals),
    subjects = subjects,
    employee = employee,
    emails = emails.map { it.fromJson() },
    phones = phones.map { it.fromJson() },
    locations = locations.map { it.fromJson() },
    webs = webs.map { it.fromJson() },
    offices = offices,
    isHide = isHide == 1L,
    updatedAt = updatedAt,
)

fun BaseOrganizationEntity.mapToShort() = OrganizationShortEntity(
    uid = uid,
    main = isMain == 1L,
    shortName = shortName,
    type = type,
    avatar = avatar,
    locations = locations.map { it.fromJson() },
    offices = offices,
    scheduleTimeIntervals = Json.decodeFromString(scheduleTimeIntervals),
    updatedAt = updatedAt,
)