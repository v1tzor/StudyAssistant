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

package ru.aleshin.studyassistant.core.domain.entities.tasks

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.organizations.OrganizationShort
import ru.aleshin.studyassistant.core.domain.entities.subject.Subject

/**
 * @author Stanislav Aleshin on 03.06.2025.
 */
data class HomeworkDetails(
    val uid: UID,
    val classId: UID? = null,
    val deadline: Instant,
    val subject: Subject? = null,
    val organization: OrganizationShort,
    val theoreticalTasks: HomeworkTasksDetails,
    val practicalTasks: HomeworkTasksDetails,
    val presentationTasks: HomeworkTasksDetails,
    val test: String? = null,
    val priority: TaskPriority = TaskPriority.STANDARD,
    val isDone: Boolean = false,
    val status: HomeworkStatus,
    val completeDate: Instant?,
)

fun Homework.convertToDetails(status: HomeworkStatus) = HomeworkDetails(
    uid = uid,
    classId = classId,
    deadline = deadline,
    subject = subject,
    organization = organization,
    theoreticalTasks = HomeworkTasksDetails(
        origin = theoreticalTasks,
        components = theoreticalTasks.toHomeworkComponents()
    ),
    practicalTasks = HomeworkTasksDetails(
        origin = practicalTasks,
        components = practicalTasks.toHomeworkComponents()
    ),
    presentationTasks = HomeworkTasksDetails(
        origin = presentationTasks,
        components = presentationTasks.toHomeworkComponents()
    ),
    test = test,
    priority = priority,
    isDone = isDone,
    status = status,
    completeDate = completeDate,
)