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

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalData
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteData
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType
import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationShortPojo
import ru.aleshin.studyassistant.core.remote.models.organizations.ScheduleTimeIntervalsPojo
import ru.aleshin.studyassistant.core.remote.models.users.ContactInfoPojo

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */

// Remote

fun OrganizationShort.mapToRemoteData() = OrganizationShortPojo(
    uid = uid,
    main = isMain,
    shortName = shortName,
    type = type.name,
    avatar = avatar,
    locations = locations.map { it.mapToRemoteData().toJson() },
    offices = offices,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToRemoteDate().toJson(),
    updatedAt = updatedAt,
)

fun OrganizationShortPojo.mapToDomain() = OrganizationShort(
    uid = uid,
    isMain = main,
    shortName = shortName,
    type = OrganizationType.valueOf(type),
    locations = locations.map { it.fromJson<ContactInfoPojo>().mapToDomain() },
    offices = offices,
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals.fromJson<ScheduleTimeIntervalsPojo>().mapToDomain(),
    updatedAt = updatedAt,
)

// Local

fun OrganizationShortEntity.mapToDomain() = OrganizationShort(
    uid = uid,
    isMain = main,
    shortName = shortName,
    type = OrganizationType.valueOf(type),
    locations = locations.map { it.mapToDomain() },
    offices = offices,
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToDomain(),
    updatedAt = updatedAt,
)

fun OrganizationShort.mapToLocalData() = OrganizationShortEntity(
    uid = uid,
    main = isMain,
    shortName = shortName,
    type = type.name,
    avatar = avatar,
    locations = locations.map { it.mapToLocalData() },
    offices = offices,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToLocalDate(),
    updatedAt = updatedAt,
)