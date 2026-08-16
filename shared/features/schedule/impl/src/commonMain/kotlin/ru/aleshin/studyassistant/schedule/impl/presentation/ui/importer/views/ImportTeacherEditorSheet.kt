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
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.core.ui.views.sheet.MediumDragHandle
import ru.aleshin.studyassistant.schedule.impl.presentation.models.importing.ScheduleImportEntryUi
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_apply_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teacher_label

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportTeacherEditorSheet(
    modifier: Modifier = Modifier,
    teacherName: String,
    entries: List<ScheduleImportEntryUi>,
    employees: List<EmployeeUi>,
    onDismiss: () -> Unit,
    onConfirm: (List<ScheduleImportEntryUi>) -> Unit,
) {
    var name by remember { mutableStateOf(teacherName) }
    var teacherId by remember {
        mutableStateOf(
            employees.firstOrNull { employee ->
                listOfNotNull(employee.secondName, employee.firstName, employee.patronymic)
                    .joinToString(" ")
                    .equals(teacherName, ignoreCase = true)
            }?.uid,
        )
    }

    ModalBottomSheet(
        modifier = modifier,
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        dragHandle = { MediumDragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars },
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = { value ->
                    name = value
                    teacherId = employees.firstOrNull { employee ->
                        listOfNotNull(employee.secondName, employee.firstName, employee.patronymic)
                            .joinToString(" ")
                            .equals(value, ignoreCase = true)
                    }?.uid
                },
                label = { Text(stringResource(Res.string.schedule_import_teacher_label)) },
            )
            Button(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                onClick = {
                    onConfirm(
                        entries.map { entry ->
                            if (entry.teacher.equals(teacherName, ignoreCase = true)) {
                                entry.copy(teacher = name, teacherId = teacherId)
                            } else {
                                entry
                            }
                        },
                    )
                },
            ) {
                Text(stringResource(Res.string.schedule_import_apply_button))
            }
        }
    }
}
