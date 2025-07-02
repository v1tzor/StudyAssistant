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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup

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
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.tokens.LocalWindowSize
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.preview.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.preview.impl.presentation.theme.PreviewThemeRes
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEffect
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupEvent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract.SetupViewState
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.screenmodel.rememberSetupScreenModel
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.SetupTopBar

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
internal class SetupScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberSetupScreenModel(),
        initialState = SetupViewState(),
    ) { state ->
        val strings = PreviewThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val windowSize = LocalWindowSize.current
        val rootNavigator = LocalNavigator.currentOrThrow.root()
        val coroutineScope = rememberCoroutineScope()
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                when (windowSize.heightWindowType) {
                    else -> SetupContent(
                        state = state,
                        modifier = Modifier.padding(paddingValues),
                        onUpdateProfile = { dispatchEvent(SetupEvent.UpdateProfile(it)) },
                        onUpdateOrganization = { dispatchEvent(SetupEvent.UpdateOrganization(it)) },
                        onUpdateCalendarSettings = { dispatchEvent(SetupEvent.UpdateCalendarSettings(it)) },
                        onSaveProfile = { dispatchEvent(SetupEvent.SaveProfileInfo) },
                        onUpdateProfileAvatar = { dispatchEvent(SetupEvent.UpdateProfileAvatar(it)) },
                        onDeleteProfileAvatar = { dispatchEvent(SetupEvent.DeleteProfileAvatar) },
                        onSaveOrganization = { dispatchEvent(SetupEvent.SaveOrganizationInfo) },
                        onUpdateOrganizationAvatar = { dispatchEvent(SetupEvent.UpdateOrganizationAvatar(it)) },
                        onDeleteOrganizationAvatar = { dispatchEvent(SetupEvent.DeleteOrganizationAvatar) },
                        onSaveCalendar = { dispatchEvent(SetupEvent.SaveCalendarInfo) },
                        onFillOutSchedule = { dispatchEvent(SetupEvent.NavigateToWeekScheduleEditor) },
                        onStartUsing = { dispatchEvent(SetupEvent.NavigateToSchedule) },
                        onExceedingAvatarSizeLimit = {
                            coroutineScope.launch {
                                snackbarState.showSnackbar(
                                    message = coreStrings.exceedingLimitImageSizeMessage,
                                    withDismissAction = true,
                                )
                            }
                        }
                    )
                }
            },
            topBar = {
                SetupTopBar(
                    enabled = state.currentPage.id != 0,
                    onBackPressed = { dispatchEvent(SetupEvent.NavigateToBackPage) },
                    stepProgress = state.currentPage.progress(),
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
                is SetupEffect.NavigateToGlobalScreen -> rootNavigator.push(effect.pushScreen)
                is SetupEffect.ReplaceGlobalScreen -> rootNavigator.replaceAll(effect.screen)
                is SetupEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}