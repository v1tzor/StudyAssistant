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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.ui.mappers.toLanguageString
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import kotlin.time.Duration

/**
 * @author Stanislav Aleshin on 16.08.2024.
 */
@Composable
internal fun SenderUserView(
    onClick: () -> Unit,
    username: String,
    avatar: String?,
    email: String,
    leftTime: Duration,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AvatarView(
                    modifier = Modifier.size(40.dp),
                    firstName = username.split(' ').getOrElse(0) { "-" },
                    secondName = username.split(' ').getOrNull(1),
                    imageUrl = avatar,
                )
                Column {
                    Text(
                        text = username,
                        color = MaterialTheme.colorScheme.onSurface,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Text(
                        text = email,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
            Text(
                text = buildString {
                    append(leftTime.toLanguageString(), " ")
                    append(ScheduleThemeRes.strings.agoSuffix)
                },
                color = MaterialTheme.colorScheme.secondary,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
internal fun SenderUserViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    PlaceholderBox(
        modifier = modifier.fillMaxWidth().height(56.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = MaterialTheme.shapes.large,
    )
}