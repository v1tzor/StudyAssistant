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

package ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
@Immutable
@Serializable
internal data class HomeworkDetailsUi(
    val uid: UID,
    val classId: UID? = null,
    val deadline: Instant,
    val subject: SubjectUi? = null,
    val organization: OrganizationShortUi,
    val theoreticalTasks: HomeworkTasksDetailsUi,
    val practicalTasks: HomeworkTasksDetailsUi,
    val presentationTasks: HomeworkTasksDetailsUi,
    val test: String? = null,
    val priority: TaskPriority = TaskPriority.STANDARD,
    val isDone: Boolean = false,
    val linkedGoal: GoalShortUi?,
    val status: HomeworkStatus,
    val completeDate: Instant?,
    val updatedAt: Long,
)