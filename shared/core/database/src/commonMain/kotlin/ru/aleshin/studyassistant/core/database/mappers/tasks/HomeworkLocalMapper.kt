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

package ru.aleshin.studyassistant.core.database.mappers.tasks

import ru.aleshin.studyassistant.core.database.models.organizations.OrganizationShortEntity
import ru.aleshin.studyassistant.core.database.models.subjects.SubjectDetailsEntity
import ru.aleshin.studyassistant.core.database.models.tasks.HomeworkDetailsEntity
import ru.aleshin.studyassistant.sqldelight.tasks.FetchActiveAndLinkedHomeworks
import ru.aleshin.studyassistant.sqldelight.tasks.HomeworkEntity

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun HomeworkDetailsEntity.mapToBase() = HomeworkEntity(
    uid = uid,
    class_id = classId,
    deadline = deadline,
    subject_id = subject?.uid,
    organization_id = organization.uid,
    theoretical_tasks = theoreticalTasks,
    practical_tasks = practicalTasks,
    presentations = presentations,
    test = test,
    priority = priority,
    is_done = if (isDone) 1L else 0L,
    complete_date = completeDate,
)

fun HomeworkEntity.mapToDetails(
    organization: OrganizationShortEntity,
    subject: SubjectDetailsEntity?,
) = HomeworkDetailsEntity(
    uid = uid,
    classId = class_id,
    deadline = deadline,
    subject = subject,
    organization = organization,
    theoreticalTasks = theoretical_tasks,
    practicalTasks = practical_tasks,
    presentations = presentations,
    test = test,
    priority = priority,
    isDone = is_done == 1L,
    completeDate = complete_date,
)

fun FetchActiveAndLinkedHomeworks.mapToDetails(
    organization: OrganizationShortEntity,
    subject: SubjectDetailsEntity?,
) = HomeworkDetailsEntity(
    uid = uid,
    classId = class_id,
    deadline = deadline,
    subject = subject,
    organization = organization,
    theoreticalTasks = theoretical_tasks,
    practicalTasks = practical_tasks,
    presentations = presentations,
    test = test,
    priority = priority,
    isDone = is_done == 1L,
    completeDate = complete_date,
)