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

package ru.aleshin.studyassistant.profile.impl.presentation.ui

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import ru.aleshin.studyassistant.core.common.architecture.screen.ScreenContent
import ru.aleshin.studyassistant.core.common.navigation.root
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.profile.api.presentation.ProfileRootScreen
import ru.aleshin.studyassistant.profile.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.profile.impl.presentation.theme.ProfileTheme
import ru.aleshin.studyassistant.profile.impl.presentation.theme.ProfileThemeRes
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEffect
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEvent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileViewState
import ru.aleshin.studyassistant.profile.impl.presentation.ui.screenmodel.rememberProfileScreenModel
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileTopBar

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
internal class ProfileScreen : ProfileRootScreen() {

    @Composable
    override fun Content() = ScreenContent(
        screenModel = rememberProfileScreenModel(),
        initialState = ProfileViewState(),
    ) { state ->
        ProfileTheme {
            val strings = ProfileThemeRes.strings
            val coreStrings = StudyAssistantRes.strings
            val rootNavigator = LocalNavigator.currentOrThrow.root()
            val snackbarState = remember { SnackbarHostState() }

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                content = { paddingValues ->
                    ProfileContent(
                        state = state,
                        modifier = Modifier.padding(paddingValues),
                        onFriendsClick = { dispatchEvent(ProfileEvent.NavigateToFriends) },
                        onPrivacySettingsClick = { dispatchEvent(ProfileEvent.NavigateToPrivacySettings) },
                        onGeneralSettingsClick = { dispatchEvent(ProfileEvent.NavigateToGeneralSettings) },
                        onNotifySettingsClick = { dispatchEvent(ProfileEvent.NavigateToNotifySettings) },
                        onCalendarSettingsClick = { dispatchEvent(ProfileEvent.NavigateToCalendarSettings) },
                        onPaymentsSettingsClick = { dispatchEvent(ProfileEvent.NavigateToPaymentsSettings) },
                        onShowSchedule = { dispatchEvent(ProfileEvent.NavigateToShareScheduleViewer(it.uid)) },
                        onCancelSentSchedule = { dispatchEvent(ProfileEvent.CancelSentSchedule(it)) },
                        onShareSchedule = { dispatchEvent(ProfileEvent.SendSharedSchedule(it)) }
                    )
                },
                topBar = {
                    ProfileTopBar(
                        onSignOutClick = { dispatchEvent(ProfileEvent.SignOut) },
                        onEditClick = { dispatchEvent(ProfileEvent.NavigateToProfileEditor) },
                    )
                },
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackbarState,
                        snackbar = { ErrorSnackbar(it) },
                    )
                },
                contentWindowInsets = WindowInsets.statusBars
            )

            handleEffect { effect ->
                when (effect) {
                    is ProfileEffect.PushGlobalScreen -> rootNavigator.push(effect.screen)
                    is ProfileEffect.ReplaceGlobalScreen -> rootNavigator.replaceAll(effect.screen)
                    is ProfileEffect.ShowError -> {
                        snackbarState.showSnackbar(
                            message = effect.failures.mapToMessage(strings, coreStrings),
                            withDismissAction = true,
                        )
                    }
                }
            }
        }
    }
}