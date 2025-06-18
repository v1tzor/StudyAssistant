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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.profile.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.core.PlatformFile
import ru.aleshin.studyassistant.core.ui.theme.material.bottomSide
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.FreeOrPaidContent
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.UserCodeView
import ru.aleshin.studyassistant.core.ui.views.menu.ClickableAvatarView
import ru.aleshin.studyassistant.core.ui.views.menu.SelectableAvatarView
import ru.aleshin.studyassistant.editor.impl.presentation.models.users.AppUserUi

/**
 * @author Stanislav Aleshin on 28.07.2024.
 */
@Composable
internal fun ProfileTopSheet(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    isPaidUser: Boolean?,
    appUser: AppUserUi?,
    onUpdateAvatar: (PlatformFile) -> Unit,
    onDeleteAvatar: () -> Unit,
    onExceedingLimit: (Int) -> Unit,
    onOpenBillingScreen: () -> Unit,
) {
    FreeOrPaidContent(
        isPaidUser = isPaidUser,
        isLoading = isLoading,
        modifier = modifier,
        placeholders = {
            ProfileTopSheetPlaceholders()
        },
        paidContent = {
            ProfileTopSheetPaid(
                appUser = appUser,
                onUpdateAvatar = onUpdateAvatar,
                onDeleteAvatar = onDeleteAvatar,
                onExceedingLimit = onExceedingLimit,
            )
        },
        freeContent = {
            ProfileTopSheetFree(
                appUser = appUser,
                onClick = onOpenBillingScreen,
            )
        },
    )
}

@Composable
private fun ProfileTopSheetFree(
    modifier: Modifier = Modifier,
    appUser: AppUserUi?,
    onClick: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge.bottomSide,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (appUser != null) {
                ClickableAvatarView(
                    onClick = onClick,
                    modifier = Modifier.size(120.dp),
                    imageUrl = appUser.avatar,
                    sideIcon = {
                        Icon(
                            modifier = Modifier.clip(MaterialTheme.shapes.full),
                            imageVector = Icons.Default.Stars,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    firstName = appUser.username.split(' ').getOrNull(0) ?: "-",
                    secondName = appUser.username.split(' ').getOrNull(1),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    iconOffset = DpOffset((-4).dp, (-4).dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserCodeView(code = appUser.code)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = appUser.username,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = appUser.email,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTopSheetPaid(
    modifier: Modifier = Modifier,
    appUser: AppUserUi?,
    onUpdateAvatar: (PlatformFile) -> Unit,
    onDeleteAvatar: () -> Unit,
    onExceedingLimit: (Int) -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge.bottomSide,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (appUser != null) {
                SelectableAvatarView(
                    onSelect = onUpdateAvatar,
                    onDelete = onDeleteAvatar,
                    onExceedingLimit = onExceedingLimit,
                    modifier = Modifier.size(120.dp),
                    imageUrl = appUser.avatar,
                    firstName = appUser.username.split(' ').getOrNull(0) ?: "-",
                    secondName = appUser.username.split(' ').getOrNull(1),
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    iconOffset = DpOffset((-4).dp, (-4).dp),
                )
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    UserCodeView(code = appUser.code)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = appUser.username,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                modifier = Modifier.size(18.dp),
                                imageVector = Icons.Outlined.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = appUser.email,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileTopSheetPlaceholders(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge.bottomSide,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 12.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaceholderBox(
                modifier = Modifier.size(120.dp),
                shape = MaterialTheme.shapes.full,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PlaceholderBox(
                    modifier = Modifier.size(100.dp, 24.dp),
                    shape = MaterialTheme.shapes.full,
                    color = MaterialTheme.colorScheme.primaryContainer,
                )
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    PlaceholderBox(
                        modifier = Modifier.size(195.dp, 28.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                    PlaceholderBox(
                        modifier = Modifier.size(170.dp, 20.dp),
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.surfaceContainer,
                    )
                }
            }
        }
    }
}

// @Composable
// internal fun ProfileTopSheet(
//    modifier: Modifier = Modifier,
//    isLoading: Boolean,
//    isPaidUser: Boolean?,
//    appUser: AppUserUi?,
//    onUpdateAvatar: (PlatformFile) -> Unit,
//    onDeleteAvatar: () -> Unit,
//    onExceedingLimit: (Int) -> Unit,
// ) {
//    Surface(
//        modifier = modifier.fillMaxWidth(),
//        shape = MaterialTheme.shapes.extraLarge.bottomSide,
//        color = MaterialTheme.colorScheme.surfaceContainerLow,
//    ) {
//        Crossfade(
//            targetState = isLoading,
//            animationSpec = spring(
//                stiffness = Spring.StiffnessMediumLow,
//                visibilityThreshold = Spring.DefaultDisplacementThreshold,
//            ),
//        ) { loading ->
//            Row(
//                modifier = Modifier.padding(start = 16.dp, end = 12.dp, bottom = 12.dp),
//                horizontalArrangement = Arrangement.spacedBy(16.dp),
//                verticalAlignment = Alignment.CenterVertically,
//            ) {
//                if (!loading && appUser != null) {
//                    SelectableAvatarView(
//                        onSelect = onUpdateAvatar,
//                        onDelete = onDeleteAvatar,
//                        onExceedingLimit = onExceedingLimit,
//                        modifier = Modifier.size(120.dp),
//                        imageUrl = appUser.avatar,
//                        firstName = appUser.username.split(' ').getOrNull(0) ?: "-",
//                        secondName = appUser.username.split(' ').getOrNull(1),
//                        style = MaterialTheme.typography.displaySmall.copy(
//                            fontWeight = FontWeight.SemiBold
//                        ),
//                        iconOffset = DpOffset((-4).dp, (-4).dp),
//                    )
//                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                        UserCodeView(code = appUser.code)
//                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                            Text(
//                                text = appUser.username,
//                                color = MaterialTheme.colorScheme.onSurface,
//                                fontWeight = FontWeight.Bold,
//                                overflow = TextOverflow.Ellipsis,
//                                maxLines = 2,
//                                style = MaterialTheme.typography.titleLarge,
//                            )
//                            Row(
//                                horizontalArrangement = Arrangement.spacedBy(4.dp),
//                                verticalAlignment = Alignment.CenterVertically,
//                            ) {
//                                Icon(
//                                    modifier = Modifier.size(18.dp),
//                                    imageVector = Icons.Outlined.Email,
//                                    contentDescription = null,
//                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
//                                )
//                                Text(
//                                    text = appUser.email,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                    overflow = TextOverflow.Ellipsis,
//                                    maxLines = 1,
//                                    style = MaterialTheme.typography.labelLarge,
//                                )
//                            }
//                        }
//                    }
//                } else {
//                    PlaceholderBox(
//                        modifier = Modifier.size(120.dp),
//                        shape = MaterialTheme.shapes.full,
//                        color = MaterialTheme.colorScheme.surfaceContainer,
//                    )
//                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
//                        PlaceholderBox(
//                            modifier = Modifier.size(100.dp, 24.dp),
//                            shape = MaterialTheme.shapes.full,
//                            color = MaterialTheme.colorScheme.primaryContainer,
//                        )
//                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
//                            PlaceholderBox(
//                                modifier = Modifier.size(195.dp, 28.dp),
//                                shape = MaterialTheme.shapes.small,
//                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
//                            )
//                            PlaceholderBox(
//                                modifier = Modifier.size(170.dp, 20.dp),
//                                shape = MaterialTheme.shapes.small,
//                                color = MaterialTheme.colorScheme.surfaceContainer,
//                            )
//                        }
//                    }
//                }
//            }
//        }
//    }
// }