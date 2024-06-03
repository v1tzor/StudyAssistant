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

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

/**
 * @author Stanislav Aleshin on 08.02.2024.
 */
@Composable
fun TopAppBarTitle(
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    header: String,
    title: String? = null,
    headerStyle: TextStyle = MaterialTheme.typography.titleLarge,
    titleStyle: TextStyle = MaterialTheme.typography.titleSmall,
) {
    Column(modifier = modifier) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = header,
            textAlign = textAlign,
            color = MaterialTheme.colorScheme.onBackground,
            style = headerStyle,
        )
        if (title != null) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = title,
                textAlign = textAlign,
                color = MaterialTheme.colorScheme.onBackground,
                style = titleStyle,
            )
        }
    }
}

@Composable
fun TopAppBarEmptyButton(modifier: Modifier = Modifier) {
    Spacer(modifier = modifier.size(48.dp))
}

@Composable
fun TopAppBarButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    imageVector: ImageVector,
    imageDescription: String?,
    onButtonClick: () -> Unit,
    onDoubleButtonClick: (() -> Unit)? = null,
    onLongButtonClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    ExtendedIconButton(
        modifier = modifier.size(48.dp),
        enabled = enabled,
        onClick = onButtonClick,
        onDoubleClick = onDoubleButtonClick,
        onLongClick = onLongButtonClick,
        interactionSource = interactionSource,
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = imageDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
fun TopAppBarButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    imagePainter: Painter,
    imageDescription: String?,
    badge: (@Composable () -> Unit)? = null,
    onButtonClick: () -> Unit,
    onDoubleButtonClick: (() -> Unit)? = null,
    onLongButtonClick: (() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Box {
        if (badge != null) {
            Box(
                modifier = Modifier.padding(top = 4.dp, end = 2.dp).align(Alignment.TopEnd)
            ) { badge() }
        }
        ExtendedIconButton(
            modifier = modifier.size(48.dp),
            enabled = enabled,
            onClick = onButtonClick,
            onDoubleClick = onDoubleButtonClick,
            onLongClick = onLongButtonClick,
            interactionSource = interactionSource,
        ) {
            Icon(
                painter = imagePainter,
                contentDescription = imageDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
fun TopAppBarTextButton(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    TextButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        enabled = enabled,
        interactionSource = interactionSource,
    ) {
        Text(text = text)
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
fun <T : TopAppBarAction> TopAppBarMoreActions(
    modifier: Modifier = Modifier,
    items: Array<T>,
    onItemClick: (T) -> Unit,
    moreIconDescription: String?,
) {
    val expanded = rememberSaveable { mutableStateOf(false) }
    Box(
        modifier = modifier.wrapContentSize(Alignment.TopEnd),
    ) {
        IconButton(onClick = { expanded.value = true }) {
            Icon(
                imageVector = Icons.Filled.MoreVert,
                contentDescription = moreIconDescription,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        DropdownMenu(
            expanded = expanded.value,
            offset = DpOffset(0.dp, 10.dp),
            onDismissRequest = { expanded.value = false },
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = {
                        Text(
                            modifier = Modifier.defaultMinSize(minWidth = 200.dp),
                            text = item.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    },
                    leadingIcon = if (item.icon != null) {
                        {
                            Icon(
                                painter = painterResource(DrawableResource(checkNotNull(item.icon))),
                                contentDescription = item.title,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    } else {
                        null
                    },
                    onClick = {
                        expanded.value = false
                        onItemClick.invoke(item)
                    },
                )
            }
        }
    }
}
