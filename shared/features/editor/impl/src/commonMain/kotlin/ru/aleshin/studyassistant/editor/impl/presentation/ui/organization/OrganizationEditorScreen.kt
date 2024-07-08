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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.organization

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
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEditorDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEditorEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEditorEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEditorViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.screenmodel.rememberOrganizationEditorScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.views.OrganizationEditorTopBar

/**
 * @author Stanislav Aleshin on 08.07.2024
 */
internal data class OrganizationEditorScreen(val organizationId: UID?) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberOrganizationEditorScreenModel(),
        initialState = OrganizationEditorViewState(),
        dependencies = OrganizationEditorDeps(organizationId = organizationId),
    ) { state ->
        val strings = EditorThemeRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                OrganizationEditorContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onUpdateAvatar = { dispatchEvent(OrganizationEditorEvent.UpdateAvatar(it)) },
                    onSelectedType = { dispatchEvent(OrganizationEditorEvent.UpdateType(it)) },
                    onUpdateName = { short, full ->
                        dispatchEvent(OrganizationEditorEvent.UpdateName(short, full))
                    },
                    onUpdateEmails = { dispatchEvent(OrganizationEditorEvent.UpdateEmails(it)) },
                    onUpdatePhones = { dispatchEvent(OrganizationEditorEvent.UpdatePhones(it)) },
                    onUpdateWebs = { dispatchEvent(OrganizationEditorEvent.UpdateWebs(it)) },
                    onUpdateLocations = { dispatchEvent(OrganizationEditorEvent.UpdateLocations(it)) },
                    onStatusChange = { dispatchEvent(OrganizationEditorEvent.UpdateStatus(it)) },
                )
            },
            topBar = {
                OrganizationEditorTopBar(
                    enabledSave = state.editableOrganization?.isValid() == true,
                    onBackClick = { dispatchEvent(OrganizationEditorEvent.NavigateToBack) },
                    onSaveClick = { dispatchEvent(OrganizationEditorEvent.SaveOrganization) },
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
                is OrganizationEditorEffect.NavigateToBack -> navigator.nestedPop()
                is OrganizationEditorEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}