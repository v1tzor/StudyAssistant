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

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.domain.entities.employee.MediatedEmployee
import ru.aleshin.studyassistant.core.remote.models.users.MediatedEmployeePojo

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
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

fun MediatedEmployeePojo.mapToDomain() = MediatedEmployee(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = EmployeePost.valueOf(post),
    birthday = birthday,
    workTime = workTimeStart?.let { startTime ->
        workTimeEnd?.let { endTime ->
            TimeRange(startTime.mapEpochTimeToInstant(), endTime.mapEpochTimeToInstant())
        }
    },
    emails = emails.map { it.mapToDomain() },
    phones = phones.map { it.mapToDomain() },
    locations = locations.map { it.mapToDomain() },
    webs = webs.map { it.mapToDomain() },
)
