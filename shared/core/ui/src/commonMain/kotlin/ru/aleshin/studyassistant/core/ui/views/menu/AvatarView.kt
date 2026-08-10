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

package ru.aleshin.studyassistant.core.ui.views.menu

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.AsyncImageState
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.github.panpf.sketch.request.placeholder
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.dialogs.FileKitType
import io.github.vinceglb.filekit.dialogs.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.add_title as core_add_title
import ru.aleshin.studyassistant.core.ui.resources.avatar_desc as core_avatar_desc
import ru.aleshin.studyassistant.core.ui.resources.change_confirm_title as core_change_confirm_title
import ru.aleshin.studyassistant.core.ui.resources.delete_confirm_title as core_delete_confirm_title
import ru.aleshin.studyassistant.core.ui.resources.ic_upload_circular as core_ic_upload_circular

/**
 * @author Stanislav Aleshin on 20.07.2024.
 */
@Composable
fun AvatarView(
    modifier: Modifier = Modifier,
    state: AsyncImageState = rememberAsyncImageState(
        options = ComposableImageOptions {
            crossfade(Constants.Animations.STANDARD_TWEEN)
            placeholder(MaterialTheme.colorScheme.secondaryContainer)
        }
    ),
    firstName: String,
    secondName: String?,
    imageUrl: String?,
    shape: Shape = MaterialTheme.shapes.full,
    sideIcon: @Composable (() -> Unit)? = null,
    iconPosition: Alignment = Alignment.BottomEnd,
    iconOffset: DpOffset = DpOffset(0.dp, 0.dp),
    contentScale: ContentScale = ContentScale.Crop,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Box {
        Surface(
            modifier = modifier.defaultMinSize(48.dp, 48.dp),
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    uri = imageUrl,
                    contentDescription = firstName,
                    modifier = Modifier.fillMaxSize().clip(shape),
                    state = state,
                    contentScale = contentScale,
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = buildString {
                            append(firstName.firstOrNull()?.uppercase() ?: "")
                            if (!secondName.isNullOrBlank()) append(secondName.first().uppercase())
                        },
                        style = style,
                    )
                }
            }
        }
        Box(modifier = Modifier.align(iconPosition).offset(iconOffset.x, iconOffset.y)) {
            sideIcon?.invoke()
        }
    }
}

@Composable
fun ClickableAvatarView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    state: AsyncImageState = rememberAsyncImageState(
        options = ComposableImageOptions {
            crossfade(Constants.Animations.STANDARD_TWEEN)
            placeholder(MaterialTheme.colorScheme.secondaryContainer)
        }
    ),
    firstName: String,
    secondName: String?,
    imageUrl: String?,
    shape: Shape = MaterialTheme.shapes.full,
    sideIcon: @Composable (() -> Unit)? = null,
    iconPosition: Alignment = Alignment.BottomEnd,
    iconOffset: DpOffset = DpOffset(0.dp, 0.dp),
    contentScale: ContentScale = ContentScale.Crop,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
) {
    Box {
        Surface(
            onClick = onClick,
            modifier = modifier.defaultMinSize(48.dp, 48.dp),
            enabled = enabled,
            shape = shape,
            color = containerColor,
            contentColor = contentColor,
        ) {
            if (imageUrl != null) {
                AsyncImage(
                    uri = imageUrl,
                    contentDescription = firstName,
                    modifier = Modifier.fillMaxSize().clip(shape),
                    state = state,
                    contentScale = contentScale,
                )
            } else {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = buildString {
                            append(firstName.firstOrNull()?.uppercase() ?: "")
                            if (!secondName.isNullOrBlank()) append(secondName.first().uppercase())
                        },
                        style = style,
                    )
                }
            }
        }
        Box(modifier = Modifier.align(iconPosition).offset(iconOffset.x, iconOffset.y)) {
            sideIcon?.invoke()
        }
    }
}

@Composable
fun SelectableAvatarView(
    onSelect: (PlatformFile) -> Unit,
    onDelete: () -> Unit,
    onExceedingLimit: (currentSize: Int) -> Unit = {},
    imageByteLimit: Int? = Constants.Image.AVATAR_MAX_SIZE_IN_BYTES,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    state: AsyncImageState = rememberAsyncImageState(
        options = ComposableImageOptions {
            crossfade(Constants.Animations.STANDARD_TWEEN)
            placeholder(MaterialTheme.colorScheme.secondaryContainer)
        }
    ),
    firstName: String,
    secondName: String?,
    imageUrl: String?,
    shape: Shape = MaterialTheme.shapes.full,
    contentScale: ContentScale = ContentScale.Crop,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    iconOffset: DpOffset = DpOffset(0.dp, 0.dp),
) {
    Box {
        var isExpandAvatarMenu by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val imagePickerLauncher = rememberFilePickerLauncher(FileKitType.Image) { file ->
            coroutineScope.launch {
                if (file != null) {
                    val size = file.readBytes().size
                    if (imageByteLimit != null && size > imageByteLimit) {
                        return@launch onExceedingLimit(size)
                    }
                    onSelect(file)
                }
            }
        }

        ClickableAvatarView(
            onClick = { isExpandAvatarMenu = true },
            modifier = modifier.defaultMinSize(90.dp, 90.dp),
            enabled = enabled || imageUrl != null,
            state = state,
            firstName = firstName,
            secondName = secondName,
            imageUrl = imageUrl,
            sideIcon = {
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(MaterialTheme.shapes.full)
                        .border(2.dp, MaterialTheme.colorScheme.surfaceContainerLow),
                    painter = painterResource(CoreRes.drawable.core_ic_upload_circular),
                    contentDescription = stringResource(CoreRes.string.core_avatar_desc),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AvatarDropdownMenu(
                    isExpanded = isExpandAvatarMenu,
                    enabledAdd = enabled,
                    alreadyHaveAvatar = imageUrl != null,
                    onDelete = {
                        onDelete()
                        isExpandAvatarMenu = false
                    },
                    onDismiss = {
                        isExpandAvatarMenu = false
                    },
                    onAddOrUpdate = {
                        imagePickerLauncher.launch()
                        isExpandAvatarMenu = false
                    },
                )
            },
            iconOffset = iconOffset,
            shape = shape,
            contentScale = contentScale,
            containerColor = containerColor,
            contentColor = contentColor,
            style = style,
        )
    }
}

@Composable
internal fun AvatarDropdownMenu(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    enabledAdd: Boolean,
    alreadyHaveAvatar: Boolean,
    onDismiss: () -> Unit,
    onAddOrUpdate: () -> Unit,
    onDelete: () -> Unit,
) {
    DropdownMenu(
        expanded = isExpanded,
        onDismissRequest = onDismiss,
        modifier = modifier.sizeIn(minWidth = 180.dp),
        shape = MaterialTheme.shapes.large,
        offset = DpOffset(0.dp, 6.dp),
    ) {
        if (alreadyHaveAvatar && enabledAdd) {
            DropdownMenuItem(
                onClick = onAddOrUpdate,
                text = { Text(text = stringResource(CoreRes.string.core_change_confirm_title)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                },
            )
        } else if (enabledAdd) {
            DropdownMenuItem(
                onClick = onAddOrUpdate,
                text = { Text(text = stringResource(CoreRes.string.core_add_title)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                },
            )
        }
        if (alreadyHaveAvatar) {
            DropdownMenuItem(
                onClick = onDelete,
                text = { Text(text = stringResource(CoreRes.string.core_delete_confirm_title)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                },
            )
        }
    }
}
