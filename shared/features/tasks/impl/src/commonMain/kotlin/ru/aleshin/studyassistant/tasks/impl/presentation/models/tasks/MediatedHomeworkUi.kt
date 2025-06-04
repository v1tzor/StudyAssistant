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
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.randomUUID
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.tasks.impl.presentation.models.organization.OrganizationShortUi

/**
 * @author Stanislav Aleshin on 18.07.2024.
 */
@Parcelize
internal data class MediatedHomeworkUi(
    val uid: UID,
    val subjectName: String,
    val theoreticalTasks: HomeworkTasksDetailsUi,
    val practicalTasks: HomeworkTasksDetailsUi,
    val presentationTasks: HomeworkTasksDetailsUi,
    val test: String? = null,
    val priority: TaskPriority = TaskPriority.STANDARD,
) : Parcelable

internal fun HomeworkDetailsUi.convertToMediated(
    uid: UID = randomUUID(),
) = MediatedHomeworkUi(
    uid = uid,
    subjectName = subject?.name ?: "*",
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentationTasks,
    test = test,
    priority = priority,
)

internal fun MediatedHomeworkUi.createHomework(
    date: Instant,
    organization: OrganizationShortUi,
    linkData: MediatedHomeworkLinkData,
) = HomeworkUi(
    uid = uid,
    classId = linkData.actualLinkedClass?.data?.uid,
    deadline = date.startThisDay(),
    subject = linkData.actualSubject,
    organization = organization,
    theoreticalTasks = theoreticalTasks.origin,
    practicalTasks = practicalTasks.origin,
    presentationTasks = presentationTasks.origin,
    test = test,
    priority = priority,
    isDone = false,
    completeDate = null,
)