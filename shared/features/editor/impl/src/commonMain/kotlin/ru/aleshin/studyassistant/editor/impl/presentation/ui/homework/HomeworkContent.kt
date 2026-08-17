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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.homework

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.core.presentation.models.schedules.ClassUi
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.ui.EditorLayoutMode
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.EditorSplitPanes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.OrganizationInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.SubjectInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.TaskPriorityInfoView
import ru.aleshin.studyassistant.editor.impl.presentation.ui.fetchEditorLayoutMode
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.contarct.HomeworkState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.store.HomeworkComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views.HomeworkBottomActions
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views.HomeworkTasksFields
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views.HomeworkTestInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views.HomeworkTopBar
import ru.aleshin.studyassistant.editor.impl.presentation.ui.homework.views.LinkedClassInfoField

/**
 * @author Stanislav Aleshin on 23.06.2024
 */
@Composable
internal fun HomeworkContent(
    homeworkComponent: HomeworkComponent,
    modifier: Modifier = Modifier,
) {
    val store = homeworkComponent.store
    val state by store.stateAsState()
    val snackbarState = remember { SnackbarHostState() }
    val layoutMode = currentWindowAdaptiveInfoV2().fetchEditorLayoutMode()
    val isExpanded = layoutMode == EditorLayoutMode.EXPANDED

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            HomeworkLayout(
                state = state,
                modifier = Modifier.padding(paddingValues),
                layoutMode = layoutMode,
                onAddOrganization = {
                    store.dispatchEvent(
                        HomeworkEvent.NavigateToOrganizationEditor(
                            null
                        )
                    )
                },
                onAddSubject = { store.dispatchEvent(HomeworkEvent.NavigateToSubjectEditor(null)) },
                onEditSubject = { store.dispatchEvent(HomeworkEvent.NavigateToSubjectEditor(it.uid)) },
                onSelectedOrganization = { store.dispatchEvent(HomeworkEvent.UpdateOrganization(it)) },
                onSelectedSubject = { store.dispatchEvent(HomeworkEvent.UpdateSubject(it)) },
                onSelectedDate = { store.dispatchEvent(HomeworkEvent.UpdateDate(it)) },
                onSelectedClass = { classModel, date ->
                    store.dispatchEvent(HomeworkEvent.UpdateLinkedClass(classModel?.uid, date))
                },
                onTaskChange = { theory, practice, presentations ->
                    store.dispatchEvent(HomeworkEvent.UpdateTask(theory, practice, presentations))
                },
                onTestChange = { isTest, topic ->
                    store.dispatchEvent(HomeworkEvent.UpdateTestTopic(isTest, topic))
                },
                onChangePriority = { store.dispatchEvent(HomeworkEvent.UpdatePriority(it)) },
            )
        },
        topBar = {
            HomeworkTopBar(
                isExpanded = isExpanded,
                onBackClick = { store.dispatchEvent(HomeworkEvent.NavigateToBack) },
            )
        },
        bottomBar = {
            HomeworkBottomActions(
                isLoadingSave = state.isLoadingSave,
                saveEnabled = state.editableHomework?.isValid() == true,
                showDeleteAction = state.showDeleteAction,
                contentMaxWidth = if (isExpanded) AdaptiveLayoutDefaults.MediumContentMaxWidth else null,
                horizontalPadding = if (isExpanded) {
                    AdaptiveLayoutDefaults.ExpandedHorizontalPadding
                } else {
                    AdaptiveLayoutDefaults.CompactHorizontalPadding
                },
                onCancelClick = { store.dispatchEvent(HomeworkEvent.NavigateToBack) },
                onSaveClick = { store.dispatchEvent(HomeworkEvent.SaveHomework) },
                onDeleteClick = { store.dispatchEvent(HomeworkEvent.DeleteHomework) },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
    )

    store.handleEffects { effect ->
        when (effect) {
            is HomeworkEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun HomeworkLayout(
    state: HomeworkState,
    modifier: Modifier = Modifier,
    layoutMode: EditorLayoutMode,
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
) {
    when (layoutMode) {
        EditorLayoutMode.COMPACT -> BaseHomeworkContent(
            state = state,
            modifier = modifier,
            onAddOrganization = onAddOrganization,
            onAddSubject = onAddSubject,
            onEditSubject = onEditSubject,
            onSelectedOrganization = onSelectedOrganization,
            onSelectedSubject = onSelectedSubject,
            onSelectedDate = onSelectedDate,
            onSelectedClass = onSelectedClass,
            onTaskChange = onTaskChange,
            onTestChange = onTestChange,
            onChangePriority = onChangePriority,
        )
        EditorLayoutMode.EXPANDED -> HomeworkExpandedLayout(
            state = state,
            modifier = modifier,
            onAddOrganization = onAddOrganization,
            onAddSubject = onAddSubject,
            onEditSubject = onEditSubject,
            onSelectedOrganization = onSelectedOrganization,
            onSelectedSubject = onSelectedSubject,
            onSelectedDate = onSelectedDate,
            onSelectedClass = onSelectedClass,
            onTaskChange = onTaskChange,
            onTestChange = onTestChange,
            onChangePriority = onChangePriority,
        )
    }
}

@Composable
private fun HomeworkExpandedLayout(
    state: HomeworkState,
    modifier: Modifier = Modifier,
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
) {
    val editableHomework = state.editableHomework
    EditorSplitPanes(
        modifier = modifier,
        startPane = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                OrganizationInfoField(
                    isLoading = state.isLoading,
                    organization = editableHomework?.organization,
                    allOrganization = state.organizations,
                    onAddOrganization = onAddOrganization,
                    onSelected = onSelectedOrganization,
                )
                SubjectInfoField(
                    enabledAddSubject = state.editableHomework?.organization != null,
                    isLoading = state.isLoading,
                    subject = editableHomework?.subject,
                    allSubjects = state.subjects,
                    onAddSubject = onAddSubject,
                    onEditSubject = onEditSubject,
                    onSelectedSubject = onSelectedSubject,
                )
                LinkedClassInfoField(
                    isLoading = state.isClassesLoading,
                    currentDate = state.currentDate,
                    selectedDate = editableHomework?.deadline,
                    linkedClass = editableHomework?.classId,
                    classesForLinked = state.classesForLinking,
                    onSelectedClass = onSelectedClass,
                    onSelectedDate = onSelectedDate,
                )
            }
        },
        endPane = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                HomeworkTasksFields(
                    isLoading = state.isLoading,
                    theoreticalTasks = editableHomework?.theoreticalTasks ?: "",
                    practicalTasks = editableHomework?.practicalTasks ?: "",
                    presentationsTasks = editableHomework?.presentationTasks ?: "",
                    onTaskChange = onTaskChange,
                )
                HomeworkTestInfoField(
                    isLoading = state.isLoading,
                    testTopic = editableHomework?.testTopic ?: "",
                    isTest = editableHomework?.isTest ?: false,
                    onTestChange = onTestChange,
                )
                TaskPriorityInfoView(
                    isLoading = state.isLoading,
                    priority = editableHomework?.priority,
                    onChangePriority = onChangePriority,
                )
            }
        },
    )
}

