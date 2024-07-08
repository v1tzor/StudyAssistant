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

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationDetailsEntity
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeEntity
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationEntity

/**
 * @author Stanislav Aleshin on 08.07.2024.
 */
fun OrganizationDetailsEntity.mapToBase() = OrganizationEntity(
    uid = uid,
    is_main = if (isMain) 1L else 0L,
    short_name = shortName,
    full_name = fullName,
    type = type,
    avatar = avatar,
    schedule_time_intervals = Json.encodeToString(scheduleTimeIntervals),
    emails = emails.map { Json.encodeToString(it) },
    phones = phones.map { Json.encodeToString(it) },
    locations = locations.map { Json.encodeToString(it) },
    webs = webs.map { Json.encodeToString(it) },
    offices = offices,
    is_hide = if (isHide) 1L else 0L,
)

fun OrganizationEntity.mapToDetails(
    subjects: List<SubjectDetailsEntity>,
    employee: List<EmployeeEntity>,
) = OrganizationDetailsEntity(
    uid = uid,
    isMain = is_main == 1L,
    shortName = short_name,
    fullName = full_name,
    type = type,
    avatar = avatar,
    scheduleTimeIntervals = Json.decodeFromString(schedule_time_intervals),
    subjects = subjects,
    employee = employee,
    emails = emails.map { Json.decodeFromString(it) },
    phones = phones.map { Json.decodeFromString(it) },
    locations = locations.map { Json.decodeFromString(it) },
    webs = webs.map { Json.decodeFromString(it) },
    offices = offices,
    isHide = is_hide == 1L,
)