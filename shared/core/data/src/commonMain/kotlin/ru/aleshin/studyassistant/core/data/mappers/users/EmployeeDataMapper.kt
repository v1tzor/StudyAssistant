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

package ru.aleshin.studyassistant.core.data.mappers.users

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.database.models.employee.BaseEmployeeEntity
import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
fun Employee.mapToLocalData() = BaseEmployeeEntity(
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
    emails = emails.map { it.mapToLocalData().toJson() },
    phones = phones.map { it.mapToLocalData().toJson() },
    locations = locations.map { it.mapToLocalData().toJson() },
    webs = webs.map { it.mapToLocalData().toJson() },
    updatedAt = updatedAt,
)

fun BaseEmployeeEntity.mapToDomain() = Employee(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = EmployeePost.valueOf(post),
    avatar = avatar,
    birthday = birthday,
    workTime = workTimeStart?.let { startTime ->
        workTimeEnd?.let { endTime ->
            TimeRange(startTime.mapEpochTimeToInstant(), endTime.mapEpochTimeToInstant())
        }
    },
    emails = emails.map { it.fromJson<ContactInfoEntity>().mapToDomain() },
    phones = phones.map { it.fromJson<ContactInfoEntity>().mapToDomain() },
    locations = locations.map { it.fromJson<ContactInfoEntity>().mapToDomain() },
    webs = webs.map { it.fromJson<ContactInfoEntity>().mapToDomain() },
    updatedAt = updatedAt,
)
