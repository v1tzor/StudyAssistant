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

package ru.aleshin.studyassistant.editor.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.tasks.Homework
import ru.aleshin.studyassistant.editor.impl.presentation.models.tasks.HomeworkUi

/**
 * @author Stanislav Aleshin on 22.06.2024.
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
    updatedAt = updatedAt,
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
    updatedAt = updatedAt,
)