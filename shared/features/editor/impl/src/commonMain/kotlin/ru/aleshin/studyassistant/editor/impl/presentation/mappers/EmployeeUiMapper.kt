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

package ru.aleshin.studyassistant.editor.impl.presentation.mappers

import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.employee.Employee
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeeDetails
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeDetailsUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeUi

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal fun Employee.mapToUi() = EmployeeUi(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post,
    avatar = avatar,
    birthday = birthday,
    workTimeStart = workTime?.from,
    workTimeEnd = workTime?.to,
    emails = emails.map { it.mapToUi() },
    phones = phones.map { it.mapToUi() },
    locations = locations.map { it.mapToUi() },
    webs = webs.map { it.mapToUi() },
)

internal fun EmployeeDetails.mapToUi() = EmployeeDetailsUi(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post,
    subjects = subjects.map { it.mapToUi() },
    avatar = avatar,
    birthday = birthday,
    workTimeStart = workTime?.from,
    workTimeEnd = workTime?.to,
    emails = emails.map { it.mapToUi() },
    phones = phones.map { it.mapToUi() },
    locations = locations.map { it.mapToUi() },
    webs = webs.map { it.mapToUi() },
)

internal fun EmployeeUi.mapToDomain() = Employee(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post,
    avatar = avatar,
    birthday = birthday,
    workTime = if (workTimeStart != null && workTimeEnd != null) TimeRange(workTimeStart, workTimeEnd) else null,
    emails = emails.map { it.mapToDomain() },
    phones = phones.map { it.mapToDomain() },
    locations = locations.map { it.mapToDomain() },
    webs = webs.map { it.mapToDomain() },
)

internal fun EmployeeDetailsUi.mapToDomain() = EmployeeDetails(
    uid = uid,
    organizationId = organizationId,
    firstName = firstName,
    secondName = secondName,
    patronymic = patronymic,
    post = post,
    subjects = subjects.map { it.mapToDomain() },
    avatar = avatar,
    birthday = birthday,
    workTime = if (workTimeStart != null && workTimeEnd != null) TimeRange(workTimeStart, workTimeEnd) else null,
    emails = emails.map { it.mapToDomain() },
    phones = phones.map { it.mapToDomain() },
    locations = locations.map { it.mapToDomain() },
    webs = webs.map { it.mapToDomain() },
)