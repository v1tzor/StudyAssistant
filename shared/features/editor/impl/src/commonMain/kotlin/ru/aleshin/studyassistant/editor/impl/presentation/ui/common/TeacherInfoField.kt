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

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.domain.entities.employee.EmployeePost
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ClickableInfoTextField
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorAddItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorNotSelectedItemView
import ru.aleshin.studyassistant.core.ui.views.dialog.SelectorSwipeItemView
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.core.ui.views.sheet.BaseSelectorBottomSheet
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeDetailsUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.EmployeeUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
internal fun TeacherInfoField(
    modifier: Modifier = Modifier,
    enabledAddTeacher: Boolean,
    isLoading: Boolean,
    teacher: EmployeeUi?,
    allEmployee: List<EmployeeDetailsUi>,
    onAddTeacher: () -> Unit,
    onEditTeacher: (EmployeeDetailsUi) -> Unit,
    onSelected: (EmployeeDetailsUi?) -> Unit,
) {
    var openTeacherSelectorSheet by remember { mutableStateOf(false) }

    ClickableInfoTextField(
        onClick = { openTeacherSelectorSheet = true },
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        enabled = !isLoading,
        value = teacher?.fullName(),
        label = EditorThemeRes.strings.teacherFieldLabel,
        placeholder = EditorThemeRes.strings.teacherFieldPlaceholder,
        infoIcon = painterResource(StudyAssistantRes.icons.employee),
        trailingIcon = {
            ExpandedIcon(
                isExpanded = openTeacherSelectorSheet,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )

    if (openTeacherSelectorSheet) {
        TeacherSelectorBottomSheet(
            enabledAdd = enabledAddTeacher,
            selected = teacher,
            employees = allEmployee,
            onAddTeacher = onAddTeacher,
            onEditTeacher = onEditTeacher,
            onDismiss = { openTeacherSelectorSheet = false },
            onConfirm = {
                onSelected(it)
                openTeacherSelectorSheet = false
            },
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun TeacherSelectorBottomSheet(
    modifier: Modifier = Modifier,
    enabledAdd: Boolean,
    selected: EmployeeUi?,
    employees: List<EmployeeDetailsUi>,
    onAddTeacher: () -> Unit,
    onEditTeacher: (EmployeeDetailsUi) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: (EmployeeDetailsUi?) -> Unit,
) {
    var searchQuery by remember { mutableStateOf<String?>(null) }
    val selectedEmployee by derivedStateOf { employees.find { it.uid == selected?.uid } }
    val searchedTeachers = remember(searchQuery, employees) {
        employees.filter { employee ->
            val firstNameFilter = employee.firstName.contains(searchQuery ?: "", true)
            val secondNameFilter = employee.secondName?.contains(searchQuery ?: "", true) ?: false
            val patronymicFilter = employee.patronymic?.contains(searchQuery ?: "", true) ?: false
            return@filter searchQuery == null || (firstNameFilter or secondNameFilter or patronymicFilter)
        }
    }
    val teachers = remember(searchedTeachers) {
        searchedTeachers.filter { it.post == EmployeePost.TEACHER }
    }
    val otherEmployee = remember(searchedTeachers) {
        searchedTeachers.filter { it.post != EmployeePost.TEACHER }
    }
    var selectedTeacher by remember { mutableStateOf(selectedEmployee) }

    BaseSelectorBottomSheet(
        modifier = modifier,
        selected = selectedTeacher,
        items = teachers + otherEmployee,
        header = EditorThemeRes.strings.teacherSelectorHeader,
        title = EditorThemeRes.strings.teacherSelectorTitle,
        itemView = { employee ->
            val dismissState = rememberSwipeToDismissBoxState(
                confirmValueChange = { dismissBoxValue ->
                    when (dismissBoxValue) {
                        SwipeToDismissBoxValue.StartToEnd -> Unit
                        SwipeToDismissBoxValue.EndToStart -> onEditTeacher(employee)
                        SwipeToDismissBoxValue.Settled -> Unit
                    }
                    return@rememberSwipeToDismissBoxState false
                },
                positionalThreshold = { it * .60f },
            )
            SelectorSwipeItemView(
                onClick = { selectedTeacher = employee },
                state = dismissState,
                selected = employee.uid == selectedTeacher?.uid,
                title = employee.fullName(),
                label = employee.subjects.firstOrNull()?.name ?: employee.post.mapToString(StudyAssistantRes.strings),
                leadingIcon = {
                    AvatarView(
                        modifier = modifier.size(40.dp),
                        firstName = employee.firstName,
                        secondName = employee.patronymic ?: employee.secondName,
                        imageUrl = employee.avatar,
                        style = MaterialTheme.typography.titleMedium,
                        containerColor = MaterialTheme.colorScheme.secondary,
                        contentColor = MaterialTheme.colorScheme.onSecondary
                    )
                },
                enableDismissFromStartToEnd = false,
                backgroundContent = {
                    SwipeToDismissBackground(
                        dismissState = dismissState,
                        endToStartContent = {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = null,
                            )
                        },
                        endToStartColor = StudyAssistantRes.colors.accents.orangeContainer,
                    )
                },
            )
        },
        addItemView = if (searchQuery == null) {
            {
                SelectorAddItemView(
                    enabled = enabledAdd,
                    onClick = onAddTeacher,
                )
            }
        } else {
            null
        },
        notSelectedItem = if (searchQuery == null) {
            {
                SelectorNotSelectedItemView(
                    selected = selectedTeacher == null,
                    onClick = { selectedTeacher = null },
                )
            }
        } else {
            null
        },
        searchBar = {
            SearchBar(
                inputField = {
                    val focusManager = LocalFocusManager.current
                    val searchInteractionSource = remember { MutableInteractionSource() }
                    val isFocus = searchInteractionSource.collectIsFocusedAsState().value

                    SearchBarDefaults.InputField(
                        query = searchQuery ?: "",
                        onQueryChange = { searchQuery = it.takeIf { it.isNotBlank() } },
                        onSearch = {
                            focusManager.clearFocus()
                            searchQuery = it.takeIf { it.isNotBlank() }
                        },
                        expanded = false,
                        onExpandedChange = {},
                        enabled = true,
                        placeholder = {
                            Text(text = EditorThemeRes.strings.employeeSearchBarPlaceholder)
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = StudyAssistantRes.strings.backIconDesc,
                                tint = MaterialTheme.colorScheme.onSurface,
                            )
                        },
                        trailingIcon = {
                            AnimatedVisibility(
                                visible = isFocus,
                                enter = fadeIn() + scaleIn(),
                                exit = fadeOut() + scaleOut(),
                            ) {
                                IconButton(
                                    onClick = {
                                        searchQuery = null
                                        focusManager.clearFocus()
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = StudyAssistantRes.strings.clearSearchBarDesk,
                                        tint = MaterialTheme.colorScheme.error,
                                    )
                                }
                            }
                        },
                        interactionSource = searchInteractionSource,
                    )
                },
                expanded = false,
                onExpandedChange = {},
                modifier = Modifier.fillMaxWidth(),
                windowInsets = WindowInsets(0.dp),
                content = {},
            )
        },
        onDismissRequest = onDismiss,
        onConfirm = onConfirm,
    )
}