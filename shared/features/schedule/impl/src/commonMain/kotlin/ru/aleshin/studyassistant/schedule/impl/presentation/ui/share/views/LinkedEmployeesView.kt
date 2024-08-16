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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.dialog.BaseSelectorDialog
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorDialogNotSelectedItemView
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.users.MediatedEmployeeUi
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes

/**
 * @author Stanislav Aleshin on 16.08.2024.
 */
@Composable
internal fun LinkedEmployeesView(
    modifier: Modifier = Modifier,
    enabledLink: Boolean,
    sharedTeacher: MediatedEmployeeUi,
    linkedTeacher: EmployeeUi?,
    onLinkEmployee: () -> Unit,
) {
    Row(
        modifier = modifier.height(IntrinsicSize.Min).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = MaterialTheme.shapes.large,
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    text = sharedTeacher.post.mapToString(StudyAssistantRes.strings),
                    color = MaterialTheme.colorScheme.primary,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = sharedTeacher.officialName(),
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
        if (linkedTeacher != null && enabledLink) {
            Icon(
                imageVector = Icons.Default.Link,
                contentDescription = null,
                tint = StudyAssistantRes.colors.accents.green,
            )
            Surface(
                modifier = Modifier.weight(1f),
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = MaterialTheme.shapes.large,
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = linkedTeacher.post.mapToString(StudyAssistantRes.strings),
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = linkedTeacher.officialName(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
        } else if (enabledLink) {
            Surface(
                onClick = onLinkEmployee,
                modifier = Modifier.fillMaxHeight(),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = MaterialTheme.shapes.medium,
            ) {
                Box(modifier = Modifier.padding(8.dp), contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                }
            }
        }
    }
}

@Composable
internal fun LinkedEmployeesViewPlaceholder(
    modifier: Modifier = Modifier
) {
    PlaceholderBox(
        modifier = modifier.fillMaxWidth().height(60.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun EmployeeLinkerDialog(
    modifier: Modifier = Modifier,
    selected: EmployeeUi?,
    employees: List<EmployeeUi>,
    onDismiss: () -> Unit,
    onConfirm: (EmployeeUi?) -> Unit,
) {
    val selectedEmployee by derivedStateOf { employees.find { it.uid == selected?.uid } }
    val teachers by remember { derivedStateOf { employees.filter { it.post == EmployeePost.TEACHER } } }
    val otherEmployee by remember { derivedStateOf { employees.filter { it.post != EmployeePost.TEACHER } } }
    var selectedTeacher by remember { mutableStateOf(selectedEmployee) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedTeacher,
        items = teachers + otherEmployee,
        header = ScheduleThemeRes.strings.employeeLinkerDialogHeader,
        title = ScheduleThemeRes.strings.employeeLinkerDialogTitle,
        itemView = { employee ->
            SelectorDialogItemView(
                onClick = { selectedTeacher = employee },
                selected = employee.uid == selectedTeacher?.uid,
                title = employee.officialName(),
                label = null,
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