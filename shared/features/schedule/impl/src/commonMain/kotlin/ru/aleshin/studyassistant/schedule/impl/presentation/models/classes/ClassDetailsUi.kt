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

package ru.aleshin.studyassistant.schedule.impl.presentation.models.classes

import androidx.compose.runtime.Immutable
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.EmployeeUi

/**
 * @author Stanislav Aleshin on 05.05.2024.
 */
@Immutable
@Serializable
internal data class ClassDetailsUi(
    val uid: UID,
    val scheduleId: UID,
    val organization: OrganizationShortUi,
    val eventType: EventType,
    val subject: SubjectUi?,
    val customData: String? = null,
    val teacher: EmployeeUi?,
    val office: String,
    val location: ContactInfoUi?,
    val timeRange: TimeRange,
    val number: Int,
    val homework: HomeworkDetailsUi?,
)