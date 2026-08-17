/*
 * Copyright 2026 Stanislav Aleshin
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

package ru.aleshin.studyassistant.schedule.impl.presentation.models.importing

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Serializable
internal data class ScheduleImportClassUi(
    val uid: UID,
    val repeatWeek: Int,
    val dayOfWeek: Int,
    val number: Int?,
    val startTime: String,
    val endTime: String,
    val subjectId: UID?,
    val teacherId: UID?,
    val office: String,
    val location: String?,
    val eventType: EventType?,
    val included: Boolean,
)
