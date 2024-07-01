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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.classes.views

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import extensions.formatByTimeZone
import functional.TimeRange
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 03.06.2024.
 */
@Composable
internal fun ClassTimeRangeChooser(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    currentTime: TimeRange?,
    freeClassTimeRanges: Map<TimeRange, Boolean>?,
    onChoose: (TimeRange) -> Unit,
) {
    LazyRow(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainer),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically,
        userScrollEnabled = enabled,
    ) {
        if (!freeClassTimeRanges.isNullOrEmpty()) {
            items(freeClassTimeRanges.keys.toList()) { timeRange ->
                ClassTimeRangeItem(
                    enabled = enabled,
                    selected = timeRange.timeEquals(currentTime),
                    startTime = timeRange.from,
                    endTime = timeRange.to,
                    isFree = freeClassTimeRanges[timeRange] ?: false,
                    onClick = { onChoose(timeRange) },
                )
            }
        } else {
            item {
                Text(
                    modifier = Modifier.fillParentMaxWidth(),
                    text = EditorThemeRes.strings.notCollectedTimeData,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                    )
                )
            }
        }
    }
}

@Composable
internal fun ClassTimeRangeChooserPlaceholder(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(28.dp),
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                modifier = Modifier.size(18.dp),
                strokeWidth = 3.dp,
            )
        }
    }
}

@Composable
private fun ClassTimeRangeItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selected: Boolean,
    startTime: Instant,
    endTime: Instant,
    isFree: Boolean,
) {
    val timeFormat = DateTimeComponents.Format {
        hour(Padding.NONE)
        char(':')
        minute()
    }
    Box(
        modifier = modifier
            .fillMaxHeight()
            .background(
                color = when {
                    selected && isFree -> MaterialTheme.colorScheme.primary
                    selected && !isFree -> MaterialTheme.colorScheme.errorContainer
                    isFree -> MaterialTheme.colorScheme.primaryContainer
                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                }
            ).clickable(
                enabled = enabled,
                onClick = onClick
            )
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            text = buildString {
                append(startTime.formatByTimeZone(timeFormat))
                append("-")
                append(endTime.formatByTimeZone(timeFormat))
            },
            color = when {
                selected && isFree -> MaterialTheme.colorScheme.onPrimary
                selected && !isFree -> MaterialTheme.colorScheme.onErrorContainer
                isFree -> MaterialTheme.colorScheme.onPrimaryContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            maxLines = 1,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
            )
        )
    }
}