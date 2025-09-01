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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.subject

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeDetailsUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.LocationInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.TeacherInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.store.SubjectComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.views.ColorInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.views.EventTypeInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.views.SubjectNameInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.views.SubjectTopBar

/**
 * @author Stanislav Aleshin on 05.06.2024
 */
@Composable
internal fun SubjectContent(
    subjectComponent: SubjectComponent,
    modifier: Modifier = Modifier,
) {
    val store = subjectComponent.store
    val state by store.stateAsState()
    val strings = EditorThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseSubjectContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onAddTeacher = {
                    store.dispatchEvent(SubjectEvent.NavigateToEmployeeEditor(null))
                },
                onEditTeacher = {
                    store.dispatchEvent(SubjectEvent.NavigateToEmployeeEditor(it.uid))
                },
                onUpdateLocations = {
                    store.dispatchEvent(SubjectEvent.UpdateOrganizationLocations(it))
                },
                onUpdateOffices = {
                    store.dispatchEvent(SubjectEvent.UpdateOrganizationOffices(it))
                },
                onSelectEventType = {
                    store.dispatchEvent(SubjectEvent.SelectEventType(it))
                },
                onPickColor = {
                    store.dispatchEvent(SubjectEvent.UpdateColor(it))
                },
                onSelectTeacher = {
                    store.dispatchEvent(SubjectEvent.UpdateTeacher(it))
                },
                onEditName = {
                    store.dispatchEvent(SubjectEvent.EditName(it))
                },
                onSelectLocation = { location, office ->
                    store.dispatchEvent(SubjectEvent.UpdateLocation(location, office))
                },
            )
        },
        topBar = {
            SubjectTopBar(
                enabledSave = state.editableSubject?.isValid() ?: false,
                onBackClick = { store.dispatchEvent(SubjectEvent.NavigateToBack) },
                onSaveClick = { store.dispatchEvent(SubjectEvent.SaveSubject) },
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
            is SubjectEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun BaseSubjectContent(
    state: SubjectState,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onAddTeacher: () -> Unit,
    onEditTeacher: (EmployeeDetailsUi) -> Unit,
    onUpdateLocations: (List<ContactInfoUi>) -> Unit,
    onUpdateOffices: (List<String>) -> Unit,
    onSelectEventType: (EventType?) -> Unit,
    onEditName: (String) -> Unit,
    onPickColor: (Int?) -> Unit,
    onSelectTeacher: (EmployeeDetailsUi?) -> Unit,
    onSelectLocation: (ContactInfoUi?, String?) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(scrollState).padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            EventTypeInfoField(
                isLoading = state.isLoading,
                eventType = state.editableSubject?.eventType,
                onSelected = onSelectEventType,
            )
            SubjectNameInfoField(
                isLoading = state.isLoading,
                name = state.editableSubject?.name,
                onNameChange = onEditName,
            )
            ColorInfoField(
                isLoading = state.isLoading,
                color = state.editableSubject?.color,
                onSelected = onPickColor,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TeacherInfoField(
                enabledAddTeacher = state.editableSubject?.organizationId != null,
                isLoading = state.isLoading,
                teacher = state.editableSubject?.teacher,
                allEmployee = state.employees,
                onAddTeacher = onAddTeacher,
                onEditTeacher = onEditTeacher,
                onSelected = onSelectTeacher,
            )
            LocationInfoField(
                enabledAdd = state.editableSubject?.organizationId != null,
                isLoading = state.isLoading,
                location = state.editableSubject?.location,
                office = state.editableSubject?.office,
                allLocations = state.organization?.locations ?: emptyList(),
                allOffices = state.organization?.offices ?: emptyList(),
                onUpdateOffices = onUpdateOffices,
                onUpdateLocations = onUpdateLocations,
                onSelectedLocation = { onSelectLocation(it, state.editableSubject?.office) },
                onSelectedOffice = { onSelectLocation(state.editableSubject?.location, it) }
            )
        }
    }
}