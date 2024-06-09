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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.employee.views

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import theme.StudyAssistantRes
import theme.material.full
import views.PlaceholderBox

/**
 * @author Stanislav Aleshin on 06.06.2024
 */
@Composable
internal fun EmployeeAvatarSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    firstName: String?,
    secondName: String?,
    avatar: String?,
    onUpdate: (String?) -> Unit
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)).togetherWith(
                    fadeOut(animationSpec = tween(300))
                )
            },
        ) { loading ->
            if (loading) {
                AvatarViewPlaceholder()
            } else {
                SelectableAvatarView(
                    firstName = firstName,
                    secondName = secondName,
                    imageUrl = avatar,
                    onClick = {},
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalResourceApi::class)
internal fun SelectableAvatarView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    firstName: String?,
    secondName: String?,
    imageUrl: String?,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Box {
        Surface(
            onClick = onClick,
            modifier = modifier.size(90.dp, 90.dp),
            enabled = enabled,
            shape = MaterialTheme.shapes.full(),
            color = containerColor,
            contentColor = contentColor,
        ) {
            if (imageUrl != null) {
                // TODO Get image with glide
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = buildString {
                            append(firstName?.firstOrNull()?.uppercase() ?: "")
                            append(secondName?.firstOrNull()?.uppercase() ?: "")
                            if (firstName == null && secondName == null) append('-')
                        },
                        style = MaterialTheme.typography.displaySmall,
                    )
                }
            }
        }
        Icon(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(24.dp)
                .clip(MaterialTheme.shapes.full())
                .border(2.dp, MaterialTheme.colorScheme.surfaceContainerLow),
            painter = painterResource(StudyAssistantRes.icons.upload),
            contentDescription = StudyAssistantRes.strings.avatarDesc,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun AvatarViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    PlaceholderBox(
        modifier = modifier.size(90.dp),
        shape = MaterialTheme.shapes.full(),
        highlight = null,
    )
}