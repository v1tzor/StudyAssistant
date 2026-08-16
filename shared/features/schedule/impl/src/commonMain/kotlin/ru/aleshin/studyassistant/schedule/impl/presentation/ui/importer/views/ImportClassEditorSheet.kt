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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.views.sheet.MediumDragHandle
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportEntryUi
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_apply_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_day_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_end_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_office_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_start_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_subject_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teacher_label

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportClassEditorSheet(
    modifier: Modifier = Modifier,
    entry: ScheduleImportEntryUi,
    subjects: List<SubjectUi>,
    employees: List<EmployeeUi>,
    onDismiss: () -> Unit,
    onConfirm: (ScheduleImportEntryUi) -> Unit,
) {
    var editable by remember(entry) { mutableStateOf(entry) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { MediumDragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = editable.subject,
                onValueChange = { value -> editable = editable.copy(subject = value, subjectId = null) },
                label = { Text(stringResource(Res.string.schedule_import_subject_label)) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                subjects.take(4).forEach { subject ->
                    FilterChip(
                        selected = editable.subjectId == subject.uid,
                        onClick = {
                            editable = editable.copy(subject = subject.name, subjectId = subject.uid)
                        },
                        label = { Text(subject.name) },
                    )
                }
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = editable.teacher,
                onValueChange = { value -> editable = editable.copy(teacher = value, teacherId = null) },
                label = { Text(stringResource(Res.string.schedule_import_teacher_label)) },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                employees.take(4).forEach { employee ->
                    val name = listOfNotNull(employee.secondName, employee.firstName).joinToString(" ")
                    FilterChip(
                        selected = editable.teacherId == employee.uid,
                        onClick = {
                            editable = editable.copy(teacher = name, teacherId = employee.uid)
                        },
                        label = { Text(name) },
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = editable.startTime,
                    onValueChange = { value -> editable = editable.copy(startTime = value) },
                    label = { Text(stringResource(Res.string.schedule_import_start_label)) },
                )
                OutlinedTextField(
                    modifier = Modifier.weight(1f),
                    value = editable.endTime,
                    onValueChange = { value -> editable = editable.copy(endTime = value) },
                    label = { Text(stringResource(Res.string.schedule_import_end_label)) },
                )
            }
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = editable.office,
                onValueChange = { value -> editable = editable.copy(office = value) },
                label = { Text(stringResource(Res.string.schedule_import_office_label)) },
            )
            Text(text = stringResource(Res.string.schedule_import_day_label))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DayOfWeek.entries.forEach { day ->
                    FilterChip(
                        selected = editable.dayOfWeek == day.ordinal + 1,
                        onClick = { editable = editable.copy(dayOfWeek = day.ordinal + 1) },
                        label = { Text(day.mapToSting()) },
                    )
                }
            }
            Button(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                onClick = { onConfirm(editable) },
            ) {
                Text(stringResource(Res.string.schedule_import_apply_button))
            }
        }
    }
}
