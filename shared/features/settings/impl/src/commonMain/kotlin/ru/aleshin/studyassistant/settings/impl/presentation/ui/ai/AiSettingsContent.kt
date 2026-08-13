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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.handleEffects
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.domain.entities.ai.AiSettings
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.settings.impl.presentation.mappers.mapToMessage
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.AiSettingsUi
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.contract.AiSettingsEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.ai.store.AiSettingsComponent
import ru.aleshin.studyassistant.settings.impl.resources.Res
import ru.aleshin.studyassistant.settings.impl.resources.ai_privacy_description
import ru.aleshin.studyassistant.settings.impl.resources.ai_privacy_title
import ru.aleshin.studyassistant.settings.impl.resources.ai_quota_title
import ru.aleshin.studyassistant.settings.impl.resources.ai_settings_description
import ru.aleshin.studyassistant.settings.impl.resources.ai_settings_title
import ru.aleshin.studyassistant.settings.impl.resources.backend_ai_description
import ru.aleshin.studyassistant.settings.impl.resources.backend_ai_title

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

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data -> ErrorSnackbar(data) }
        },
    ) { contentPadding ->
        AiSettingsLayout(
            settings = state.settings,
            modifier = Modifier.padding(contentPadding),
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

@Composable
private fun AiSettingsLayout(
    settings: AiSettingsUi?,
    modifier: Modifier = Modifier,
) {
    if (settings == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        AiSettingsView(
            settings = settings,
            modifier = modifier,
        )
    }
}

@Composable
private fun AiSettingsView(
    settings: AiSettingsUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = stringResource(Res.string.ai_settings_title),
            style = MaterialTheme.typography.headlineSmall,
        )
        Text(
            text = stringResource(Res.string.ai_settings_description),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        AiSettingsCard(
            title = stringResource(Res.string.backend_ai_title),
            description = stringResource(Res.string.backend_ai_description),
            supportingText = stringResource(
                Res.string.ai_quota_title,
                settings.quotaRemaining,
                settings.quotaLimit,
                settings.rewardedResetsRemaining,
                AiSettings.MAX_REWARDED_RESETS,
            ),
        )
        AiSettingsCard(
            title = stringResource(Res.string.ai_privacy_title),
            description = stringResource(Res.string.ai_privacy_description),
        )
    }
}

@Composable
private fun AiSettingsCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            supportingText?.let { text ->
                Text(
                    text = text,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
