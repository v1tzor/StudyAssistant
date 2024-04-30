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

package mappers

import entities.organizations.Organization
import entities.organizations.OrganizationType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import models.organizations.OrganizationDetails
import models.organizations.OrganizationPojo
import models.subjects.SubjectDetails
import models.users.EmployeeDetails
import ru.aleshin.studyassistant.sqldelight.organizations.OrganizationEntity

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
fun OrganizationDetails.mapToDomain() = Organization(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    fullName = fullName,
    type = OrganizationType.valueOf(type),
    avatar = avatar,
    subjects = subjects.map { it.mapToDomain() },
    employee = employee.map { it.mapToDomain() },
    emails = emails.map { it.mapToDomain() },
    phones = phones.map { it.mapToDomain() },
    locations = locations.map { it.mapToDomain() },
    webs = webs.map { it.mapToDomain() },
    isHide = isHide,
)

fun Organization.mapToData() = OrganizationDetails(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    fullName = fullName,
    type = type.name,
    avatar = avatar,
    subjects = subjects.map { it.mapToData() },
    employee = employee.map { it.mapToData() },
    emails = emails.map { it.mapToData() },
    phones = phones.map { it.mapToData() },
    locations = locations.map { it.mapToData() },
    webs = webs.map { it.mapToData() },
    isHide = isHide,
)

fun OrganizationDetails.mapToLocalData() = OrganizationEntity(
    uid = uid,
    is_main = if (isMain) 1L else 0L,
    short_name = shortName,
    full_name = fullName,
    type = type,
    avatar = avatar,
    emails = emails.map { Json.encodeToString(it) },
    phones = phones.map { Json.encodeToString(it) },
    locations = locations.map { Json.encodeToString(it) },
    webs = webs.map { Json.encodeToString(it) },
    is_hide = if (isHide) 1L else 0L,
)

fun OrganizationEntity.mapToDetailsData(
    subjects: List<SubjectDetails>,
    employee: List<EmployeeDetails>,
) = OrganizationDetails(
    uid = uid,
    isMain = is_main == 1L,
    shortName = short_name,
    fullName = full_name,
    type = type,
    avatar = avatar,
    subjects = subjects,
    employee = employee,
    emails = emails.map { Json.decodeFromString(it) },
    phones = phones.map { Json.decodeFromString(it) },
    locations = locations.map { Json.decodeFromString(it) },
    webs = webs.map { Json.decodeFromString(it) },
    isHide = is_hide == 1L,
)

fun OrganizationDetails.mapToRemoteData() = OrganizationPojo(
    uid = uid,
    main = isMain,
    shortName = shortName,
    fullName = fullName,
    type = type,
    avatar = avatar,
    subjects = subjects.map { it.uid },
    employee = employee.map { it.uid },
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
    hide = isHide,
)

fun OrganizationPojo.mapToDetailsData(
    subjects: List<SubjectDetails>,
    employee: List<EmployeeDetails>,
) = OrganizationDetails(
    uid = uid,
    isMain = main,
    shortName = shortName,
    fullName = fullName,
    type = type,
    avatar = avatar,
    subjects = subjects,
    employee = employee,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
    isHide = hide,
)