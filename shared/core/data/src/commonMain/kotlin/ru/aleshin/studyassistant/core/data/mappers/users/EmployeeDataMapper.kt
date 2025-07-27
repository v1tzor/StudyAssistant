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

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.utils.sync.MultipleSyncMapper
import ru.aleshin.studyassistant.core.database.models.employee.BaseEmployeeEntity
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.domain.entities.employee.MediatedEmployee
import ru.aleshin.studyassistant.core.remote.models.users.ContactInfoPojo
import ru.aleshin.studyassistant.core.remote.models.users.EmployeePojo
import ru.aleshin.studyassistant.core.remote.models.users.MediatedEmployeePojo

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */

// Remote

fun Employee.mapToRemoteData(userId: UID) = EmployeePojo(
    id = uid,
    userId = userId,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post.name,
    avatar = avatar,
    birthday = birthday,
    workTimeStart = workTime?.from?.toEpochMilliseconds(),
    workTimeEnd = workTime?.to?.toEpochMilliseconds(),
    emails = emails.map { it.mapToRemoteData().toJson() },
    phones = phones.map { it.mapToRemoteData().toJson() },
    locations = locations.map { it.mapToRemoteData().toJson() },
    webs = webs.map { it.mapToRemoteData().toJson() },
    updatedAt = updatedAt,
)

fun MediatedEmployee.mapToRemoteData() = MediatedEmployeePojo(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post.name,
    birthday = birthday,
    workTimeStart = workTime?.from?.toEpochMilliseconds(),
    workTimeEnd = workTime?.to?.toEpochMilliseconds(),
    emails = emails.map { it.mapToRemoteData() },
    phones = phones.map { it.mapToRemoteData() },
    locations = locations.map { it.mapToRemoteData() },
    webs = webs.map { it.mapToRemoteData() },
)

fun EmployeePojo.mapToDomain() = Employee(
    uid = id,
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
    emails = emails.map { it.fromJson<ContactInfoPojo>().mapToDomain() },
    phones = phones.map { it.fromJson<ContactInfoPojo>().mapToDomain() },
    locations = locations.map { it.fromJson<ContactInfoPojo>().mapToDomain() },
    webs = webs.map { it.fromJson<ContactInfoPojo>().mapToDomain() },
    updatedAt = updatedAt,
)

fun MediatedEmployeePojo.mapToDomain() = MediatedEmployee(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = EmployeePost.valueOf(post),
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

// Local

fun Employee.mapToLocalData() = BaseEmployeeEntity(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post.toString(),
    avatar = avatar,
    birthday = birthday,
    workTimeStart = workTime?.from?.toEpochMilliseconds(),
    workTimeEnd = workTime?.to?.toEpochMilliseconds(),
    emails = emails.map { it.mapToLocalData().toJson() },
    phones = phones.map { it.mapToLocalData().toJson() },
    locations = locations.map { it.mapToLocalData().toJson() },
    webs = webs.map { it.mapToLocalData().toJson() },
    updatedAt = updatedAt,
    isCacheData = 0L,
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
    workTime = if (workTimeStart != null && workTimeEnd != null) {
        TimeRange(
            from = workTimeStart!!.mapEpochTimeToInstant(),
            to = workTimeEnd!!.mapEpochTimeToInstant(),
        )
    } else {
        null
    },
    emails = emails.map { it.fromJson<ContactInfoPojo>().mapToDomain() },
    phones = phones.map { it.fromJson<ContactInfoPojo>().mapToDomain() },
    locations = locations.map { it.fromJson<ContactInfoPojo>().mapToDomain() },
    webs = webs.map { it.fromJson<ContactInfoPojo>().mapToDomain() },
    updatedAt = updatedAt,
)

// Combined

fun BaseEmployeeEntity.convertToRemote(userId: String) = EmployeePojo(
    id = uid,
    userId = userId,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post,
    avatar = avatar,
    birthday = birthday,
    workTimeStart = workTimeStart,
    workTimeEnd = workTimeEnd,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
    updatedAt = updatedAt,
)

fun EmployeePojo.convertToLocal() = BaseEmployeeEntity(
    uid = id,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post,
    avatar = avatar,
    birthday = birthday,
    workTimeStart = workTimeStart,
    workTimeEnd = workTimeEnd,
    emails = emails,
    phones = phones,
    locations = locations,
    webs = webs,
    updatedAt = updatedAt,
    isCacheData = 1L,
)

class EmployeeSyncMapper : MultipleSyncMapper<BaseEmployeeEntity, EmployeePojo>(
    localToRemote = { userId -> convertToRemote(userId) },
    remoteToLocal = { convertToLocal() },
)