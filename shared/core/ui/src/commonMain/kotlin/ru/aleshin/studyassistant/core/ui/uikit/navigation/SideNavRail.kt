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

package ru.aleshin.studyassistant.core.ui.uikit.navigation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

/**
 * @author Stanislav Aleshin on 12.09.2025.
 */
@Immutable
interface SideNavRailItem {
    val label: String @Composable get
    val icon: DrawableResource @Composable get
}

@Composable
fun <Item : SideNavRailItem> SideNavRail(
    modifier: Modifier = Modifier,
    selectedItem: Item?,
    items: List<Item>,
    onItemSelected: (Item) -> Unit,
    bottomContent: (@Composable () -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.surfaceDim,
    selectedColor: Color = MaterialTheme.colorScheme.onSurface,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        modifier = modifier.fillMaxHeight(),
        color = containerColor,
    ) {
        Column(
            modifier = Modifier.padding(
                start = 8.dp,
                end = 8.dp,
                top = 48.dp,
                bottom = 12.dp,
            ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.width(60.dp).weight(1f)) {
                items.forEach { item ->
                    key(item) {
                        SideNavRailItem(
                            onSelect = { onItemSelected(item) },
                            selected = selectedItem == item,
                            icon = item.icon,
                            label = item.label,
                            selectedColor = selectedColor,
                            unselectedColor = unselectedColor
                        )
                    }
                }
            }
            if (bottomContent != null) {
                bottomContent()
            }
        }
    }
}

@Composable
fun SideNavRailItem(
    enabled: Boolean = true,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier,
    selected: Boolean,
    icon: DrawableResource,
    label: String,
    selectedColor: Color = MaterialTheme.colorScheme.onSurface,
    unselectedColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(
        modifier = modifier
            .size(60.dp)
            .padding(
                start = 6.dp,
                end = 6.dp,
                top = 12.dp,
                bottom = 16.dp,
            )
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                enabled = enabled,
                onClick = onSelect,
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(modifier = Modifier.size(32.dp), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(icon),
                contentDescription = null,
                tint = if (selected) selectedColor else unselectedColor,
            )
        }
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = label,
            color = if (selected) selectedColor else unselectedColor,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}