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

package ru.aleshin.studyassistant.core.database.models.organizations

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationType

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
@Serializable
data class OrganizationShortEntity(
    val uid: UID = "",
    val main: Boolean = false,
    val shortName: String = "",
    val type: String = OrganizationType.SCHOOL.toString(),
    val avatar: String? = null,
    val locations: List<ContactInfoEntity> = emptyList(),
    val offices: List<String> = emptyList(),
    val scheduleTimeIntervals: ScheduleTimeIntervalsEntity = ScheduleTimeIntervalsEntity(),
    val updatedAt: Long,
)