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

package ru.aleshin.studyassistant.tasks.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.tasks.impl.presentation.models.organization.OrganizationShortUi

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
internal fun OrganizationShort.mapToUi() = OrganizationShortUi(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    type = type,
    locations = locations.map { it.mapToUi() },
    offices = offices,
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToUi(),
    updatedAt = updatedAt,
)

internal fun OrganizationShortUi.mapToDomain() = OrganizationShort(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    type = type,
    locations = locations.map { it.mapToDomain() },
    offices = offices,
    avatar = avatar,
    scheduleTimeIntervals = scheduleTimeIntervals.mapToDomain(),
    updatedAt = updatedAt,
)