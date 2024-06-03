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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import entities.subject.EventType
import mappers.mapToSting
import mappers.mapToString
import ru.aleshin.studyassistant.editor.impl.presentation.models.ContactInfoUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.EmployeeUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes
import theme.material.full
import views.dialog.BaseSelectorDialog
import views.dialog.SelectorDialogAddItemView
import views.dialog.SelectorDialogItemView

/**
 * @author Stanislav Aleshin on 02.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OrganizationSelectorDialog(
    modifier: Modifier = Modifier,
    selected: OrganizationShortUi?,
    organizations: List<OrganizationShortUi>,
    onAddOrganization: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (OrganizationShortUi?) -> Unit,
) {
    var selectedOrganization by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedOrganization,
        items = organizations,
        header = EditorThemeRes.strings.organizationSelectorHeader,
        title = EditorThemeRes.strings.organizationSelectorTitle,
        itemView = { organization ->
            SelectorDialogItemView(
                onClick = { selectedOrganization = organization },
                selected = organization.uid == selectedOrganization?.uid,
                title = organization.shortName,
                label = organization.type.mapToSting(StudyAssistantRes.strings),
            )
        },
        addItemView = {
            SelectorDialogAddItemView(onClick = onAddOrganization)
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun EventTypeSelectorDialog(
    modifier: Modifier = Modifier,
    selected: EventType?,
    onDismiss: () -> Unit,
    onConfirm: (EventType?) -> Unit,
) {
    var selectedEventType by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedEventType,
        items = EventType.entries,
        header = EditorThemeRes.strings.eventTypeSelectorHeader,
        title = EditorThemeRes.strings.eventTypeSelectorTitle,
        itemView = { eventType ->
            SelectorDialogItemView(
                onClick = { selectedEventType = eventType },
                selected = eventType == selectedEventType,
                title = eventType.mapToString(StudyAssistantRes.strings),
                label = null,
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun SubjectSelectorDialog(
    modifier: Modifier = Modifier,
    eventType: EventType?,
    selected: SubjectUi?,
    subjects: List<SubjectUi>,
    onAddSubject: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (SubjectUi?) -> Unit,
) {
    var selectedSubject by remember { mutableStateOf(selected) }
    val subjectsByEventType = subjects.filter { it.eventType == eventType }
    val otherSubjects = subjects.filter { it.eventType != eventType }.sortedBy { it.eventType }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedSubject,
        items = subjectsByEventType + otherSubjects,
        header = EditorThemeRes.strings.subjectSelectorHeader,
        title = EditorThemeRes.strings.subjectSelectorTitle,
        itemView = { subject ->
            SelectorDialogItemView(
                onClick = { selectedSubject = subject },
                selected = subject.uid == selectedSubject?.uid,
                title = subject.name,
                label = subject.eventType.mapToString(StudyAssistantRes.strings),
                leadingIcon = {
                    Surface(
                        modifier = Modifier.height(IntrinsicSize.Max),
                        shape = MaterialTheme.shapes.full(),
                        color = Color(subject.color),
                        content = { Box(modifier = Modifier.size(8.dp, 24.dp)) },
                    )
                },
            )
        },
        addItemView = {
            SelectorDialogAddItemView(onClick = onAddSubject)
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun TeacherSelectorDialog(
    modifier: Modifier = Modifier,
    selected: EmployeeUi?,
    teachers: List<EmployeeUi>,
    onAddTeacher: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (EmployeeUi?) -> Unit,
) {
    var selectedTeacher by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedTeacher,
        items = teachers,
        header = EditorThemeRes.strings.teacherSelectorHeader,
        title = EditorThemeRes.strings.teacherSelectorTitle,
        itemView = { teacher ->
            SelectorDialogItemView(
                onClick = { selectedTeacher = teacher },
                selected = teacher.uid == selectedTeacher?.uid,
                title = teacher.name(),
                label = teacher.post.mapToString(StudyAssistantRes.strings),
            )
        },
        addItemView = {
            SelectorDialogAddItemView(onClick = onAddTeacher)
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun LocationSelectorDialog(
    modifier: Modifier = Modifier,
    selected: ContactInfoUi?,
    locations: List<ContactInfoUi>,
    onAddLocation: (ContactInfoUi) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (ContactInfoUi?) -> Unit,
) {
    var selectedLocation by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedLocation,
        items = locations,
        header = EditorThemeRes.strings.locationSelectorHeader,
        title = EditorThemeRes.strings.locationSelectorTitle,
        itemView = { location ->
            SelectorDialogItemView(
                onClick = { selectedLocation = location },
                selected = location == selectedLocation,
                title = location.value,
                label = location.label,
            )
        },
        addItemView = {
            SelectorDialogAddItemView(onClick = {})
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun OfficeSelectorDialog(
    modifier: Modifier = Modifier,
    selected: Int?,
    offices: List<Int>,
    onAddOffice: (Int) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (Int?) -> Unit,
) {
    var selectedOffice by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedOffice,
        items = offices,
        header = EditorThemeRes.strings.officeSelectorHeader,
        title = EditorThemeRes.strings.officeSelectorTitle,
        itemView = { office ->
            SelectorDialogItemView(
                onClick = { selectedOffice = office },
                selected = office == selectedOffice,
                title = office.toString(),
                label = null,
            )
        },
        addItemView = {
            SelectorDialogAddItemView(onClick = {})
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}
