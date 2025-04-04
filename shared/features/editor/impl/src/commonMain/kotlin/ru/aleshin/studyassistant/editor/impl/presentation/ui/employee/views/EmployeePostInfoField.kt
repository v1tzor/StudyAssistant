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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.views

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorNotSelectedItemView
import ru.aleshin.studyassistant.core.ui.views.sheet.BaseSelectorBottomSheet
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
internal fun EmployeePostInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    post: EmployeePost?,
    onSelected: (EmployeePost?) -> Unit,
) {
    var employeePostSelectorState by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        onClick = { employeePostSelectorState = true },
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        value = post?.mapToString(StudyAssistantRes.strings),
        label = EditorThemeRes.strings.employeePostFieldLabel,
        placeholder = EditorThemeRes.strings.employeePostFieldPlaceholder,
        infoIcon = painterResource(EditorThemeRes.icons.employeePost),
        trailingIcon = {
            ExpandedIcon(
                isExpanded = employeePostSelectorState,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )

    if (employeePostSelectorState) {
        EmployeePostSelectorBottomSheet(
            selected = post,
            onDismiss = { employeePostSelectorState = false },
            onConfirm = { selectedEventType ->
                onSelected(selectedEventType)
                employeePostSelectorState = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun EmployeePostSelectorBottomSheet(
    modifier: Modifier = Modifier,
    selected: EmployeePost?,
    onDismiss: () -> Unit,
    onConfirm: (EmployeePost?) -> Unit,
) {
    var selectedPost by remember { mutableStateOf(selected) }

    BaseSelectorBottomSheet(
        modifier = modifier,
        selected = selectedPost,
        items = EmployeePost.entries,
        header = EditorThemeRes.strings.employeePostSelectorHeader,
        title = EditorThemeRes.strings.employeePostSelectorTitle,
        itemView = { post ->
            SelectorItemView(
                onClick = { selectedPost = post },
                selected = post == selectedPost,
                title = post.mapToString(StudyAssistantRes.strings),
                label = null,
            )
        },
        notSelectedItem = {
            SelectorNotSelectedItemView(
                selected = selectedPost == null,
                onClick = { selectedPost = null },
            )
        },
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
    )
}