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

import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.database.models.employee.BaseEmployeeEntity
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity
import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity

/**
 * @author Stanislav Aleshin on 30.04.2024.
 */
data class OrganizationDetailsEntity(
    val uid: UID,
    val isMain: Boolean,
    val shortName: String,
    val fullName: String? = null,
    val type: String,
    val avatar: String? = null,
    val scheduleTimeIntervals: ScheduleTimeIntervalsEntity,
    val subjects: List<SubjectDetailsEntity> = emptyList(),
    val employee: List<BaseEmployeeEntity> = emptyList(),
    val emails: List<ContactInfoEntity> = emptyList(),
    val phones: List<ContactInfoEntity> = emptyList(),
    val locations: List<ContactInfoEntity> = emptyList(),
    val webs: List<ContactInfoEntity> = emptyList(),
    val offices: List<String> = emptyList(),
    val isHide: Boolean = false,
    val updatedAt: Long,
)