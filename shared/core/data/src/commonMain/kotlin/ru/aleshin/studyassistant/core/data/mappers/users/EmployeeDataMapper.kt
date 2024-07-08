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

package ru.aleshin.studyassistant.core.data.mappers.users

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeEntity

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
fun EmployeePojo.mapToDomain() = Employee(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = EmployeePost.valueOf(post),
    avatar = avatar,
    birthday = birthday,
    workTime = if (workTimeStart != null && workTimeEnd != null) {
        TimeRange(
            from = workTimeStart!!.mapEpochTimeToInstant(),
            to = workTimeEnd!!.mapEpochTimeToInstant(),
        )
    } else {
        null
    },
    emails = emails.map { it.mapToDomain() },
    phones = phones.map { it.mapToDomain() },
    locations = locations.map { it.mapToDomain() },
    webs = webs.map { it.mapToDomain() },
)

fun EmployeeEntity.mapToDomain() = Employee(
    uid = uid,
    organizationId = organization_id,
    firstName = first_name,
    secondName = second_name,
    patronymic = patronymic,
    post = EmployeePost.valueOf(post),
    avatar = avatar,
    birthday = birthday,
    workTime = if (workTimeStart != null && workTimeEnd != null) {
        TimeRange(
            from = workTimeStart!!.mapEpochTimeToInstant(),
            to = workTimeEnd!!.mapEpochTimeToInstant(),
        )
    } else {
        null
    },
    emails = emails.map { Json.decodeFromString(it) },
    phones = phones.map { Json.decodeFromString(it) },
    locations = locations.map { Json.decodeFromString(it) },
    webs = webs.map { Json.decodeFromString(it) },
)

fun Employee.mapToRemoteData() = EmployeePojo(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post.name,
    avatar = avatar,
    birthday = birthday,
    workTimeStart = workTime?.from?.toEpochMilliseconds(),
    workTimeEnd = workTime?.to?.toEpochMilliseconds(),
    emails = emails.map { it.mapToRemoteData() },
    phones = phones.map { it.mapToRemoteData() },
    locations = locations.map { it.mapToRemoteData() },
    webs = webs.map { it.mapToRemoteData() },
)

fun Employee.mapToLocalData() = EmployeeEntity(
    uid = uid,
    organization_id = organizationId,
    first_name = firstName,
    second_name = secondName,
    patronymic = patronymic,
    post = post.toString(),
    avatar = avatar,
    birthday = birthday,
    workTimeStart = workTime?.from?.toEpochMilliseconds(),
    workTimeEnd = workTime?.to?.toEpochMilliseconds(),
    emails = emails.map { Json.encodeToString(it) },
    phones = phones.map { Json.encodeToString(it) },
    locations = locations.map { Json.encodeToString(it) },
    webs = webs.map { Json.encodeToString(it) },
)