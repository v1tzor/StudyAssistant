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

package ru.aleshin.studyassistant.core.remote.models.subjects

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.remote.models.users.ContactInfoPojo

/**
 * @author Stanislav Aleshin on 29.04.2024.
 */
@Serializable
data class MediatedSubjectPojo(
    val uid: UID = "",
    val organizationId: UID = "",
    val eventType: String = EventType.LESSON.toString(),
    val name: String = "",
    val teacherId: UID? = null,
    val office: String = "",
    val color: Int = 0,
    val location: ContactInfoPojo? = null,
)