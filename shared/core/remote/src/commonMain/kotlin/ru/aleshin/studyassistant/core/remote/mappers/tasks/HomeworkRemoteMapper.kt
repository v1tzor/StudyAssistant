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

package ru.aleshin.studyassistant.core.remote.mappers.tasks

import ru.aleshin.studyassistant.core.remote.models.organizations.OrganizationShortPojo
import ru.aleshin.studyassistant.core.remote.models.subjects.SubjectDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.HomeworkDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.tasks.HomeworkPojo

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun HomeworkDetailsPojo.mapToBase() = HomeworkPojo(
    uid = uid,
    userId = userId,
    classId = classId,
    deadline = deadline,
    subjectId = subject?.uid,
    organizationId = organization.uid,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentations = presentations,
    test = test,
    priority = priority,
    done = isDone,
    completeDate = completeDate,
)

fun HomeworkPojo.mapToDetails(
    organization: OrganizationShortPojo,
    subject: SubjectDetailsPojo?,
) = HomeworkDetailsPojo(
    uid = uid,
    userId = userId,
    classId = classId,
    deadline = deadline,
    subject = subject,
    organization = organization,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentations = presentations,
    test = test,
    priority = priority,
    isDone = done,
    completeDate = completeDate,
)