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

package ru.aleshin.studyassistant.users.impl.presentation.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.github.panpf.sketch.request.error
import com.github.panpf.sketch.request.placeholder
import ru.aleshin.studyassistant.core.common.functional.Constants.Animations.STANDARD_TWEEN
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
@Composable
internal fun UserView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    name: String,
    avatar: String?,
    supportText: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (BoxScope.() -> Unit)? = null,
    actions: @Composable (RowScope.() -> Unit)? = null,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                enabled = enabled,
                onClick = onClick,
            )
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = if (actions != null) Alignment.Top else Alignment.CenterVertically,
    ) {
        UserAvatarView(
            firstName = name.split(' ').getOrElse(0) { "-" },
            secondName = name.split(' ').getOrNull(1),
            imageUrl = avatar,
        )
        Row(
            modifier = Modifier.fillMaxWidth().animateContentSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = name,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 2,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    CompositionLocalProvider(
                        LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        ProvideTextStyle(value = MaterialTheme.typography.bodySmall) {
                            supportText?.invoke()
                        }
                    }
                }
                if (actions != null) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        content = actions,
                    )
                }
            }
            Box(contentAlignment = Alignment.Center) {
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface
                ) {
                    ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
                        trailingIcon?.invoke(this)
                    }
                }
            }
        }
    }
}

@Composable
internal fun UserAvatarView(
    modifier: Modifier = Modifier,
    firstName: String,
    secondName: String?,
    imageUrl: String?,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        modifier = modifier.size(48.dp),
        shape = MaterialTheme.shapes.full,
        color = containerColor,
        contentColor = contentColor,
    ) {
        if (imageUrl != null) {
            AsyncImage(
                uri = imageUrl,
                contentDescription = null,
                modifier = Modifier.size(48.dp).clip(MaterialTheme.shapes.full),
                state = rememberAsyncImageState(
                    options = ComposableImageOptions {
                        crossfade(STANDARD_TWEEN)
                        placeholder(MaterialTheme.colorScheme.secondaryContainer)
                        error(StudyAssistantRes.icons.testsOutline)
                    }
                ),
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = buildString {
                        append(firstName.first().uppercase())
                        if (!secondName.isNullOrBlank()) append(secondName.first().uppercase())
                    },
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
internal fun UserViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth().padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PlaceholderBox(
            modifier = Modifier.size(48.dp),
            shape = MaterialTheme.shapes.full,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        )
        PlaceholderBox(
            modifier = Modifier.height(48.dp).weight(1f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainer,
        )
    }
}