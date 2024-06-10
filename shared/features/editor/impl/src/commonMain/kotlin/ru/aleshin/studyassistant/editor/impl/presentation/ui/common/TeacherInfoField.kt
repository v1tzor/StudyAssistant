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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import entities.employee.EmployeePost
import mappers.mapToString
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes
import views.ClickableInfoTextField
import views.ExpandedIcon
import views.dialog.BaseSelectorDialog
import views.dialog.SelectorDialogAddItemView
import views.dialog.SelectorDialogItemView
import views.dialog.SelectorDialogNotSelectedItemView

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun TeacherInfoField(
    modifier: Modifier = Modifier,
    enabledAddTeacher: Boolean,
    isLoading: Boolean,
    teacher: EmployeeUi?,
    allEmployee: List<EmployeeUi>,
    allSubjects: List<SubjectUi>,
    onAddTeacher: () -> Unit,
    onSelected: (EmployeeUi?) -> Unit,
) {
    var isOpenTeacherSelector by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        onClick = { isOpenTeacherSelector = true },
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        value = teacher?.name(),
        label = EditorThemeRes.strings.teacherFieldLabel,
        placeholder = EditorThemeRes.strings.teacherFieldPlaceholder,
        infoIcon = painterResource(EditorThemeRes.icons.employee),
        trailingIcon = {
            ExpandedIcon(
                isExpanded = isOpenTeacherSelector,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )

    if (isOpenTeacherSelector) {
        TeacherSelectorDialog(
            enabledAdd = enabledAddTeacher,
            selected = teacher,
            employees = allEmployee,
            subjects = allSubjects,
            onAddTeacher = onAddTeacher,
            onDismiss = { isOpenTeacherSelector = false },
            onConfirm = {
                onSelected(it)
                isOpenTeacherSelector = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun TeacherSelectorDialog(
    modifier: Modifier = Modifier,
    enabledAdd: Boolean,
    selected: EmployeeUi?,
    employees: List<EmployeeUi>,
    subjects: List<SubjectUi>,
    onAddTeacher: () -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (EmployeeUi?) -> Unit,
) {
    val teachers by remember { derivedStateOf { employees.filter { it.post == EmployeePost.TEACHER } } }
    val otherEmployee by remember { derivedStateOf { employees.filter { it.post != EmployeePost.TEACHER } } }
    var selectedTeacher by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedTeacher,
        items = teachers + otherEmployee,
        header = EditorThemeRes.strings.teacherSelectorHeader,
        title = EditorThemeRes.strings.teacherSelectorTitle,
        itemView = { employee ->
            SelectorDialogItemView(
                onClick = { selectedTeacher = employee },
                selected = employee.uid == selectedTeacher?.uid,
                title = employee.name(),
                label = subjects.find {
                    it.teacher?.uid == employee.uid
                }?.name ?: employee.post.mapToString(StudyAssistantRes.strings),
            )
        },
        addItemView = {
            SelectorDialogAddItemView(
                enabled = enabledAdd,
                onClick = onAddTeacher,
            )
        },
        notSelectedItem = {
            SelectorDialogNotSelectedItemView(
                selected = selectedTeacher == null,
                onClick = { selectedTeacher = null },
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}