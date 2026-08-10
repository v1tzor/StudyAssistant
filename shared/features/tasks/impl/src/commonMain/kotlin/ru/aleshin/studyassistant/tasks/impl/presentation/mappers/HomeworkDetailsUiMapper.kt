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

package ru.aleshin.studyassistant.tasks.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkDetails
import ru.aleshin.studyassistant.core.presentation.mappers.organizations.mapToUi
import ru.aleshin.studyassistant.core.presentation.mappers.subjects.mapToUi
import ru.aleshin.studyassistant.core.presentation.mappers.tasks.mapToUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
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
    updatedAt = updatedAt,
)
