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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.core.ui.mappers.toMinutesOrHoursTitle
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.endSide
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.theme.material.startSide
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 14.07.2024.
 */
@Composable
internal fun NumberedDurationView(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    number: String,
    duration: Millis?,
    onNumberClick: (() -> Unit)?,
    onDurationClick: (() -> Unit)?,
    numberedContainerColor: Color = MaterialTheme.colorScheme.primary,
    durationContainerColor: Color = MaterialTheme.colorScheme.primaryContainer,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .animateContentSize()
                .height(32.dp)
                .clip(MaterialTheme.shapes.full.startSide)
                .background(numberedContainerColor)
                .clickable(
                    enabled = enabled && onNumberClick != null,
                    onClick = { if (onNumberClick != null) onNumberClick() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp),
                text = number,
                color = MaterialTheme.colorScheme.contentColorFor(numberedContainerColor),
                style = MaterialTheme.typography.titleSmall,
            )
        }
        Box(
            modifier = Modifier
                .height(32.dp)
                .weight(1f)
                .clip(MaterialTheme.shapes.full.endSide)
                .background(durationContainerColor)
                .clickable(
                    enabled = enabled && onDurationClick != null,
                    onClick = { if (onDurationClick != null) onDurationClick() },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (duration != null) {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = duration.toMinutesOrHoursTitle(),
                    color = MaterialTheme.colorScheme.contentColorFor(durationContainerColor),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                )
            } else {
                Text(
                    modifier = Modifier.padding(horizontal = 8.dp),
                    text = StudyAssistantRes.strings.specifyTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }
    }
}

@Composable
internal fun AddNumberedDurationView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    containerColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    contentColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(28.dp),
        enabled = enabled,
        shape = MaterialTheme.shapes.full,
        color = containerColor,
        interactionSource = interactionSource,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = EditorThemeRes.strings.addTitle,
                color = contentColor,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}