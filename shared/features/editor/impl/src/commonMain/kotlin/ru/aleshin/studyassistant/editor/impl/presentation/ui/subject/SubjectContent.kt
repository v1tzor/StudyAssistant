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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeDetailsUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.LocationInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.TeacherInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.contract.SubjectViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.views.ColorInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.views.EventTypeInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.views.SubjectNameInfoField

/**
 * @author Stanislav Aleshin on 05.06.2024
 */
@Composable
internal fun SubjectContent(
    state: SubjectViewState,
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
) = with(state) {
    Column(
        modifier = modifier.fillMaxSize().padding(top = 20.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            EventTypeInfoField(
                isLoading = isLoading,
                eventType = editableSubject?.eventType,
                onSelected = onSelectEventType,
            )
            SubjectNameInfoField(
                isLoading = isLoading,
                name = editableSubject?.name,
                onNameChange = onEditName,
            )
            ColorInfoField(
                isLoading = isLoading,
                color = editableSubject?.color,
                onSelected = onPickColor,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            TeacherInfoField(
                enabledAddTeacher = editableSubject?.organizationId != null,
                isLoading = isLoading,
                teacher = editableSubject?.teacher,
                allEmployee = employees,
                onAddTeacher = onAddTeacher,
                onEditTeacher = onEditTeacher,
                onSelected = onSelectTeacher,
            )
            LocationInfoField(
                enabledAdd = editableSubject?.organizationId != null,
                isLoading = isLoading,
                location = editableSubject?.location,
                office = editableSubject?.office,
                allLocations = organization?.locations ?: emptyList(),
                allOffices = organization?.offices ?: emptyList(),
                onUpdateOffices = onUpdateOffices,
                onUpdateLocations = onUpdateLocations,
                onSelectedLocation = { onSelectLocation(it, editableSubject?.office) },
                onSelectedOffice = { onSelectLocation(editableSubject?.location, it) }
            )
        }
    }
}