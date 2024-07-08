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

package ru.aleshin.studyassistant.core.database.models.tasks

import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
data class HomeworkDetailsEntity(
    val uid: UID,
    val classId: UID? = null,
    val deadline: Long,
    val subject: SubjectDetailsEntity? = null,
    val organization: OrganizationShortEntity,
    val theoreticalTasks: String = "",
    val practicalTasks: String = "",
    val presentations: String = "",
    val test: String? = null,
    val priority: String,
    val isDone: Boolean = false,
    val completeDate: Long? = null,
)