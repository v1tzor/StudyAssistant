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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import kotlinx.coroutines.launch
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.contract.EmployeeViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.screenmodel.rememberEmployeeScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.views.EmployeeTopBar

/**
 * @author Stanislav Aleshin on 06.06.2024
 */
internal data class EmployeeScreen(
    private val employeeId: UID?,
    private val organizationId: UID,
) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberEmployeeScreenModel(),
        initialState = EmployeeViewState(),
        dependencies = EmployeeDeps(
            employeeId = employeeId,
            organizationId = organizationId,
        ),
    ) { state ->
        val strings = EditorThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val coroutineScope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                EmployeeContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onUpdateAvatar = { dispatchEvent(EmployeeEvent.UpdateAvatar(it)) },
                    onDeleteAvatar = { dispatchEvent(EmployeeEvent.DeleteAvatar) },
                    onEmployeePostSelected = { dispatchEvent(EmployeeEvent.UpdatePost(it)) },
                    omBirthdaySelected = { dispatchEvent(EmployeeEvent.UpdateBirthday(it)) },
                    onUpdateEmails = { dispatchEvent(EmployeeEvent.UpdateEmails(it)) },
                    onUpdatePhones = { dispatchEvent(EmployeeEvent.UpdatePhones(it)) },
                    onUpdateWebs = { dispatchEvent(EmployeeEvent.UpdateWebs(it)) },
                    onUpdateLocations = { dispatchEvent(EmployeeEvent.UpdateLocations(it)) },
                    omWorkTimeSelected = { start, end ->
                        dispatchEvent(EmployeeEvent.UpdateWorkTime(start, end))
                    },
                    onUpdateName = { first, second, patronymic ->
                        dispatchEvent(EmployeeEvent.UpdateName(first, second, patronymic))
                    },
                    onExceedingAvatarSizeLimit = {
                        coroutineScope.launch {
                            snackbarState.showSnackbar(
                                message = coreStrings.exceedingLimitImageSizeMessage,
                                withDismissAction = true,
                            )
                        }
                    },
                )
            },
            topBar = {
                EmployeeTopBar(
                    enabledSave = state.editableEmployee?.isValid() ?: false,
                    onBackClick = { dispatchEvent(EmployeeEvent.NavigateToBack) },
                    onSaveClick = { dispatchEvent(EmployeeEvent.SaveEmployee) },
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
                is EmployeeEffect.NavigateToBack -> navigator.nestedPop()
                is EmployeeEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings, coreStrings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}