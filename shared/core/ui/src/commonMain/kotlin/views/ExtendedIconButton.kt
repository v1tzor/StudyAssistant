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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import extensions.alphaByEnabled

val SIZE = 40.dp
val SHAPE = CircleShape
const val DisabledIconOpacity = 0.38f

/**
 * @author Stanislav Aleshin on 13.04.2024.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
fun ExtendedIconButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
    onDoubleClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit,
) {
    val containerColor: Color = Color.Transparent
    val contentColor: Color = LocalContentColor.current
    val disabledContainerColor: Color = Color.Transparent
    val disabledContentColor: Color = contentColor.copy(alpha = DisabledIconOpacity)

    val backgroundColor = if (enabled) containerColor else disabledContainerColor

    // Copied from `IconButton` with some modifications
    Box(
        modifier = modifier
            .alphaByEnabled(enabled)
            .minimumInteractiveComponentSize()
            .size(SIZE)
            .clip(SHAPE)
            .background(color = backgroundColor)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick,
                onDoubleClick = onDoubleClick,
                enabled = enabled,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = LocalIndication.current
            ),
        contentAlignment = Alignment.Center,
    ) {
        val color = if (enabled) contentColor else disabledContentColor
        CompositionLocalProvider(LocalContentColor provides color, content = content)
    }
}
