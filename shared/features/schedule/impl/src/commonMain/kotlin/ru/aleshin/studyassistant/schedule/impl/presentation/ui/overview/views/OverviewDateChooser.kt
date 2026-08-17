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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.shortWeekdayDayMonthFormat
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.next_date_desc
import ru.aleshin.studyassistant.schedule.impl.resources.previous_date_desc

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun OverviewDateChooser(
    modifier: Modifier = Modifier,
    selectedDate: Instant?,
    enabled: Boolean = true,
    onDateChange: (Instant) -> Unit,
    onOpenCalendar: () -> Unit,
) {
    val dateFormat = DateTimeComponents.Formats.shortWeekdayDayMonthFormat()
    val dateTitle = remember(selectedDate, dateFormat) {
        selectedDate?.formatByTimeZone(dateFormat).orEmpty()
    }

    Surface(
        modifier = modifier.height(38.dp),
        shape = MaterialTheme.shapes.full,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            modifier = Modifier.fillMaxHeight(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OverviewDateChooserIcon(
                icon = Icons.AutoMirrored.Filled.NavigateBefore,
                description = stringResource(Res.string.previous_date_desc),
                enabled = enabled && selectedDate != null,
                onClick = {
                    selectedDate?.let { date ->
                        onDateChange(date.shiftDay(amount = -1))
                    }
                },
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clip(MaterialTheme.shapes.medium)
                    .clickable(
                        enabled = enabled,
                        role = Role.Button,
                        onClick = onOpenCalendar,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .graphicsLayer(alpha = if (enabled) 1f else 0.5f),
                    text = dateTitle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            OverviewDateChooserIcon(
                icon = Icons.AutoMirrored.Filled.NavigateNext,
                description = stringResource(Res.string.next_date_desc),
                enabled = enabled && selectedDate != null,
                onClick = {
                    selectedDate?.let { date ->
                        onDateChange(date.shiftDay(amount = 1))
                    }
                },
            )
        }
    }
}

@Composable
private fun OverviewDateChooserIcon(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    description: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    IconButton(
        modifier = modifier.size(38.dp),
        enabled = enabled,
        onClick = onClick,
    ) {
        Icon(
            modifier = Modifier
                .size(18.dp)
                .graphicsLayer(alpha = if (enabled) 1f else 0.5f),
            imageVector = icon,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface,
        )
    }
}
