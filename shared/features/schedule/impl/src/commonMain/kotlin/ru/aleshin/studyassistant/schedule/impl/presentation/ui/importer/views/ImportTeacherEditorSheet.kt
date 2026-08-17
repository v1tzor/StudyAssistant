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
import ru.aleshin.studyassistant.core.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.core.ui.views.InfoTextField
import ru.aleshin.studyassistant.core.ui.views.sheet.MediumDragHandle
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_apply_button
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_first_name_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_patronymic_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_second_name_label
import ru.aleshin.studyassistant.schedule.impl.resources.schedule_import_teacher_editor_title
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.delete_confirm_title as core_delete_confirm_title
import ru.aleshin.studyassistant.core.ui.resources.ic_employee as core_ic_employee
import ru.aleshin.studyassistant.core.ui.resources.ic_profile as core_ic_profile

/**
 * @author Stanislav Aleshin on 16.08.2026.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun ImportTeacherEditorSheet(
    modifier: Modifier = Modifier,
    employee: EmployeeUi,
    onDismiss: () -> Unit,
    onConfirm: (EmployeeUi) -> Unit,
    onDelete: () -> Unit,
) {
    var firstName by remember(employee.uid) { mutableStateOf(employee.firstName) }
    var secondName by remember(employee.uid) { mutableStateOf(employee.secondName.orEmpty()) }
    var patronymic by remember(employee.uid) { mutableStateOf(employee.patronymic.orEmpty()) }
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
                text = stringResource(Res.string.schedule_import_teacher_editor_title),
                style = MaterialTheme.typography.titleLarge,
            )
            InfoTextField(
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = firstName,
                onValueChange = { value -> firstName = value },
                label = stringResource(Res.string.schedule_import_first_name_label),
                leadingInfoIcon = painterResource(CoreRes.drawable.core_ic_profile),
                containerColor = sheetContainerColor,
            )
            InfoTextField(
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = secondName,
                onValueChange = { value -> secondName = value },
                label = stringResource(Res.string.schedule_import_second_name_label),
                leadingInfoIcon = painterResource(CoreRes.drawable.core_ic_employee),
                containerColor = sheetContainerColor,
            )
            InfoTextField(
                modifier = Modifier.padding(start = 16.dp, end = 24.dp),
                value = patronymic,
                onValueChange = { value -> patronymic = value },
                label = stringResource(Res.string.schedule_import_patronymic_label),
                leadingInfoIcon = painterResource(CoreRes.drawable.core_ic_employee),
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
                            employee.copy(
                                firstName = firstName.trim(),
                                secondName = secondName.trim().takeIf(String::isNotEmpty),
                                patronymic = patronymic.trim().takeIf(String::isNotEmpty),
                            )
                        )
                    },
                ) {
                    Text(stringResource(Res.string.schedule_import_apply_button))
                }
            }
        }
    }
}
