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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.ai

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
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.contract.AiSettingsEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.store.AiSettingsComponent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.fetchSettingsLayoutMode

/**
 * @author Stanislav Aleshin on 12.08.2026.
 */
@Composable
internal fun AiSettingsContent(
    component: AiSettingsComponent,
    modifier: Modifier = Modifier,
) {
    val store = component.store
    val state by store.stateAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val layoutMode = currentWindowAdaptiveInfoV2().fetchSettingsLayoutMode()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data -> ErrorSnackbar(data) }
        },
        contentWindowInsets = WindowInsets()
    ) { contentPadding ->
        AiSettingsLayout(
            modifier = Modifier.padding(contentPadding),
            layoutMode = layoutMode,
            settings = state.settings,
        )
    }

    store.handleEffects { effect ->
        when (effect) {
            is AiSettingsEffect.ShowError -> snackbarHostState.showSnackbar(
                message = effect.failure.mapToMessage(),
                withDismissAction = true,
            )
        }
    }
}
