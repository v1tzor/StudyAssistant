/*
 * Copyright 2023 Stanislav Aleshin
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
package views

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import mappers.pxToDp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import theme.StudyAssistantRes

/**
 * @author Stanislav Aleshin on 08.02.2024.
 */
interface BottomBarItem {
    val label: String @Composable get
    val enabledIcon: DrawableResource @Composable get
    val disabledIcon: DrawableResource @Composable get
    val containerColor: Color @Composable get() = MaterialTheme.colorScheme.background
}

@Composable
fun <Item : BottomBarItem> BottomNavigationBar(
    modifier: Modifier,
    selectedItem: Item,
    items: Array<Item>,
    onItemSelected: (Item) -> Unit,
    showLabel: Boolean,
    windowInsets: WindowInsets = WindowInsets.navigationBars,
) {
    val density = LocalDensity.current
    NavigationBar(
        modifier = modifier.height(
            height = (if (showLabel) 80.dp else 60.dp) + windowInsets.getBottom(density).pxToDp()
        ),
        containerColor = selectedItem.containerColor,
        tonalElevation = StudyAssistantRes.elevations.levelZero,
        windowInsets = windowInsets,
    ) {
        items.forEach { item ->
            NavigationBarItem(
                selected = selectedItem == item,
                onClick = { if (selectedItem != item) onItemSelected.invoke(item) },
                icon = {
                    BottomBarIcon(
                        selected = selectedItem == item,
                        enabledIcon = painterResource(item.enabledIcon),
                        disabledIcon = painterResource(item.disabledIcon),
                        description = item.label,
                    )
                },
                label = if (showLabel) {
                    {
                        BottomBarLabel(
                            selected = selectedItem == item,
                            title = item.label,
                        )
                    }
                } else {
                    null
                },
                colors = NavigationBarItemDefaults.colors(
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            )
        }
    }
}

@Composable
fun BottomBarIcon(
    selected: Boolean,
    enabledIcon: Painter,
    disabledIcon: Painter,
    description: String,
) {
    Icon(
        painter = if (selected) enabledIcon else disabledIcon,
        contentDescription = description,
        tint = when (selected) {
            true -> MaterialTheme.colorScheme.onSecondaryContainer
            false -> MaterialTheme.colorScheme.onSurfaceVariant
        },
    )
}

@Composable
fun BottomBarLabel(
    selected: Boolean,
    title: String,
) {
    Text(
        text = title,
        color = when (selected) {
            true -> MaterialTheme.colorScheme.onSurface
            false -> MaterialTheme.colorScheme.onSurfaceVariant
        },
        style = MaterialTheme.typography.labelMedium,
    )
}