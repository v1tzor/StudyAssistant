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

package ru.aleshin.studyassistant.core.database.models.classes

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.database.models.users.ContactInfoEntity
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType

/**
 * @author Stanislav Aleshin on 01.05.2024.
 */
@Serializable
data class ClassEntity(
    val uid: UID = "",
    val organizationId: UID = "",
    val eventType: String = EventType.CLASS.toString(),
    val subjectId: UID? = null,
    val customData: String? = null,
    val teacherId: UID? = null,
    val office: String = "",
    val location: ContactInfoEntity? = null,
    val startTime: Long = 0L,
    val endTime: Long = 0L,
    val notification: Boolean = false,
)