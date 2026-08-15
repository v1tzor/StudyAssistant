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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.presentation.models.users.ProfileUi
import ru.aleshin.studyassistant.core.ui.utils.isCompactHeight
import ru.aleshin.studyassistant.core.ui.utils.useExpandedLayout
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.profile.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEffect
import ru.aleshin.studyassistant.profile.impl.presentation.ui.contract.ProfileEvent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.store.ProfileFeatureComponent
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileActionsSection
import ru.aleshin.studyassistant.profile.impl.presentation.ui.views.ProfileInfoSection
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

    ProfileScaffold(
        modifier = modifier.fillMaxSize(),
        isLoading = state.isLoading,
        profile = state.profile,
        useTwoPaneLayout = adaptiveInfo.useExpandedLayout || adaptiveInfo.isCompactHeight,
        snackbarState = snackbarState,
        onEvent = store::dispatchEvent,
    )

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

@Composable
private fun ProfileScaffold(
    isLoading: Boolean,
    profile: ProfileUi?,
    useTwoPaneLayout: Boolean,
    snackbarState: SnackbarHostState,
    onEvent: (ProfileEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            ProfileTopBar(
                onEditClick = { onEvent(ProfileEvent.ClickEditProfile) },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
        contentWindowInsets = WindowInsets()
    ) { paddingValues ->
        ProfileLayout(
            modifier = Modifier.padding(paddingValues),
            isLoading = isLoading,
            profile = profile,
            useTwoPaneLayout = useTwoPaneLayout,
            onEvent = onEvent,
        )
    }
}

@Composable
private fun ProfileLayout(
    isLoading: Boolean,
    profile: ProfileUi?,
    useTwoPaneLayout: Boolean,
    onEvent: (ProfileEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (useTwoPaneLayout) {
            Row(
                modifier = Modifier.fillMaxSize().widthIn(max = 1200.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                ProfileInfoSection(
                    modifier = Modifier.weight(2f),
                    isLoading = isLoading,
                    profile = profile,
                )
                ProfileActionsSection(
                    columns = 2,
                    modifier = Modifier.weight(3f),
                    onAboutAppClick = { onEvent(ProfileEvent.ClickAboutApp) },
                    onGeneralSettingsClick = { onEvent(ProfileEvent.ClickGeneralSettings) },
                    onNotifySettingsClick = { onEvent(ProfileEvent.ClickNotifySettings) },
                    onCalendarSettingsClick = { onEvent(ProfileEvent.ClickCalendarSettings) },
                    onAiSettingsClick = { onEvent(ProfileEvent.ClickAiSettings) },
                    onShareScheduleClick = { onEvent(ProfileEvent.ClickShareSchedule) },
                )
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth().widthIn(max = 720.dp),
            ) {
                ProfileInfoSection(
                    isLoading = isLoading,
                    profile = profile,
                )
                ProfileActionsSection(
                    columns = 2,
                    modifier = Modifier.weight(1f),
                    onAboutAppClick = { onEvent(ProfileEvent.ClickAboutApp) },
                    onGeneralSettingsClick = { onEvent(ProfileEvent.ClickGeneralSettings) },
                    onNotifySettingsClick = { onEvent(ProfileEvent.ClickNotifySettings) },
                    onCalendarSettingsClick = { onEvent(ProfileEvent.ClickCalendarSettings) },
                    onAiSettingsClick = { onEvent(ProfileEvent.ClickAiSettings) },
                    onShareScheduleClick = { onEvent(ProfileEvent.ClickShareSchedule) },
                )
            }
        }
    }
}
