/*
 * Copyright 2026 Stanislav Aleshin
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.profile.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEffect
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEvent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.store.ProfileFeatureComponent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileTopBar

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
@Composable
internal fun ProfileContent(
    profileComponent: ProfileFeatureComponent,
    modifier: Modifier = Modifier,
) {
    val store = profileComponent.store
    val snackbarState = remember { SnackbarHostState() }
    val state by store.stateAsState()
    val adaptiveInfo = currentWindowAdaptiveInfoV2()
    val layoutMode = adaptiveInfo.fetchProfileLayoutMode()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets(),
        topBar = {
            if (layoutMode.showScaffoldTopBar) {
                ProfileTopBar(
                    onEditClick = { store.dispatchEvent(ProfileEvent.ClickEditProfile) },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
    ) { paddingValues ->
        ProfileLayout(
            modifier = Modifier.padding(paddingValues),
            layoutMode = layoutMode,
            isLoading = state.isLoading,
            profile = state.profile,
            onEditClick = { store.dispatchEvent(ProfileEvent.ClickEditProfile) },
            onAboutAppClick = { store.dispatchEvent(ProfileEvent.ClickAboutApp) },
            onGeneralSettingsClick = { store.dispatchEvent(ProfileEvent.ClickGeneralSettings) },
            onNotifySettingsClick = { store.dispatchEvent(ProfileEvent.ClickNotifySettings) },
            onCalendarSettingsClick = { store.dispatchEvent(ProfileEvent.ClickCalendarSettings) },
            onAiSettingsClick = { store.dispatchEvent(ProfileEvent.ClickAiSettings) },
            onShareScheduleClick = { store.dispatchEvent(ProfileEvent.ClickShareSchedule) },
        )
    }

    store.handleEffects { effect ->
        when (effect) {
            is ProfileEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(),
                    withDismissAction = true,
                )
            }
        }
    }
}
