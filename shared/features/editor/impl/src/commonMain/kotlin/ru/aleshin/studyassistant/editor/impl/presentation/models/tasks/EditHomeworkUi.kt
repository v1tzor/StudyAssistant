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

package ru.aleshin.studyassistant.editor.impl.presentation.models.tasks

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi

/**
 * @author Stanislav Aleshin on 22.06.2024.
 */
@Immutable
@Serializable
internal data class EditHomeworkUi(
    val uid: UID,
    val classId: UID? = null,
    val deadline: Instant? = null,
    val subject: SubjectUi? = null,
    val organization: OrganizationShortUi? = null,
    val theoreticalTasks: String = "",
    val practicalTasks: String = "",
    val presentationTasks: String = "",
    val isTest: Boolean = false,
    val testTopic: String = "",
    val priority: TaskPriority = TaskPriority.STANDARD,
    val isDone: Boolean = false,
    val completeDate: Instant? = null,
) {

    fun isValid() = deadline != null && subject != null && organization != null &&
        (theoreticalTasks.isNotEmpty() || practicalTasks.isNotEmpty() || presentationTasks.isNotEmpty())

    companion object {
        fun createEditModel(
            uid: UID?,
            organization: OrganizationShortUi? = null,
            deadline: Instant? = null,
            subject: SubjectUi? = null,
        ) = EditHomeworkUi(
            uid = uid ?: "",
            organization = organization,
            deadline = deadline,
            subject = subject,
        )
    }
}

internal fun HomeworkUi.convertToEdit() = EditHomeworkUi(
    uid = uid,
    classId = classId,
    deadline = deadline,
    subject = subject,
    organization = organization,
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentationTasks,
    testTopic = test ?: "",
    isTest = test != null,
    priority = priority,
    isDone = isDone,
    completeDate = completeDate,
)

internal fun EditHomeworkUi.convertToBase() = HomeworkUi(
    uid = uid,
    classId = classId,
    deadline = checkNotNull(deadline),
    subject = subject,
    organization = checkNotNull(organization),
    theoreticalTasks = theoreticalTasks,
    practicalTasks = practicalTasks,
    presentationTasks = presentationTasks,
    test = if (isTest) testTopic else null,
    priority = priority,
    isDone = isDone,
    completeDate = completeDate,
)