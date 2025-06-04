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

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.InstantParceler
import ru.aleshin.studyassistant.core.common.platform.NullInstantParceler
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.domain.entities.tasks.toHomeworkComponents
import ru.aleshin.studyassistant.tasks.impl.presentation.mappers.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
@Parcelize
internal data class HomeworkUi(
    val uid: UID,
    val classId: UID? = null,
    @TypeParceler<Instant, InstantParceler>
    val deadline: Instant,
    val subject: SubjectUi? = null,
    val organization: OrganizationShortUi,
    val theoreticalTasks: String = "",
    val practicalTasks: String = "",
    val presentationTasks: String = "",
    val test: String? = null,
    val priority: TaskPriority = TaskPriority.STANDARD,
    val isDone: Boolean = false,
    @TypeParceler<Instant?, NullInstantParceler>
    val completeDate: Instant?,
) : Parcelable

internal fun HomeworkUi.convertToDetails(status: HomeworkStatus) = HomeworkDetailsUi(
    uid = uid,
    classId = classId,
    deadline = deadline,
    subject = subject,
    organization = organization,
    theoreticalTasks = HomeworkTasksDetailsUi(
        origin = theoreticalTasks,
        components = theoreticalTasks.toHomeworkComponents().map { it.mapToUi() }
    ),
    practicalTasks = HomeworkTasksDetailsUi(
        origin = practicalTasks,
        components = practicalTasks.toHomeworkComponents().map { it.mapToUi() }
    ),
    presentationTasks = HomeworkTasksDetailsUi(
        origin = presentationTasks,
        components = presentationTasks.toHomeworkComponents().map { it.mapToUi() }
    ),
    test = test,
    priority = priority,
    isDone = isDone,
    status = status,
    completeDate = completeDate,
)

internal fun HomeworkDetailsUi.convertToBase() = HomeworkUi(
    uid = uid,
    classId = classId,
    deadline = deadline,
    subject = subject,
    organization = organization,
    theoreticalTasks = theoreticalTasks.origin,
    practicalTasks = practicalTasks.origin,
    presentationTasks = presentationTasks.origin,
    test = test,
    priority = priority,
    isDone = isDone,
    completeDate = completeDate,
)