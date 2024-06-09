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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.subject.views

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import extensions.alphaByEnabled
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes
import theme.material.full
import theme.tokens.CustomColors

/**
 * @author Stanislav Aleshin on 05.06.2024.
 */
@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun ColorInfoField(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    color: Int?,
    onSelected: (Int?) -> Unit,
) {
    Row(
        modifier = modifier.padding(start = 16.dp, end = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(modifier = Modifier.padding(top = 5.dp)) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(EditorThemeRes.icons.color),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Box(modifier = Modifier.padding(top = 5.dp)) {
            Surface(
                modifier = Modifier.alphaByEnabled(!isLoading).height(96.dp),
                shape = MaterialTheme.shapes.large,
                color = Color.Transparent,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            ) {
                LazyRow(
                    modifier = Modifier.height(64.dp).fillMaxWidth().padding(horizontal = 12.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    userScrollEnabled = !isLoading,
                ) {
                    items(CustomColors.entries) { customColor ->
                        val darkColor = Color(customColor.dark)
                        val lightColor = Color(customColor.light)
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            ColorView(
                                enabled = !isLoading,
                                color = darkColor,
                                selected = color == darkColor.toArgb(),
                                onClick = { onSelected(darkColor.toArgb()) },
                            )
                            ColorView(
                                enabled = !isLoading,
                                color = lightColor,
                                selected = color == lightColor.toArgb(),
                                onClick = { onSelected(lightColor.toArgb()) },
                            )
                        }
                    }
                }
            }
            Surface(
                modifier = Modifier.offset(x = 16.dp, y = (-8).dp),
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.background,
            ) {
                Text(
                    modifier = Modifier.padding(horizontal = 4.dp),
                    text = EditorThemeRes.strings.colorPickerLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}

@Composable
internal fun ColorView(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) = Box(
    modifier = modifier
        .size(28.dp)
        .clip(MaterialTheme.shapes.full())
        .background(color)
        .clickable(
            interactionSource = interactionSource,
            indication = LocalIndication.current,
            enabled = enabled,
            onClick = onClick,
        )
) {
    AnimatedVisibility(
        modifier = Modifier.align(Alignment.Center),
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut(),
        visible = selected,
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color.White,
        )
    }
}
