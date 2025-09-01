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

package ru.aleshin.studyassistant.info.impl.presentation.ui.employee

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.info.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeState
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.store.EmployeeComponent
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.views.DetailsEmployeeViewItem
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.views.EmployeeFiltersView
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.views.EmployeeSearchTopBar
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.InfoThemeRes

/**
 * @author Stanislav Aleshin on 19.06.2024.
 */
@Composable
internal fun EmployeeContent(
    employeeComponent: EmployeeComponent,
    modifier: Modifier = Modifier,
) {
    val store = employeeComponent.store
    val state by store.stateAsState()
    val strings = InfoThemeRes.strings
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseEmployeeContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
                onOpenEmployeeProfile = {
                    store.dispatchEvent(EmployeeEvent.ClickEmployeeProfile(it))
                },
                onEditEmployee = {
                    store.dispatchEvent(EmployeeEvent.ClickEditEmployee(it))
                },
                onDeleteEmployee = {
                    store.dispatchEvent(EmployeeEvent.ClickDeleteEmployee(it))
                }
            )
        },
        topBar = {
            Column {
                EmployeeSearchTopBar(
                    isLoading = state.isLoading,
                    onBackPress = {
                        store.dispatchEvent(EmployeeEvent.BackClick)
                    },
                    onSearch = {
                        store.dispatchEvent(EmployeeEvent.SearchEmployee(it))
                    }
                )
                EmployeeFiltersView(
                    isLoading = state.isLoading,
                    selectedOrganization = state.selectedOrganization,
                    allOrganizations = state.organizations,
                    onSelectOrganization = {
                        store.dispatchEvent(EmployeeEvent.SelectedOrganization(it.uid))
                    },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    store.dispatchEvent(EmployeeEvent.ClickEditEmployee(null))
                },
                shape = MaterialTheme.shapes.large,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    )

    store.handleEffects { effect ->
        when (effect) {
            is EmployeeEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(strings),
                    withDismissAction = true,
                )
            }
        }
    }
}

@Composable
private fun BaseEmployeeContent(
    state: EmployeeState,
    modifier: Modifier = Modifier,
    listState: LazyListState = rememberLazyListState(),
    onOpenEmployeeProfile: (UID) -> Unit,
    onEditEmployee: (UID) -> Unit,
    onDeleteEmployee: (UID) -> Unit,
) {
    Crossfade(
        modifier = modifier.padding(start = 12.dp, end = 16.dp, top = 16.dp),
        targetState = state.isLoading,
        animationSpec = floatSpring(),
    ) { loading ->
        if (loading) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(Placeholder.EMPLOYEES_OR_SUBJECTS) {
                    PlaceholderBox(
                        modifier = Modifier.fillMaxWidth().height(90.dp),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.large,
                    )
                }
            }
        } else if (state.employees.isNotEmpty()) {
            val employeesList = remember(state.employees) {
                state.employees.toList()
            }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                items(employeesList, key = { it.first }) { alphabeticEmployees ->
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(
                            modifier = Modifier.padding(top = 16.dp),
                            text = alphabeticEmployees.first.toString(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            style = MaterialTheme.typography.headlineSmall,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            alphabeticEmployees.second.forEach { employee ->
                                DetailsEmployeeViewItem(
                                    avatar = employee.data.avatar,
                                    post = employee.data.post,
                                    firstName = employee.data.firstName,
                                    secondName = employee.data.secondName,
                                    patronymic = employee.data.patronymic,
                                    subjects = employee.subjects,
                                    isHavePhone = employee.data.phones.isNotEmpty(),
                                    isHaveEmail = employee.data.emails.isNotEmpty(),
                                    isHaveWebsite = employee.data.webs.isNotEmpty(),
                                    onOpenProfile = { onOpenEmployeeProfile(employee.data.uid) },
                                    onEdit = { onEditEmployee(employee.data.uid) },
                                    onDelete = { onDeleteEmployee(employee.data.uid) }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            Text(
                modifier = Modifier.fillMaxSize(),
                text = StudyAssistantRes.strings.noResultTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}