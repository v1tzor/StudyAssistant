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

import ru.aleshin.studyassistant.core.domain.entities.tasks.DailyHomeworks
import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkDetails
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkScope
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkTaskComponent
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkTasksDetails
import ru.aleshin.studyassistant.tasks.impl.domain.entities.HomeworksCompleteProgress
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.DailyHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkScopeUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTaskComponentUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTasksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworksCompleteProgressUi

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

internal fun HomeworkDetails.mapToUi() = HomeworkDetailsUi(
    uid = uid,
    classId = classId,
    deadline = deadline,
    subject = subject?.mapToUi(),
    organization = organization.mapToUi(),
    theoreticalTasks = theoreticalTasks.mapToUi(),
    practicalTasks = practicalTasks.mapToUi(),
    presentationTasks = presentationTasks.mapToUi(),
    test = test,
    priority = priority,
    isDone = isDone,
    status = status,
    linkedGoal = linkedGoal?.mapToUi(),
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

internal fun DailyHomeworks.mapToUi() = DailyHomeworksUi(
    dailyStatus = dailyStatus,
    homeworks = homeworks.mapValues { entry -> entry.value.map { it.mapToUi() } },
)

internal fun HomeworksCompleteProgress.mapToUi() = HomeworksCompleteProgressUi(
    comingHomeworksExecution = comingHomeworksExecution,
    comingHomeworksProgress = comingHomeworksProgress,
    weekHomeworksExecution = weekHomeworksExecution,
    weekHomeworksProgress = weekHomeworksProgress,
    overdueTasks = overdueTasks.map { it.mapToUi() },
    detachedActiveTasks = detachedActiveTasks.map { it.mapToUi() },
    completedHomeworksCount = completedHomeworksCount,
)

internal fun HomeworkTasksDetails.mapToUi() = HomeworkTasksDetailsUi(
    origin = origin,
    components = components.map { it.mapToUi() }
)

internal fun HomeworkTaskComponent.mapToUi() = when (this) {
    is HomeworkTaskComponent.Label -> HomeworkTaskComponentUi.Label(text)
    is HomeworkTaskComponent.Tasks -> HomeworkTaskComponentUi.Tasks(taskList)
}

internal fun HomeworkTaskComponentUi.mapToDomain() = when (this) {
    is HomeworkTaskComponentUi.Label -> HomeworkTaskComponent.Label(text)
    is HomeworkTaskComponentUi.Tasks -> HomeworkTaskComponent.Tasks(taskList)
}

internal fun HomeworkScope.mapToUi() = HomeworkScopeUi(
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentationTasks,
)