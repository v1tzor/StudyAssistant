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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.ai.AiSettings
import ru.aleshin.studyassistant.core.ui.theme.tokens.AdaptiveLayoutDefaults
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.AiSettingsUi
import ru.aleshin.studyassistant.settings.impl.presentation.ui.SettingsLayoutMode
import ru.aleshin.studyassistant.settings.impl.presentation.ui.common.SettingsExpandedPane
import ru.aleshin.studyassistant.settings.impl.resources.Res
import ru.aleshin.studyassistant.settings.impl.resources.ai_privacy_description
import ru.aleshin.studyassistant.settings.impl.resources.ai_privacy_title
import ru.aleshin.studyassistant.settings.impl.resources.ai_quota_title
import ru.aleshin.studyassistant.settings.impl.resources.ai_settings_description
import ru.aleshin.studyassistant.settings.impl.resources.ai_settings_title
import ru.aleshin.studyassistant.settings.impl.resources.backend_ai_description
import ru.aleshin.studyassistant.settings.impl.resources.backend_ai_title

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun AiSettingsLayout(
    modifier: Modifier = Modifier,
    layoutMode: SettingsLayoutMode,
    settings: AiSettingsUi?,
) {
    if (settings == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
    } else {
        when (layoutMode) {
            SettingsLayoutMode.COMPACT -> AiSettingsCompactLayout(
                modifier = modifier,
                settings = settings,
            )
            SettingsLayoutMode.EXPANDED -> AiSettingsExpandedLayout(
                modifier = modifier,
                settings = settings,
            )
        }
    }
}

@Composable
private fun AiSettingsCompactLayout(
    modifier: Modifier = Modifier,
    settings: AiSettingsUi,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AiSettingsItems(
            settings = settings,
            useExpandedStyle = false,
        )
    }
}

@Composable
private fun AiSettingsExpandedLayout(
    modifier: Modifier = Modifier,
    settings: AiSettingsUi,
) {
    SettingsExpandedPane(modifier = modifier) {
        AiSettingsItems(
            settings = settings,
            useExpandedStyle = true,
        )
    }
}

@Composable
private fun AiSettingsItems(
    settings: AiSettingsUi,
    useExpandedStyle: Boolean,
) {
    Text(
        modifier = Modifier.widthIn(max = AdaptiveLayoutDefaults.MediumContentMaxWidth),
        text = stringResource(Res.string.ai_settings_title),
        style = MaterialTheme.typography.headlineSmall,
    )
    Text(
        modifier = Modifier.widthIn(max = AdaptiveLayoutDefaults.MediumContentMaxWidth),
        text = stringResource(Res.string.ai_settings_description),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    AiSettingsCard(
        modifier = Modifier.widthIn(max = AdaptiveLayoutDefaults.MediumContentMaxWidth),
        title = stringResource(Res.string.backend_ai_title),
        description = stringResource(Res.string.backend_ai_description),
        supportingText = stringResource(
            Res.string.ai_quota_title,
            settings.quotaRemaining,
            settings.quotaLimit,
            settings.rewardedResetsRemaining,
            AiSettings.MAX_REWARDED_RESETS,
        ),
        useExpandedStyle = useExpandedStyle,
    )
    AiSettingsCard(
        modifier = Modifier.widthIn(max = AdaptiveLayoutDefaults.MediumContentMaxWidth),
        title = stringResource(Res.string.ai_privacy_title),
        description = stringResource(Res.string.ai_privacy_description),
        useExpandedStyle = useExpandedStyle,
    )
}

@Composable
private fun AiSettingsCard(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    useExpandedStyle: Boolean = false,
) {
    val contentPadding = 16.dp
    val titleStyle = MaterialTheme.typography.titleMedium
    val shape = MaterialTheme.shapes.large
    val containerColor = MaterialTheme.colorScheme.surfaceContainer

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(contentPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                text = title,
                style = titleStyle,
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