@Composable
private fun BaseHomeworkContent(
    state: HomeworkState,
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
) {
    Column(
        modifier = modifier.verticalScroll(scrollState).padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        val editableHomework = state.editableHomework
        OrganizationInfoField(
            isLoading = state.isLoading,
            organization = editableHomework?.organization,
            allOrganization = state.organizations,
            onAddOrganization = onAddOrganization,
            onSelected = onSelectedOrganization,
        )
        SubjectInfoField(
            enabledAddSubject = state.editableHomework?.organization != null,
            isLoading = state.isLoading,
            subject = editableHomework?.subject,
            allSubjects = state.subjects,
            onAddSubject = onAddSubject,
            onEditSubject = onEditSubject,
            onSelectedSubject = onSelectedSubject,
        )
        LinkedClassInfoField(
            isLoading = state.isClassesLoading,
            currentDate = state.currentDate,
            selectedDate = editableHomework?.deadline,
            linkedClass = editableHomework?.classId,
            classesForLinked = state.classesForLinking,
            onSelectedClass = onSelectedClass,
            onSelectedDate = onSelectedDate,
        )
        HomeworkTasksFields(
            isLoading = state.isLoading,
            theoreticalTasks = editableHomework?.theoreticalTasks ?: "",
            practicalTasks = editableHomework?.practicalTasks ?: "",
            presentationsTasks = editableHomework?.presentationTasks ?: "",
            onTaskChange = { theory, practice, presentations ->
                onTaskChange(theory, practice, presentations)
            }
        )
        HomeworkTestInfoField(
            isLoading = state.isLoading,
            testTopic = editableHomework?.testTopic ?: "",
            isTest = editableHomework?.isTest ?: false,
            onTestChange = { isTest, topic -> onTestChange(isTest, topic) },
        )
        TaskPriorityInfoView(
            isLoading = state.isLoading,
            priority = editableHomework?.priority,
            onChangePriority = onChangePriority,
        )
        Spacer(modifier = Modifier.height(60.dp))
    }
}