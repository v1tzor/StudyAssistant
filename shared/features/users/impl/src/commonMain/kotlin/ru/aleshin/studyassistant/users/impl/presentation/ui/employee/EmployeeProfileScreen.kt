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

package ru.aleshin.studyassistant.users.impl.presentation.ui.employee

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.users.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.users.impl.presentation.theme.UsersThemeRes
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileDeps
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileEffect
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileEvent
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.contract.EmployeeProfileViewState
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.screenmodel.rememberEmployeeProfileScreenModel
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeProfileTopBar
import ru.aleshin.studyassistant.users.impl.presentation.ui.employee.views.EmployeeTopSheet

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
internal data class EmployeeProfileScreen(private val employeeId: UID) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberEmployeeProfileScreenModel(),
        initialState = EmployeeProfileViewState(),
        dependencies = EmployeeProfileDeps(employeeId = employeeId)
    ) { state ->
        val strings = UsersThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                EmployeeProfileContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                )
            },
            topBar = {
                Column {
                    EmployeeProfileTopBar(
                        enabledEdit = !state.employee?.uid.isNullOrBlank(),
                        onBackClick = { dispatchEvent(EmployeeProfileEvent.NavigateToBack) },
                        onEditClick = { dispatchEvent(EmployeeProfileEvent.NavigateToEditor) },
                    )
                    EmployeeTopSheet(
                        isLoading = state.isLoading,
                        employee = state.employee,
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
                is EmployeeProfileEffect.NavigateToGlobal -> navigator.root().push(effect.pushScreen)
                is EmployeeProfileEffect.NavigateToBack -> navigator.nestedPop()
                is EmployeeProfileEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}