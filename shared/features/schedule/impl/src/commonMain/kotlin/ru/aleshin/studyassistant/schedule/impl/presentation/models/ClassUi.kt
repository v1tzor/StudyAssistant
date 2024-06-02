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

package ru.aleshin.studyassistant.schedule.impl.presentation.models

import entities.subject.EventType
import functional.TimeRange
import functional.UID
import platform.JavaSerializable

/**
 * @author Stanislav Aleshin on 05.05.2024.
 */
internal data class ClassUi(
    val uid: UID,
    val scheduleId: UID,
    val organization: OrganizationShortUi,
    val eventType: EventType,
    val subject: SubjectUi?,
    val customData: String? = null,
    val teacher: EmployeeUi?,
    val office: Int,
    val location: ContactInfoUi,
    val timeRange: TimeRange,
    val notification: Boolean = false,
) : JavaSerializable
