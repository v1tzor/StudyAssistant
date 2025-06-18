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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.profile

import androidx.compose.foundation.layout.Column
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
import ru.aleshin.studyassistant.core.common.navigation.nestedPop
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.editor.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.contract.ProfileViewState
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.screenmodel.rememberProfileScreenModel
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.ProfileTopBar
import ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views.ProfileTopSheet

/**
 * @author Stanislav Aleshin on 28.07.2024
 */
internal class ProfileScreen : Screen {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberProfileScreenModel(),
        initialState = ProfileViewState(),
    ) { state ->
        val strings = EditorThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val navigator = LocalNavigator.currentOrThrow
        val coroutineScope = rememberCoroutineScope()
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            content = { paddingValues ->
                ProfileContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onUpdateName = { dispatchEvent(ProfileEvent.UpdateUsername(it)) },
                    onUpdateDescription = { dispatchEvent(ProfileEvent.UpdateDescription(it)) },
                    onUpdateBirthday = { dispatchEvent(ProfileEvent.UpdateBirthday(it)) },
                    onUpdateGender = { dispatchEvent(ProfileEvent.UpdateGender(it)) },
                    onUpdateCity = { dispatchEvent(ProfileEvent.UpdateCity(it)) },
                    onUpdateSocialNetworks = { dispatchEvent(ProfileEvent.UpdateSocialNetworks(it)) },
                )
            },
            topBar = {
                Column {
                    ProfileTopBar(
                        onBackClick = { dispatchEvent(ProfileEvent.NavigateToBack) },
                        onChangePassword = { old, new -> dispatchEvent(ProfileEvent.UpdatePassword(old, new)) },
                    )
                    ProfileTopSheet(
                        isLoading = state.isLoading,
                        isPaidUser = state.isPaidUser,
                        appUser = state.appUser,
                        onUpdateAvatar = { file -> dispatchEvent(ProfileEvent.UpdateAvatar(file)) },
                        onDeleteAvatar = { dispatchEvent(ProfileEvent.DeleteAvatar) },
                        onOpenBillingScreen = { dispatchEvent(ProfileEvent.NavigateToBillingScreen) },
                        onExceedingLimit = {
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
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarState,
                    snackbar = { ErrorSnackbar(it) },
                )
            },
        )

        handleEffect { effect ->
            when (effect) {
                is ProfileEffect.NavigateToBack -> navigator.nestedPop()
                is ProfileEffect.NavigateToGlobal -> navigator.root().push(effect.pushScreen)
                is ProfileEffect.ShowError -> {
                    snackbarState.showSnackbar(
                        message = effect.failures.mapToMessage(strings),
                        withDismissAction = true,
                    )
                }
            }
        }
    }
}