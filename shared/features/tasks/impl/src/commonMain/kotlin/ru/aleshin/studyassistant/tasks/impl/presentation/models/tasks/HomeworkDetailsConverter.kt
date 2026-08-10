/*
 * Copyright 2026 Stanislav Aleshin
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

import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.toHomeworkTasksDetails
import ru.aleshin.studyassistant.core.presentation.mappers.tasks.mapToUi
import ru.aleshin.studyassistant.core.presentation.models.tasks.HomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
internal fun HomeworkUi.convertToDetails(
    status: HomeworkStatus,
    linkedGoal: GoalShortUi? = null,
) = HomeworkDetailsUi(
    uid = uid,
    classId = classId,
    deadline = deadline,
    subject = subject,
    organization = organization,
    theoreticalTasks = theoreticalTasks.toHomeworkTasksDetails().mapToUi(),
    practicalTasks = practicalTasks.toHomeworkTasksDetails().mapToUi(),
    presentationTasks = presentationTasks.toHomeworkTasksDetails().mapToUi(),
    test = test,
    priority = priority,
    isDone = isDone,
    status = status,
    linkedGoal = linkedGoal,
    completeDate = completeDate,
    updatedAt = updatedAt,
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
    updatedAt = updatedAt,
)
