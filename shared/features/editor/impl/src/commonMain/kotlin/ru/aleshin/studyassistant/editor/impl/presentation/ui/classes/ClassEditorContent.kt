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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import entities.subject.EventType
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.contract.ClassEditorViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.NotifyParameter
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.SubjectInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views.TimeInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.LocationInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.OrganizationInfoField
import ru.aleshin.studyassistant.editor.impl.presentation.ui.common.TeacherInfoField

/**
 * @author Stanislav Aleshin on 02.06.2024
 */
@Composable
internal fun ClassEditorContent(
    state: ClassEditorViewState,
    modifier: Modifier,
    scrollState: ScrollState = rememberScrollState(),
    onAddOrganization: () -> Unit,
    onAddSubject: () -> Unit,
    onAddTeacher: () -> Unit,
    onUpdateLocations: (List<ContactInfoUi>) -> Unit,
    onUpdateOffices: (List<String>) -> Unit,
    onSelectOrganization: (OrganizationUi?) -> Unit,
    onSelectSubject: (EventType?, SubjectUi?) -> Unit,
    onSelectTeacher: (EmployeeUi?) -> Unit,
    onSelectLocation: (ContactInfoUi?, String?) -> Unit,
    onSelectTime: (Instant?, Instant?) -> Unit,
    onChangeNotifyParams: (Boolean) -> Unit,
) = with(state) {
    Column(
        modifier = modifier.fillMaxSize().padding(top = 20.dp).verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        val selectedOrganization = organizations.find { it.uid == editableClass?.organization?.uid }
        OrganizationInfoField(
            isLoading = isLoading,
            organization = selectedOrganization,
            allOrganization = organizations,
            onAddOrganization = onAddOrganization,
            onSelected = onSelectOrganization,
        )
        SubjectInfoField(
            isLoading = isLoading,
            subject = editableClass?.subject,
            eventType = editableClass?.eventType,
            allSubjects = selectedOrganization?.subjects ?: emptyList(),
            onAddSubject = onAddSubject,
            onSelectedEventType = { onSelectSubject(it, editableClass?.subject) },
            onSelectedSubject = { onSelectSubject(it?.eventType, it) },
        )
        TeacherInfoField(
            enabledAddTeacher = editableClass?.organization != null,
            isLoading = isLoading,
            teacher = editableClass?.teacher,
            allEmployee = selectedOrganization?.employee ?: emptyList(),
            allSubjects = selectedOrganization?.subjects ?: emptyList(),
            onAddTeacher = onAddTeacher,
            onSelected = onSelectTeacher,
        )
        LocationInfoField(
            enabledAddOffice = editableClass?.organization != null,
            isLoading = isLoading,
            location = editableClass?.location,
            office = editableClass?.office,
            allLocations = selectedOrganization?.locations ?: emptyList(),
            allOffices = selectedOrganization?.offices ?: emptyList(),
            onUpdateOffices = onUpdateOffices,
            onUpdateLocations = onUpdateLocations,
            onSelectedLocation = { onSelectLocation(it, editableClass?.office) },
            onSelectedOffice = { onSelectLocation(editableClass?.location, it) }
        )
        TimeInfoField(
            isLoading = isLoading,
            startTime = editableClass?.startTime,
            endTime = editableClass?.endTime,
            freeClassTimeRanges = freeClassTimeRanges,
            onSelectedTime = onSelectTime,
        )
        NotifyParameter(
            isLoading = isLoading,
            notification = editableClass?.notification ?: false,
            onChangeParams = onChangeNotifyParams,
        )
    }
}