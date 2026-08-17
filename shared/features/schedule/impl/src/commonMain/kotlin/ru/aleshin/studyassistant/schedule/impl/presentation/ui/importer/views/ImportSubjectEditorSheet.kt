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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.importer.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.subject.EventType
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.ContactInfoUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.InfoTextField
import ru.aleshin.studyassistant.core.ui.views.sheet.MediumDragHandle
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_apply_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_event_type_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_event_type_placeholder
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_event_type_selector_header
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_event_type_selector_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_location_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_new_teacher_name
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_office_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_subject_editor_title
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_subject_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teacher_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teacher_placeholder
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teacher_selector_header
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teacher_selector_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.delete_confirm_title as core_delete_confirm_title
import ru.aleshin.studyassistant.core.ui.resources.ic_class as core_ic_class
import ru.aleshin.studyassistant.core.ui.resources.ic_employee as core_ic_employee
import ru.aleshin.studyassistant.core.ui.resources.ic_map_marker as core_ic_map_marker
import ru.aleshin.studyassistant.core.ui.resources.ic_organization as core_ic_organization

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportSubjectEditorSheet(
    modifier: Modifier = Modifier,
    subject: SubjectUi,
    employees: List<EmployeeUi>,
    originalEmployeeIds: Set<UID>,
    onDismiss: () -> Unit,
    onConfirm: (SubjectUi) -> Unit,
    onDelete: () -> Unit,
    onAddEmployee: (String) -> Unit,
) {
    var editable by remember(subject) { mutableStateOf(subject) }
    var isTeacherSelectorOpen by remember { mutableStateOf(false) }
    var isEventTypeSelectorOpen by remember { mutableStateOf(false) }
    val newTeacherName = stringResource(Res.string.schedule_import_new_teacher_name)
    val sheetContainerColor = MaterialTheme.colorScheme.surfaceContainerLow

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetContainerColor,
        dragHandle = { MediumDragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(Res.string.schedule_import_subject_editor_title),
                style = MaterialTheme.typography.titleLarge,
            )
            InfoTextField(
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = editable.name,
                onValueChange = { value -> editable = editable.copy(name = value) },
                label = stringResource(Res.string.schedule_import_subject_label),
                leadingInfoIcon = painterResource(CoreRes.drawable.core_ic_class),
                containerColor = sheetContainerColor,
            )
            ClickableInfoTextField(
                onClick = { isEventTypeSelectorOpen = true },
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = editable.eventType.mapToString(),
                label = stringResource(Res.string.schedule_import_event_type_label),
                placeholder = stringResource(Res.string.schedule_import_event_type_placeholder),
                infoIcon = painterResource(CoreRes.drawable.core_ic_class),
                backgroundColor = sheetContainerColor,
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isEventTypeSelectorOpen,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            ClickableInfoTextField(
                onClick = { isTeacherSelectorOpen = true },
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = editable.teacher?.fullName(),
                label = stringResource(Res.string.schedule_import_teacher_label),
                placeholder = stringResource(Res.string.schedule_import_teacher_placeholder),
                infoIcon = painterResource(CoreRes.drawable.core_ic_employee),
                backgroundColor = sheetContainerColor,
                trailingIcon = {
                    ExpandedIcon(
                        isExpanded = isTeacherSelectorOpen,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                },
            )
            InfoTextField(
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = editable.office,
                onValueChange = { value -> editable = editable.copy(office = value) },
                label = stringResource(Res.string.schedule_import_office_label),
                leadingInfoIcon = painterResource(CoreRes.drawable.core_ic_organization),
                containerColor = sheetContainerColor,
            )
            InfoTextField(
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = editable.location?.value.orEmpty(),
                onValueChange = { value ->
                    editable = editable.copy(location = ContactInfoUi(value = value))
                },
                label = stringResource(Res.string.schedule_import_location_label),
                leadingInfoIcon = painterResource(CoreRes.drawable.core_ic_map_marker),
                containerColor = sheetContainerColor,
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                    ),
                ) {
                    Text(stringResource(CoreRes.string.core_delete_confirm_title))
                }
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        onConfirm(
                            editable.copy(
                                location = editable.location?.value?.trim()?.takeIf(String::isNotEmpty)?.let { value ->
                                    ContactInfoUi(value = value)
                                },
                            )
                        )
                    },
                ) {
                    Text(stringResource(Res.string.schedule_import_apply_button))
                }
            }
        }
    }

    if (isTeacherSelectorOpen) {
        ImportEntitySelectorSheet(
            selectedId = editable.teacher?.uid,
            items = employees.map { employee ->
                ImportSelectorItem(
                    id = employee.uid,
                    title = employee.fullName(),
                    label = if (employee.uid in originalEmployeeIds) employee.post.mapToString() else null,
                )
            },
            header = stringResource(Res.string.schedule_import_teacher_selector_header),
            title = stringResource(Res.string.schedule_import_teacher_selector_title),
            onAdd = { onAddEmployee(newTeacherName) },
            onDismiss = { isTeacherSelectorOpen = false },
            onConfirm = { selectedId ->
                editable = editable.copy(
                    teacher = employees.firstOrNull { employee -> employee.uid == selectedId },
                )
                isTeacherSelectorOpen = false
            },
        )
    }
    if (isEventTypeSelectorOpen) {
        ImportEntitySelectorSheet(
            selectedId = editable.eventType.name,
            items = EventType.entries.map { type ->
                ImportSelectorItem(id = type.name, title = type.mapToString(), label = null)
            },
            header = stringResource(Res.string.schedule_import_event_type_selector_header),
            title = stringResource(Res.string.schedule_import_event_type_selector_title),
            onAdd = null,
            onDismiss = { isEventTypeSelectorOpen = false },
            onConfirm = { selectedId ->
                val type = EventType.entries.firstOrNull { item -> item.name == selectedId }
                if (type != null) editable = editable.copy(eventType = type)
                isEventTypeSelectorOpen = false
            },
        )
    }
}
