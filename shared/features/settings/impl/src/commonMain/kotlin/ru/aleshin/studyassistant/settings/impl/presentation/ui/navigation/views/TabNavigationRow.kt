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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.views

import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.SettingsTabItem

/**
 * @author Stanislav Aleshin on 08.07.2024.
 */
@Composable
internal fun TabNavigationRow(
    modifier: Modifier = Modifier,
    selectedItem: SettingsTabItem,
    onSelect: (SettingsTabItem) -> Unit,
) {
    ScrollableTabRow(
        selectedTabIndex = selectedItem.index,
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        edgePadding = 8.dp,
        indicator = { tabPositions ->
            TabRowDefaults.PrimaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedItem.index]))
        },
    ) {
        SettingsTabItem.entries.forEach { tabItem ->
            Tab(
                selected = tabItem == selectedItem,
                onClick = { onSelect(tabItem) },
                text = {
                    Text(text = tabItem.title)
                },
                icon = if (tabItem.icon != null) {
                    {
                        Icon(
                            painter = checkNotNull(tabItem.icon),
                            contentDescription = null
                        )
                    }
                } else {
                    null
                },
                selectedContentColor = MaterialTheme.colorScheme.primary,
                unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}