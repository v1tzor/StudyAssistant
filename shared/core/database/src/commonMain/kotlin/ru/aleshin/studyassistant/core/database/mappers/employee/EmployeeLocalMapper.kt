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

package ru.aleshin.studyassistant.core.database.mappers.employee

import ru.aleshin.studyassistant.core.database.models.employee.BaseEmployeeEntity
import ru.aleshin.studyassistant.sqldelight.employee.EmployeeEntity

/**
 * @author Stanislav Aleshin on 08.07.2024.
 */
fun EmployeeEntity.mapToBase() = BaseEmployeeEntity(
    uid = uid,
    organizationId = organization_id,
    firstName = first_name,
    secondName = second_name,
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
    updatedAt = updated_at,
    isCacheData = is_cache_data
)

fun BaseEmployeeEntity.mapToEntity() = EmployeeEntity(
    uid = uid,
    organization_id = organizationId,
    first_name = firstName,
    second_name = secondName,
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
    updated_at = updatedAt,
    is_cache_data = isCacheData,
)