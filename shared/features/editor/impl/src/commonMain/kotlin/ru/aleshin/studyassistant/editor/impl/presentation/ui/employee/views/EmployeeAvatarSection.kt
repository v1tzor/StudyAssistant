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

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.core.PlatformFile
import ru.aleshin.studyassistant.core.common.extensions.floatSpring
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.menu.SelectableAvatarView

/**
 * @author Stanislav Aleshin on 03.08.2024.
 */
@Composable
internal fun EmployeeAvatarSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    firstName: String?,
    lastName: String?,
    avatar: String?,
    onUpdateAvatar: (PlatformFile) -> Unit,
    onDeleteAvatar: () -> Unit,
    onExceedingAvatarSizeLimit: (Int) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        Crossfade(
            targetState = isLoading,
            animationSpec = floatSpring(),
        ) { loading ->
            if (loading) {
                EmployeeAvatarViewPlaceholder()
            } else {
                SelectableAvatarView(
                    modifier = Modifier.size(90.dp),
                    onSelect = onUpdateAvatar,
                    onDelete = onDeleteAvatar,
                    onExceedingLimit = onExceedingAvatarSizeLimit,
                    firstName = firstName ?: "*",
                    secondName = lastName,
                    imageUrl = avatar,
                    style = MaterialTheme.typography.displaySmall,
                )
            }
        }
    }
}

@Composable
internal fun EmployeeAvatarViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    PlaceholderBox(
        modifier = modifier.size(90.dp),
        shape = MaterialTheme.shapes.full,
        highlight = null,
    )
}