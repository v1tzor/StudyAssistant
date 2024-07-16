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

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.panpf.sketch.AsyncImage
import com.github.panpf.sketch.rememberAsyncImageState
import com.github.panpf.sketch.request.ComposableImageOptions
import com.github.panpf.sketch.request.error
import com.github.panpf.sketch.request.placeholder
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.UserCodeView
import ru.aleshin.studyassistant.profile.impl.presentation.models.AppUserUi

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
@Composable
internal fun ProfileInfoSection(
    modifier: Modifier = Modifier,
    profile: AppUserUi?,
) {
    AnimatedContent(
        targetState = profile == null,
        transitionSpec = {
            fadeIn(animationSpec = tween(500, delayMillis = 180)).togetherWith(
                fadeOut(animationSpec = tween(500))
            )
        },
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
                    username = profile.username,
                    imageUrl = profile.avatar
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
internal fun AvatarView(
    modifier: Modifier = Modifier,
    username: String?,
    imageUrl: String?,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        modifier = modifier.size(120.dp),
        shape = MaterialTheme.shapes.full,
        color = containerColor,
        contentColor = contentColor,
    ) {
        if (imageUrl != null) {
            AsyncImage(
                uri = imageUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize().clip(MaterialTheme.shapes.full),
                state = rememberAsyncImageState(
                    options = ComposableImageOptions {
                        crossfade(Constants.Animations.STANDARD_TWEEN)
                        placeholder(MaterialTheme.colorScheme.secondaryContainer)
                        error(StudyAssistantRes.icons.testsOutline)
                    }
                ),
            )
        } else {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = username?.substring(0, 2)?.uppercase() ?: "-",
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.displaySmall,
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
            modifier = Modifier.size(200.dp, 28.dp),
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
