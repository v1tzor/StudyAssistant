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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes

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
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeDetailsUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.store.ClassComponent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.ClassTopBar
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.TimeInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.LocationInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.OrganizationInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.SubjectAndEventTypeInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.TeacherInfoField

/**
 * @author Stanislav Aleshin on 02.06.2024
 */
@Composable
internal fun ClassContent(
    classComponent: ClassComponent,
    modifier: Modifier = Modifier,
) {
    val store = classComponent.store
    val state by store.stateAsState()
    val strings = EditorThemeRes.strings
    val coreStrings = StudyAssistantRes.strings
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseClassContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onAddOrganization = {
                    store.dispatchEvent(ClassEvent.NavigateToOrganizationEditor(null))
                },
                onAddSubject = {
                    store.dispatchEvent(ClassEvent.NavigateToSubjectEditor(null))
                },
                onAddTeacher = {
                    store.dispatchEvent(ClassEvent.NavigateToEmployeeEditor(null))
                },
                onEditSubject = {
                    store.dispatchEvent(ClassEvent.NavigateToSubjectEditor(it.uid))
                },
                onEditEmployee = {
                    store.dispatchEvent(ClassEvent.NavigateToEmployeeEditor(it.uid))
                },
                onUpdateLocations = {
                    store.dispatchEvent(ClassEvent.UpdateOrganizationLocations(it))
                },
                onUpdateOffices = {
                    store.dispatchEvent(ClassEvent.UpdateOrganizationOffices(it))
                },
                onSelectOrganization = {
                    store.dispatchEvent(ClassEvent.UpdateOrganization(it))
                },
                onSelectTeacher = {
                    store.dispatchEvent(ClassEvent.UpdateTeacher(it))
                },
                onSelectSubject = { type, subject ->
                    store.dispatchEvent(ClassEvent.UpdateSubject(type, subject))
                },
                onSelectLocation = { location, office ->
                    store.dispatchEvent(ClassEvent.UpdateLocation(location, office))
                },
                onSelectTime = { start, end ->
                    store.dispatchEvent(ClassEvent.UpdateTime(start, end))
                },
            )
        },
        topBar = {
            ClassTopBar(
                enabledSave = state.editableClass?.isValid() ?: false,
                onSaveClick = { store.dispatchEvent(ClassEvent.SaveClass) },
                onBackClick = { store.dispatchEvent(ClassEvent.NavigateToBack) },
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
            is ClassEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings, coreStrings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun BaseClassContent(
    state: ClassState,
    modifier: Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onAddOrganization: () -> Unit,
    onAddSubject: () -> Unit,
    onAddTeacher: () -> Unit,
    onEditSubject: (SubjectUi) -> Unit,
    onEditEmployee: (EmployeeDetailsUi) -> Unit,
    onUpdateLocations: (List<ContactInfoUi>) -> Unit,
    onUpdateOffices: (List<String>) -> Unit,
    onSelectOrganization: (OrganizationShortUi?) -> Unit,
    onSelectSubject: (EventType?, SubjectUi?) -> Unit,
    onSelectTeacher: (EmployeeDetailsUi?) -> Unit,
    onSelectLocation: (ContactInfoUi?, String?) -> Unit,
    onSelectTime: (Instant?, Instant?) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(top = 20.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        val selectedOrganization = remember(state.organizations, state.editableClass?.organization) {
            state.organizations.find { it.uid == state.editableClass?.organization?.uid }
        }
        OrganizationInfoField(
            isLoading = state.isLoading,
            organization = selectedOrganization,
            allOrganization = state.organizations,
            onAddOrganization = onAddOrganization,
            onSelected = onSelectOrganization,
        )
        SubjectAndEventTypeInfoField(
            enabledAddSubject = state.editableClass?.organization != null,
            isLoading = state.isLoading,
            subject = state.editableClass?.subject,
            eventType = state.editableClass?.eventType,
            allSubjects = state.subjects,
            onAddSubject = onAddSubject,
            onEditSubject = onEditSubject,
            onSelectedEventType = { onSelectSubject(it, state.editableClass?.subject) },
            onSelectedSubject = { onSelectSubject(it?.eventType, it) },
        )
        TeacherInfoField(
            enabledAddTeacher = state.editableClass?.organization != null,
            isLoading = state.isLoading,
            teacher = state.editableClass?.teacher,
            allEmployee = state.employees,
            onAddTeacher = onAddTeacher,
            onEditTeacher = onEditEmployee,
            onSelected = onSelectTeacher,
        )
        LocationInfoField(
            enabledAdd = state.editableClass?.organization != null,
            isLoading = state.isLoading,
            location = state.editableClass?.location,
            office = state.editableClass?.office,
            allLocations = selectedOrganization?.locations ?: emptyList(),
            allOffices = selectedOrganization?.offices ?: emptyList(),
            onUpdateOffices = onUpdateOffices,
            onUpdateLocations = onUpdateLocations,
            onSelectedLocation = { onSelectLocation(it, state.editableClass?.office) },
            onSelectedOffice = { onSelectLocation(state.editableClass?.location, it) }
        )
        TimeInfoField(
            isLoading = state.isLoading,
            startTime = state.editableClass?.startTime,
            endTime = state.editableClass?.endTime,
            freeClassTimeRanges = state.freeClassTimeRanges,
            onSelectedTime = onSelectTime,
        )
    }
}