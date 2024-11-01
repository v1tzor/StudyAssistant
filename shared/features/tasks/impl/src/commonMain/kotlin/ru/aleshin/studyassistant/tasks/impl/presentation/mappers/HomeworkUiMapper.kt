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

package ru.aleshin.studyassistant.tasks.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkTaskComponent
import ru.aleshin.studyassistant.tasks.impl.domain.entities.HomeworkErrors
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkErrorsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTaskComponentUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.convertToDetails

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
internal fun Homework.mapToUi() = HomeworkUi(
    uid = uid,
    classId = classId,
    deadline = deadline,
    subject = subject?.mapToUi(),
    organization = organization.mapToUi(),
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentationTasks,
    test = test,
    priority = priority,
    isDone = isDone,
    completeDate = completeDate,
)

internal fun HomeworkUi.mapToDomain() = Homework(
    uid = uid,
    classId = classId,
    deadline = deadline,
    subject = subject?.mapToDomain(),
    organization = organization.mapToDomain(),
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentationTasks,
    test = test,
    priority = priority,
    isDone = isDone,
    completeDate = completeDate,
)

internal fun HomeworkErrors.mapToUi() = HomeworkErrorsUi(
    overdueTasks = overdueTasks.map { it.mapToUi().convertToDetails(HomeworkStatus.NOT_COMPLETE) },
    detachedActiveTasks = detachedActiveTasks.map { it.mapToUi().convertToDetails(HomeworkStatus.WAIT) },
)
internal fun HomeworkTaskComponent.mapToUi() = when (this) {
    is HomeworkTaskComponent.Label -> HomeworkTaskComponentUi.Label(text)
    is HomeworkTaskComponent.Tasks -> HomeworkTaskComponentUi.Tasks(taskList)
}

internal fun HomeworkTaskComponentUi.mapToDomain() = when (this) {
    is HomeworkTaskComponentUi.Label -> HomeworkTaskComponent.Label(text)
    is HomeworkTaskComponentUi.Tasks -> HomeworkTaskComponent.Tasks(taskList)
}