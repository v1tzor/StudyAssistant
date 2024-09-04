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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.homework

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.OrganizationInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.SubjectInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.TaskPriorityInfoView
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views.HomeworkTasksFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views.HomeworkTestInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views.LinkedClassInfoField

/**
 * @author Stanislav Aleshin on 23.06.2024
 */
@Composable
internal fun HomeworkContent(
    state: HomeworkViewState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onAddOrganization: () -> Unit,
    onAddSubject: () -> Unit,
    onEditSubject: (SubjectUi) -> Unit,
    onSelectedOrganization: (OrganizationShortUi?) -> Unit,
    onSelectedSubject: (SubjectUi?) -> Unit,
    onSelectedDate: (Instant?) -> Unit,
    onSelectedClass: (ClassUi?, Instant?) -> Unit,
    onTaskChange: (theory: String, practice: String, presentations: String) -> Unit,
    onTestChange: (isTest: Boolean, topic: String) -> Unit,
    onChangePriority: (TaskPriority) -> Unit,
) = with(state) {
    Column(
        modifier = modifier.padding(top = 16.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        OrganizationInfoField(
            isLoading = isLoading,
            organization = editableHomework?.organization,
            allOrganization = organizations,
            onAddOrganization = onAddOrganization,
            onSelected = onSelectedOrganization,
        )
        SubjectInfoField(
            enabledAddSubject = editableHomework?.organization != null,
            isLoading = isLoading,
            subject = editableHomework?.subject,
            allSubjects = subjects,
            onAddSubject = onAddSubject,
            onEditSubject = onEditSubject,
            onSelectedSubject = onSelectedSubject,
        )
        LinkedClassInfoField(
            isLoading = isLoading || isClassesLoading,
            currentDate = currentDate,
            selectedDate = editableHomework?.deadline,
            linkedClass = editableHomework?.classId,
            classesForLinked = classesForLinking,
            onSelectedClass = onSelectedClass,
            onSelectedDate = onSelectedDate,
        )
        HomeworkTasksFields(
            isLoading = isLoading,
            theoreticalTasks = editableHomework?.theoreticalTasks ?: "",
            practicalTasks = editableHomework?.practicalTasks ?: "",
            presentationsTasks = editableHomework?.presentationTasks ?: "",
            onTaskChange = { theory, practice, presentations ->
                onTaskChange(theory, practice, presentations)
            }
        )
        HomeworkTestInfoField(
            isLoading = isLoading,
            testTopic = editableHomework?.testTopic ?: "",
            isTest = editableHomework?.isTest ?: false,
            onTestChange = { isTest, topic -> onTestChange(isTest, topic) },
        )
        TaskPriorityInfoView(
            isLoading = isLoading,
            priority = editableHomework?.priority,
            onChangePriority = onChangePriority,
        )
    }
}