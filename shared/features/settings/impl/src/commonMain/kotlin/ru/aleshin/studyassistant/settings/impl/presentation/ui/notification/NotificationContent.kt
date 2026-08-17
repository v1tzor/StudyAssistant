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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.notification

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
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
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.settings.impl.presentation.ui.fetchSettingsLayoutMode
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.contract.NotificationEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.notification.store.NotificationComponent

/**
 * @author Stanislav Aleshin on 25.08.2024
 */
@Composable
internal fun NotificationContent(
    notificationComponent: NotificationComponent,
    modifier: Modifier = Modifier,
) {
    val store = notificationComponent.store
    val state by store.stateAsState()
    val snackbarState = remember { SnackbarHostState() }
    val layoutMode = currentWindowAdaptiveInfoV2().fetchSettingsLayoutMode()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            NotificationLayout(
                modifier = Modifier.padding(paddingValues),
                layoutMode = layoutMode,
                state = state,
                onUpdateBeggingOfClassesNotify = {
                    store.dispatchEvent(NotificationEvent.UpdateBeggingOfClassesNotify(it))
                },
                onUpdateBeggingOfClassesExceptions = {
                    store.dispatchEvent(NotificationEvent.UpdateBeggingOfClassesExceptions(it))
                },
                onUpdateEndOfClassesNotify = {
                    store.dispatchEvent(NotificationEvent.UpdateEndOfClassesNotify(it))
                },
                onUpdateEndOfClassesExceptions = {
                    store.dispatchEvent(NotificationEvent.UpdateEndOfClassesExceptions(it))
                },
                onUpdateUnfinishedHomeworksNotify = {
                    store.dispatchEvent(NotificationEvent.UpdateUnfinishedHomeworksNotify(it))
                },
                onUpdateWorkloadWarningNotify = {
                    store.dispatchEvent(NotificationEvent.UpdateHighWorkloadWarningNotify(it))
                },
            )
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarState,
                snackbar = { ErrorSnackbar(it) },
            )
        },
        contentWindowInsets = WindowInsets.navigationBars,
    )

    store.handleEffects { effect ->
        when (effect) {
            is NotificationEffect.ShowError -> {
                snackbarState.showSnackbar(
                    message = effect.failures.mapToMessage(),
                    withDismissAction = true,
                )
            }
        }
    }
}
