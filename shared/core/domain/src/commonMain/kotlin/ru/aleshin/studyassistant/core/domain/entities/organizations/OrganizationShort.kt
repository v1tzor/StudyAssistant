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

package ru.aleshin.studyassistant.core.domain.entities.organizations

import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.ContactInfo

/**
 * @author Stanislav Aleshin on 01.05.2024.
 */
data class OrganizationShort(
    val uid: UID,
    val isMain: Boolean = false,
    val shortName: String = "",
    val type: OrganizationType = OrganizationType.SCHOOL,
    val locations: List<ContactInfo> = emptyList(),
    val offices: List<String> = emptyList(),
    val scheduleTimeIntervals: ScheduleTimeIntervals = ScheduleTimeIntervals(),
    val avatar: String? = null,
)

fun Organization.convertToShort() = OrganizationShort(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    type = type,
    locations = locations,
    offices = offices,
    scheduleTimeIntervals = scheduleTimeIntervals,
    avatar = avatar,
)

fun OrganizationShort.convertToBase(base: Organization) = base.copy(
    uid = uid,
    isMain = isMain,
    shortName = shortName,
    type = type,
    locations = locations,
    offices = offices,
    scheduleTimeIntervals = scheduleTimeIntervals,
    avatar = avatar,
)