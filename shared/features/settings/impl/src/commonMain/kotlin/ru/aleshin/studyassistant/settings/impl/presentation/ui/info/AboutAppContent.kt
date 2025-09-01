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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.info

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Web
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.architecture.store.compose.stateAsState
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.ui.views.ErrorSnackbar
import ru.aleshin.studyassistant.settings.impl.BuildKonfig.VERSION_CODE
import ru.aleshin.studyassistant.settings.impl.BuildKonfig.VERSION_NAME
import ru.aleshin.studyassistant.settings.impl.presentation.theme.SettingsThemeRes
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.contract.AboutAppState
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.store.AboutAppComponent

/**
 * @author Stanislav Aleshin on 04.08.2025
 */
@Composable
internal fun AboutAppContent(
    aboutAppComponent: AboutAppComponent,
    modifier: Modifier = Modifier,
) {
    val store = aboutAppComponent.store
    val state by store.stateAsState()
    val strings = SettingsThemeRes.strings
    val snackbarState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = { paddingValues ->
            BaseAboutAppContent(
                state = state,
                modifier = Modifier.padding(paddingValues),
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
}

@Composable
private fun BaseAboutAppContent(
    state: AboutAppState,
    scrollState: ScrollState = rememberScrollState(),
    modifier: Modifier = Modifier,
) = with(state) {
    Column(
        modifier = modifier
            .padding(vertical = 24.dp, horizontal = 16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        val uriHandler = LocalUriHandler.current
        Text(
            text = SettingsThemeRes.strings.aboutAppHeader,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelMedium,
        )
        AboutAppSectionVersion()
        AboutAppSectionDevelopment(
            onOpenGit = { uriHandler.openUri(Constants.App.GITHUB_URI) },
            onOpenIssues = { uriHandler.openUri(Constants.App.ISSUES_URI) },
            onOpenWebsite = { uriHandler.openUri(Constants.App.WEBSITE_URI) },
        )
    }
}

@Composable
private fun AboutAppSectionVersion(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            InfoView(
                title = SettingsThemeRes.strings.versionNameTitle,
                text = VERSION_NAME,
            )
            Spacer(modifier = Modifier.weight(1f))
            InfoView(
                title = SettingsThemeRes.strings.versionCodeTitle,
                text = VERSION_CODE,
            )
        }
    }
}

@Composable
private fun AboutAppSectionDevelopment(
    modifier: Modifier = Modifier,
    onOpenGit: () -> Unit,
    onOpenIssues: () -> Unit,
    onOpenWebsite: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                InfoView(
                    modifier = Modifier.fillMaxWidth(),
                    title = SettingsThemeRes.strings.developerTitle,
                    spaceInside = true,
                    text = Constants.App.DEVELOPER,
                )
                InfoView(
                    modifier = Modifier.fillMaxWidth(),
                    title = SettingsThemeRes.strings.licenseTitle,
                    spaceInside = true,
                    text = Constants.App.LICENCE,
                )
            }
            FilterChip(
                modifier = Modifier.fillMaxWidth(),
                selected = true,
                onClick = onOpenWebsite,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Web,
                        contentDescription = SettingsThemeRes.strings.websiteTitle,
                    )
                },
                label = {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = SettingsThemeRes.strings.websiteTitle,
                        textAlign = TextAlign.Center,
                    )
                },
                colors = FilterChipDefaults.filterChipSurfaceVariantColors(),
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    modifier = Modifier.weight(1f),
                    selected = true,
                    onClick = onOpenIssues,
                    label = {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = SettingsThemeRes.strings.askQuestionTitle,
                            textAlign = TextAlign.Center,
                        )
                    },
                    colors = FilterChipDefaults.filterChipSurfaceVariantColors(),
                )
                FilterChip(
                    modifier = Modifier.weight(1f),
                    selected = true,
                    onClick = onOpenGit,
                    label = {
                        Text(
                            modifier = Modifier.fillMaxWidth(),
                            text = SettingsThemeRes.strings.githubTitle,
                            textAlign = TextAlign.Center,
                        )
                    },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(SettingsThemeRes.icons.git),
                            contentDescription = null,
                        )
                    },
                    colors = FilterChipDefaults.filterChipSurfaceVariantColors(),
                )
            }
        }
    }
}

@Composable
private fun InfoView(
    modifier: Modifier = Modifier,
    spaceInside: Boolean = false,
    title: String,
    text: String,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = title,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            style = MaterialTheme.typography.titleMedium,
        )
        if (spaceInside) Spacer(modifier = Modifier.weight(1f))
        Text(
            text = text,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.titleMedium,
        )
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun FilterChipDefaults.filterChipSurfaceVariantColors() =
    FilterChipDefaults.filterChipColors(
        selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
        disabledSelectedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        selectedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        selectedLeadingIconColor = MaterialTheme.colorScheme.onSurface,
        selectedTrailingIconColor = MaterialTheme.colorScheme.onSurface,
    )