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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import architecture.screen.ScreenContent
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import functional.UID
import navigation.root
import ru.aleshin.studyassistant.info.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeDeps
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeEffect
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeEvent
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.contract.EmployeeViewState
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.screenmodel.rememberEmployeeScreenModel
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.views.EmployeeFiltersView
import ru.aleshin.studyassistant.info.impl.presentation.ui.employee.views.EmployeeSearchTopBar
import ru.aleshin.studyassistant.info.impl.presentation.ui.theme.InfoThemeRes
import views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 16.06.2024.
 */
internal data class EmployeeScreen(val organizationId: UID) : Screen {

    @Composable
    @OptIn(ExperimentalMaterial3Api::class)
    override fun Content() = ScreenContent(
        screenModel = rememberEmployeeScreenModel(),
        initialState = EmployeeViewState(),
        dependencies = EmployeeDeps(organizationId = organizationId),
    ) { state ->
        val strings = InfoThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                EmployeeContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onEditEmployee = { dispatchEvent(EmployeeEvent.NavigateToEditor(it)) },
                    onDeleteEmployee = { dispatchEvent(EmployeeEvent.DeleteEmployee(it)) }
                )
            },
            topBar = {
                Column {
                    EmployeeSearchTopBar(
                        isLoading = state.isLoading,
                        onBackPress = { dispatchEvent(EmployeeEvent.NavigateToBack) },
                        onSearch = { dispatchEvent(EmployeeEvent.SearchEmployee(it)) }
                    )
                    EmployeeFiltersView(
                        isLoading = state.isLoading,
                        selectedOrganization = state.selectedOrganization,
                        allOrganizations = state.organizations,
                        onSelectOrganization = { dispatchEvent(EmployeeEvent.SelectedOrganization(it.uid)) },
                    )
                }
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { dispatchEvent(EmployeeEvent.NavigateToEditor(null)) },
                    shape = MaterialTheme.shapes.large,
                    backgroundColor = MaterialTheme.colorScheme.surfaceContainerHigh,
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
        )

        handleEffect { effect ->
            when (effect) {
                is EmployeeEffect.NavigateToGlobal -> navigator.root().push(effect.pushScreen)
                is EmployeeEffect.NavigateToBack -> navigator.pop()
                is EmployeeEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}