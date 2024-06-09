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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.employee

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import architecture.screen.ScreenContent
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import functional.UID
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEditorDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEditorEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEditorEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEditorViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.screenmodel.rememberEmployeeEditorScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.views.EmployeeEditorTopBar
import views.ErrorSnackbar

/**
 * @author Stanislav Aleshin on 06.06.2024
 */
internal class EmployeeEditorScreen(
    private val employeeId: UID?,
    private val organizationId: UID,
) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberEmployeeEditorScreenModel(),
        initialState = EmployeeEditorViewState(),
        dependencies = EmployeeEditorDeps(
            employeeId = employeeId,
            organizationId = organizationId,
        )
    ) { state ->
        val strings = EditorThemeRes.strings
        val navigator = LocalNavigator.current
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                EmployeeEditorContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onUpdateAvatar = { dispatchEvent(EmployeeEditorEvent.UpdateAvatar(it)) },
                    onEmployeePostSelected = { dispatchEvent(EmployeeEditorEvent.SelectPost(it)) },
                    omBirthdaySelected = { dispatchEvent(EmployeeEditorEvent.SelectBirthday(it)) },
                    onUpdateEmails = { dispatchEvent(EmployeeEditorEvent.UpdateEmails(it)) },
                    onUpdatePhones = { dispatchEvent(EmployeeEditorEvent.UpdatePhones(it)) },
                    onUpdateWebs = { dispatchEvent(EmployeeEditorEvent.UpdateWebs(it)) },
                    onUpdateLocations = { dispatchEvent(EmployeeEditorEvent.UpdateLocations(it)) },
                    omWorkTimeSelected = { start, end ->
                        dispatchEvent(EmployeeEditorEvent.SelectWorkTime(start, end))
                    },
                    onUpdateName = { first, second, patronymic ->
                        dispatchEvent(EmployeeEditorEvent.UpdateName(first, second, patronymic))
                    },
                )
            },
            topBar = {
                EmployeeEditorTopBar(
                    isLoading = state.isLoading,
                    enabledSave = state.editableEmployee?.isValid() ?: false,
                    onBackClick = { dispatchEvent(EmployeeEditorEvent.NavigateToBack) },
                    onSaveClick = { dispatchEvent(EmployeeEditorEvent.SaveEmployee) },
                )
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
                is EmployeeEditorEffect.NavigateToBack -> navigator?.pop()
                is EmployeeEditorEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}
