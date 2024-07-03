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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import extensions.alphaByEnabled
import functional.TimeRange
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import mappers.format
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import theme.StudyAssistantRes
import theme.material.full
import views.PlaceholderBox

/**
 * @author Stanislav Aleshin on 03.07.2024.
 */
@Composable
internal fun HomeworksTopSheet(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    selectedTimeRange: TimeRange?,
    progressList: List<Boolean>,
    onNextTimeRange: () -> Unit,
    onPreviousTimeRange: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            HomeworksTimeRangeSelector(
                enabled = !isLoading,
                selectedTimeRange = selectedTimeRange,
                onNext = onNextTimeRange,
                onPrevious = onPreviousTimeRange,
            )
            HomeworksProgressView(
                modifier = Modifier.weight(1f),
                isLoading = isLoading,
                progressList = progressList,
            )
        }
    }
}

@Composable
internal fun HomeworksTimeRangeSelector(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    selectedTimeRange: TimeRange?,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.full(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val dateFormat = DateTimeComponents.Format {
                dayOfMonth()
                char('.')
                monthNumber()
            }
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
                    fromDateTimeFormat = dateFormat,
                    toDateTimeFormat = dateFormat
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
private fun HomeworksProgressView(
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!isLoading) {
                LinearProgressIndicator(
                    modifier = Modifier.height(10.dp).weight(1f).clip(MaterialTheme.shapes.full()),
                    progress = { progressList.count { it } / progressList.size.toFloat() },
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
                    shape = MaterialTheme.shapes.full(),
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}