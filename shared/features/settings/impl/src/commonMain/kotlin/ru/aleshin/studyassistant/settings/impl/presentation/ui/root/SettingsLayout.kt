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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.root

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.settings.impl.presentation.ui.SettingsLayoutMode
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.views.TabNavigationExpandedTopBar
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.views.TabNavigationRow
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.views.TabNavigationTopBar

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun SettingsLayout(
    modifier: Modifier = Modifier,
    layoutMode: SettingsLayoutMode,
    selectedItem: SettingsTabItem,
    onBackClick: () -> Unit,
    onSelectTab: (SettingsTabItem) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    when (layoutMode) {
        SettingsLayoutMode.COMPACT -> SettingsCompactLayout(
            modifier = modifier,
            selectedItem = selectedItem,
            onBackClick = onBackClick,
            onSelectTab = onSelectTab,
            content = content,
        )
        SettingsLayoutMode.EXPANDED -> SettingsExpandedLayout(
            modifier = modifier,
            selectedItem = selectedItem,
            onBackClick = onBackClick,
            onSelectTab = onSelectTab,
            content = content,
        )
    }
}

@Composable
private fun SettingsCompactLayout(
    modifier: Modifier = Modifier,
    selectedItem: SettingsTabItem,
    onBackClick: () -> Unit,
    onSelectTab: (SettingsTabItem) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = content,
        topBar = {
            Column {
                TabNavigationTopBar(onBackClick = onBackClick)
                TabNavigationRow(
                    selectedItem = selectedItem,
                    onSelect = onSelectTab,
                    layoutMode = SettingsLayoutMode.COMPACT,
                )
            }
        },
        contentWindowInsets = WindowInsets(0.dp),
    )
}

@Composable
private fun SettingsExpandedLayout(
    modifier: Modifier = Modifier,
    selectedItem: SettingsTabItem,
    onBackClick: () -> Unit,
    onSelectTab: (SettingsTabItem) -> Unit,
    content: @Composable (PaddingValues) -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        content = content,
        topBar = {
            Column {
                TabNavigationExpandedTopBar(onBackClick = onBackClick)
                TabNavigationRow(
                    selectedItem = selectedItem,
                    onSelect = onSelectTab,
                    layoutMode = SettingsLayoutMode.EXPANDED,
                )
            }
        },
        contentWindowInsets = WindowInsets(0.dp),
    )
}
