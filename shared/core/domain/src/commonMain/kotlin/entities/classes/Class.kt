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

package entities.classes

import entities.common.ContactInfo
import entities.employee.Employee
import entities.organizations.OrganizationShort
import entities.subject.EventType
import entities.subject.Subject
import functional.TimeRange
import functional.UID

/**
 * @author Stanislav Aleshin on 01.05.2024.
 */
data class Class(
    val uid: UID,
    val scheduleId: UID,
    val organization: OrganizationShort,
    val eventType: EventType,
    val subject: Subject?,
    val customData: String? = null,
    val teacher: Employee?,
    val office: Int,
    val location: ContactInfo,
    val timeRange: TimeRange,
    val notification: Boolean = false,
)
