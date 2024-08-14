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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.functional.uriString
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.contract.OrganizationViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.screenmodel.rememberOrganizationScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.organization.views.OrganizationTopBar
import toStorageFile

/**
 * @author Stanislav Aleshin on 08.07.2024
 */
internal data class OrganizationScreen(val organizationId: UID?) : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberOrganizationScreenModel(),
        initialState = OrganizationViewState(),
        dependencies = OrganizationDeps(organizationId = organizationId),
    ) { state ->
        val strings = EditorThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val coroutineScope = rememberCoroutineScope()
        val navigator = LocalNavigator.currentOrThrow
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                OrganizationContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onUpdateAvatar = {
                        dispatchEvent(OrganizationEvent.UpdateAvatar(it.toStorageFile().uriString()))
                    },
                    onDeleteAvatar = { dispatchEvent(OrganizationEvent.DeleteAvatar) },
                    onSelectedType = { dispatchEvent(OrganizationEvent.UpdateType(it)) },
                    onUpdateName = { short, full ->
                        dispatchEvent(OrganizationEvent.UpdateName(short, full))
                    },
                    onUpdateEmails = { dispatchEvent(OrganizationEvent.UpdateEmails(it)) },
                    onUpdatePhones = { dispatchEvent(OrganizationEvent.UpdatePhones(it)) },
                    onUpdateWebs = { dispatchEvent(OrganizationEvent.UpdateWebs(it)) },
                    onUpdateLocations = { dispatchEvent(OrganizationEvent.UpdateLocations(it)) },
                    onStatusChange = { dispatchEvent(OrganizationEvent.UpdateStatus(it)) },
                    onExceedingAvatarSizeLimit = {
                        coroutineScope.launch {
                            snackbarState.showSnackbar(
                                message = coreStrings.exceedingLimitImageSizeMessage,
                                withDismissAction = true,
                            )
                        }
                    }
                )
            },
            topBar = {
                OrganizationTopBar(
                    enabledSave = state.editableOrganization?.isValid() == true,
                    onBackClick = { dispatchEvent(OrganizationEvent.NavigateToBack) },
                    onSaveClick = { dispatchEvent(OrganizationEvent.SaveOrganization) },
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
                is OrganizationEffect.NavigateToBack -> navigator.nestedPop()
                is OrganizationEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.apply {
                            Logger.i("test") { this.toString() }
                        }.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}