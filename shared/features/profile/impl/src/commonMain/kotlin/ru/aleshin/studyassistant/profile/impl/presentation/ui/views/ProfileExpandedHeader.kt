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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.views

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.presentation.models.users.ProfileUi
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun ProfileExpandedHeader(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    profile: ProfileUi?,
) {
    Crossfade(
        modifier = modifier.fillMaxWidth(),
        targetState = isLoading,
        label = "ProfileExpandedHeader",
    ) { loading ->
        if (loading) {
            ProfileExpandedHeaderPlaceholder()
        } else if (profile != null) {
            ProfileExpandedHeaderContent(profile = profile)
        }
    }
}

@Composable
private fun ProfileExpandedHeaderContent(
    modifier: Modifier = Modifier,
    profile: ProfileUi,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        AvatarView(
            modifier = Modifier.size(110.dp),
            firstName = profile.username.split(' ').getOrNull(0) ?: "*",
            secondName = profile.username.split(' ').getOrNull(1),
            imageUrl = profile.avatar,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Medium,
            ),
        )
        Text(
            text = profile.username.ifBlank { "-" },
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
            ),
        )
    }
}

@Composable
private fun ProfileExpandedHeaderPlaceholder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        PlaceholderBox(
            modifier = Modifier.size(110.dp),
            shape = MaterialTheme.shapes.full,
            highlight = null,
        )
        PlaceholderBox(
            modifier = Modifier.size(220.dp, 28.dp),
            highlight = null,
        )
    }
}
