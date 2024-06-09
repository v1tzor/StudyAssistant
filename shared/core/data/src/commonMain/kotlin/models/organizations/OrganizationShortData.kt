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

package models.organizations

import entities.organizations.OrganizationType
import functional.UID
import kotlinx.serialization.Serializable
import models.users.ContactInfoData

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
@Serializable
data class OrganizationShortData(
    val uid: UID = "",
    val main: Boolean = false,
    val shortName: String = "",
    val type: String = OrganizationType.SCHOOL.toString(),
    val avatar: String? = null,
    val locations: List<ContactInfoData> = emptyList(),
    val offices: List<String> = emptyList(),
    val scheduleTimeIntervals: ScheduleTimeIntervalsData = ScheduleTimeIntervalsData(),
)
