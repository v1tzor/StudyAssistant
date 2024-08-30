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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.UserCodeView
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.profile.impl.presentation.models.users.AppUserUi

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
@Composable
internal fun ProfileInfoSection(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    profile: AppUserUi?,
) {
    Crossfade(
        targetState = isLoading,
        modifier = Modifier.animateContentSize(),
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            visibilityThreshold = Spring.DefaultDisplacementThreshold,
        )
    ) { loading ->
        Column(
            modifier = modifier.fillMaxWidth().padding(top = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            if (loading) {
                AvatarViewPlaceholder()
                ContactInfoViewPlaceholder()
            } else if (profile != null) {
                AvatarView(
                    modifier = Modifier.size(120.dp),
                    firstName = profile.username.split(' ').getOrNull(0) ?: "*",
                    secondName = profile.username.split(' ').getOrNull(1),
                    imageUrl = profile.avatar,
                    style = MaterialTheme.typography.displaySmall.copy(
                        fontWeight = FontWeight.Medium,
                    ),
                )
                ContactInfoView(
                    username = profile.username,
                    code = profile.code,
                )
            }
        }
    }
}

@Composable
internal fun ContactInfoView(
    modifier: Modifier = Modifier,
    username: String?,
    code: String?,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = username ?: "-",
            color = MaterialTheme.colorScheme.onBackground,
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
        UserCodeView(code = code ?: "-")
    }
}

@Composable
internal fun AvatarViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    PlaceholderBox(
        modifier = modifier.size(120.dp),
        shape = MaterialTheme.shapes.full,
        highlight = null,
    )
}

@Composable
internal fun ContactInfoViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PlaceholderBox(
            modifier = Modifier.size(200.dp, 26.dp),
            highlight = null,
        )
        PlaceholderBox(
            modifier = Modifier.size(93.dp, 24.dp),
            shape = MaterialTheme.shapes.full,
            color = MaterialTheme.colorScheme.secondary,
            highlight = null,
        )
    }
}