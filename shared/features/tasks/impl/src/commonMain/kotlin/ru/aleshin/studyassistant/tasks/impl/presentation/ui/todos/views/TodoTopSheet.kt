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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.todos.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents
import ru.aleshin.studyassistant.core.common.extensions.alphaByEnabled
import ru.aleshin.studyassistant.core.common.extensions.calculateProgress
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.ui.mappers.format
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.bottomSide
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.shortDayMonthFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 03.07.2024.
 */
@Composable
internal fun TodoTopSheet(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    selectedTimeRange: TimeRange?,
    progressList: List<Boolean>,
    onNextTimeRange: () -> Unit,
    onPreviousTimeRange: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge.bottomSide,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TodoTimeRangeSelector(
                enabled = !isLoading,
                selectedTimeRange = selectedTimeRange,
                onNext = onNextTimeRange,
                onPrevious = onPreviousTimeRange,
            )
            TodoProgressView(
                modifier = Modifier.weight(1f),
                isLoading = isLoading,
                progressList = progressList,
            )
        }
    }
}

@Composable
internal fun TodoTimeRangeSelector(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedTimeRange: TimeRange?,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.full,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onPrevious,
                modifier = Modifier.size(36.dp),
                enabled = enabled,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                    contentDescription = null,
                )
            }
            Text(
                modifier = Modifier.alphaByEnabled(enabled).animateContentSize(),
                text = selectedTimeRange?.format(
                    fromDateTimeFormat = DateTimeComponents.Formats.shortDayMonthFormat(),
                    toDateTimeFormat = DateTimeComponents.Formats.shortDayMonthFormat(),
                ) ?: StudyAssistantRes.strings.notSelectedTitle,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(
                onClick = onNext,
                modifier = Modifier.size(36.dp),
                enabled = enabled,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun TodoProgressView(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    progressList: List<Boolean>,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = TasksThemeRes.strings.overallHomeworksProgress,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
        )
        Crossfade(
            targetState = isLoading,
            animationSpec = spring(
                stiffness = Spring.StiffnessMediumLow,
                visibilityThreshold = Spring.DefaultDisplacementThreshold,
            ),
        ) { loading ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (!loading) {
                    val progress by animateFloatAsState(targetValue = progressList.calculateProgress { it })
                    LinearProgressIndicator(
                        modifier = Modifier.height(10.dp).weight(1f).clip(MaterialTheme.shapes.full),
                        progress = { progress },
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        strokeCap = StrokeCap.Square,
                        gapSize = 0.dp,
                        drawStopIndicator = {},
                    )
                    Text(
                        text = buildString {
                            append(progressList.count { it }, "/", progressList.size)
                        },
                        color = MaterialTheme.colorScheme.primary,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                } else {
                    PlaceholderBox(
                        modifier = Modifier.fillMaxWidth().height(10.dp),
                        shape = MaterialTheme.shapes.full,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                    Text(text = " ", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}