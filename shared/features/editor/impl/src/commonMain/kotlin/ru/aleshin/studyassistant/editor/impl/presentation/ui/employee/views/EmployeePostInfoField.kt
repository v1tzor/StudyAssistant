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
import entities.employee.EmployeePost
import mappers.mapToString
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.StudyAssistantRes
import views.ClickableInfoTextField
import views.ExpandedIcon
import views.dialog.BaseSelectorDialog
import views.dialog.SelectorDialogItemView
import views.dialog.SelectorDialogNotSelectedItemView

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun EmployeePostInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    post: EmployeePost?,
    onSelected: (EmployeePost?) -> Unit,
) {
    var isOpenEmployeePostSelector by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        onClick = { isOpenEmployeePostSelector = true },
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        value = post?.mapToString(StudyAssistantRes.strings),
        label = EditorThemeRes.strings.employeePostFieldLabel,
        placeholder = EditorThemeRes.strings.employeePostFieldPlaceholder,
        infoIcon = painterResource(EditorThemeRes.icons.employeePost),
        trailingIcon = {
            ExpandedIcon(
                isExpanded = isOpenEmployeePostSelector,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )

    if (isOpenEmployeePostSelector) {
        EmployeePostSelectorDialog(
            selected = post,
            onDismiss = { isOpenEmployeePostSelector = false },
            onConfirm = { selectedEventType ->
                onSelected(selectedEventType)
                isOpenEmployeePostSelector = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun EmployeePostSelectorDialog(
    modifier: Modifier = Modifier,
    selected: EmployeePost?,
    onDismiss: () -> Unit,
    onConfirm: (EmployeePost?) -> Unit,
) {
    var selectedPost by remember { mutableStateOf(selected) }

    BaseSelectorDialog(
        modifier = modifier,
        selected = selectedPost,
        items = EmployeePost.entries,
        header = EditorThemeRes.strings.employeePostSelectorHeader,
        title = EditorThemeRes.strings.employeePostSelectorTitle,
        itemView = { post ->
            SelectorDialogItemView(
                onClick = { selectedPost = post },
                selected = post == selectedPost,
                title = post.mapToString(StudyAssistantRes.strings),
                label = null,
            )
        },
        notSelectedItem = {
            SelectorDialogNotSelectedItemView(
                selected = selectedPost == null,
                onClick = { selectedPost = null },
            )
        },
        onDismiss = onDismiss,
        onConfirm = onConfirm,
    )
}