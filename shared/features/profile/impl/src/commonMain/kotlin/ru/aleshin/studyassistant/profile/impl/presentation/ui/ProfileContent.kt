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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.profile.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.ReceivedMediatedSchedulesShortUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.SentMediatedSchedulesUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.ShareSchedulesSendDataUi
import ru.aleshin.studyassistant.profile.impl.presentation.theme.ProfileTheme
import ru.aleshin.studyassistant.profile.impl.presentation.theme.ProfileThemeRes
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEffect
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEvent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileState
import ru.aleshin.studyassistant.profile.impl.presentation.ui.store.InternalProfileFeatureComponent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileActionsSection
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileInfoSection
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileTopBar

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
@Composable
internal fun ProfileContent(
    profileComponent: InternalProfileFeatureComponent,
    modifier: Modifier = Modifier,
) {
    ProfileTheme {
        val store = profileComponent.store
        val strings = ProfileThemeRes.strings
        val coreStrings = StudyAssistantRes.strings
        val snackbarState = remember { SnackbarHostState() }

        Scaffold(
            modifier = modifier.fillMaxSize(),
            content = { paddingValues ->
                val state by store.stateAsState()

                BaseProfileContent(
                    state = state,
                    modifier = Modifier.padding(paddingValues),
                    onFriendsClick = { store.dispatchEvent(ProfileEvent.ClickFriends) },
                    onAboutAppClick = { store.dispatchEvent(ProfileEvent.ClickAboutApp) },
                    onGeneralSettingsClick = { store.dispatchEvent(ProfileEvent.ClickGeneralSettings) },
                    onNotifySettingsClick = { store.dispatchEvent(ProfileEvent.ClickNotifySettings) },
                    onCalendarSettingsClick = { store.dispatchEvent(ProfileEvent.ClickCalendarSettings) },
                    onPaymentsSettingsClick = { store.dispatchEvent(ProfileEvent.ClickPaymentsSettings) },
                    onShowSharedScheduleClick = { store.dispatchEvent(ProfileEvent.ClickSharedSchedule(it.uid)) },
                    onCancelSentScheduleClick = { store.dispatchEvent(ProfileEvent.CancelSentSchedule(it)) },
                    onShareScheduleClick = { store.dispatchEvent(ProfileEvent.SendSharedSchedule(it)) }
                )
            },
            topBar = {
                ProfileTopBar(
                    onSignOutClick = { store.dispatchEvent(ProfileEvent.ClickSignOut) },
                    onEditClick = { store.dispatchEvent(ProfileEvent.ClickEditProfile) },
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

        store.handleEffects { effect ->
            when (effect) {
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

@Composable
private fun BaseProfileContent(
    state: ProfileState,
    modifier: Modifier,
    onFriendsClick: () -> Unit,
    onAboutAppClick: () -> Unit,
    onGeneralSettingsClick: () -> Unit,
    onNotifySettingsClick: () -> Unit,
    onCalendarSettingsClick: () -> Unit,
    onPaymentsSettingsClick: () -> Unit,
    onShowSharedScheduleClick: (ReceivedMediatedSchedulesShortUi) -> Unit,
    onCancelSentScheduleClick: (SentMediatedSchedulesUi) -> Unit,
    onShareScheduleClick: (ShareSchedulesSendDataUi) -> Unit,
) {
    Column(modifier = modifier) {
        ProfileInfoSection(
            isLoading = state.isLoading,
            profile = state.appUserProfile,
        )
        ProfileActionsSection(
            modifier = Modifier.weight(1f),
            isLoading = state.isLoading,
            isLoadingShare = state.isLoadingShare,
            isLoadingSend = state.isLoadingSend,
            currentTime = state.currentTime,
            profile = state.appUserProfile,
            requests = state.friendRequest,
            allOrganizations = state.allOrganizations,
            allFriends = state.allFriends,
            sharedSchedules = state.sharedSchedules,
            onFriendsClick = onFriendsClick,
            onAboutAppClick = onAboutAppClick,
            onGeneralSettingsClick = onGeneralSettingsClick,
            onNotifySettingsClick = onNotifySettingsClick,
            onCalendarSettingsClick = onCalendarSettingsClick,
            onPaymentsSettingsClick = onPaymentsSettingsClick,
            onShowScheduleClick = onShowSharedScheduleClick,
            onCancelSentScheduleClick = onCancelSentScheduleClick,
            onShareScheduleClick = onShareScheduleClick,
        )
    }
}