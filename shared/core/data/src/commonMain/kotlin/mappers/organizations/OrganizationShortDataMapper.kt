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

package mappers.organizations

import entities.organizations.OrganizationShort
import entities.organizations.OrganizationType
import mappers.mapToData
import mappers.mapToDomain
import models.organizations.OrganizationShortData

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun OrganizationShort.mapToData() = OrganizationShortData(
    uid = uid,
    main = isMain,
    shortName = shortName,
    type = type.name,
    avatar = avatar,
    locations = locations.map { it.mapToData() },
    offices = offices,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToDate(),
)

fun OrganizationShortData.mapToDomain() = OrganizationShort(
    uid = uid,
    isMain = main,
    shortName = shortName,
    type = OrganizationType.valueOf(type),
    locations = locations.map { it.mapToDomain() },
    offices = offices,
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToDomain(),
)