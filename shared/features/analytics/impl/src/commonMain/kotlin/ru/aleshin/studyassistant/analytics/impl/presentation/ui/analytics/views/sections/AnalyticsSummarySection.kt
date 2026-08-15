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

package ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsComparisonUi
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsSummaryUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsDuration
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsRate
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_backlog
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_classes
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_commitments
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_late
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_on_time
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_overdue
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_previous_period
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_scheduled_hours
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_summary_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_upcoming
import kotlin.math.roundToInt

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsSummarySection(
    summary: AnalyticsSummaryUi,
    comparison: AnalyticsComparisonUi,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.analytics_summary_title),
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column {
                SummaryHeadline(
                    modifier = Modifier.padding(16.dp),
                    summary = summary,
                    comparison = comparison,
                )
                StatusDistribution(
                    modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
                    summary = summary
                )
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                SummaryFacts(
                    modifier = Modifier.padding(16.dp),
                    summary = summary
                )
            }
        }
    }
}

@Composable
private fun SummaryHeadline(
    modifier: Modifier = Modifier,
    summary: AnalyticsSummaryUi,
    comparison: AnalyticsComparisonUi,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = stringResource(Res.string.analytics_scheduled_hours),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = formatAnalyticsDuration(summary.plannedDuration),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            comparison.plannedDurationPercent?.let { change ->
                Text(
                    text = change.formatChange(),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        OnTimeIndicator(
            rate = summary.onTimeRate,
            comparison = comparison.onTimeRatePoints,
        )
    }
}

@Composable
private fun OnTimeIndicator(
    modifier: Modifier = Modifier,
    rate: Float?,
    comparison: Float?,
) {
    val progress = rate?.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0f

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Box(
            modifier = Modifier.size(88.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.matchParentSize(),
                strokeWidth = 8.dp,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                Text(
                    text = formatAnalyticsRate(rate),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = stringResource(Res.string.analytics_on_time),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        comparison?.let { change ->
            Text(
                text = change.formatChange(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun StatusDistribution(
    modifier: Modifier = Modifier,
    summary: AnalyticsSummaryUi,
) {
    val statuses = listOf(
        SummaryStatus(
            label = stringResource(Res.string.analytics_on_time),
            value = summary.completedOnTime,
            color = MaterialTheme.colorScheme.primary,
        ),
        SummaryStatus(
            label = stringResource(Res.string.analytics_late),
            value = summary.completedLate,
            color = MaterialTheme.colorScheme.tertiary,
        ),
        SummaryStatus(
            label = stringResource(Res.string.analytics_overdue),
            value = summary.overdue,
            color = MaterialTheme.colorScheme.error,
        ),
        SummaryStatus(
            label = stringResource(Res.string.analytics_upcoming),
            value = summary.upcoming,
            color = MaterialTheme.colorScheme.outline,
        ),
    )
    val visibleStatuses = statuses.filter { it.value > 0 }
    val total = statuses.sumOf { it.value }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatusBar(
            statuses = statuses,
            total = total,
        )
        if (visibleStatuses.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                visibleStatuses.forEach { status ->
                    StatusValue(status = status)
                }
            }
        }
    }
}

@Composable
private fun StatusBar(
    statuses: List<SummaryStatus>,
    total: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        if (total > 0) {
            statuses.forEach { status ->
                if (status.value > 0) {
                    Box(
                        modifier = Modifier
                            .weight(status.value.toFloat())
                            .height(8.dp)
                            .background(status.color),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusValue(
    modifier: Modifier = Modifier,
    status: SummaryStatus,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = status.color,
                content = {},
            )
            Text(
                text = status.value.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SummaryFacts(
    modifier: Modifier = Modifier,
    summary: AnalyticsSummaryUi,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top,
    ) {
        SummaryFact(
            icon = Icons.Default.Event,
            value = summary.classesCount,
            label = stringResource(Res.string.analytics_classes),
            modifier = Modifier.weight(1f),
        )
        SummaryFact(
            icon = Icons.AutoMirrored.Filled.Assignment,
            value = summary.homeworkCount + summary.todoCount,
            label = stringResource(Res.string.analytics_commitments),
            modifier = Modifier.weight(1f),
        )
        if (summary.undatedTodoBacklog > 0) {
            SummaryFact(
                icon = Icons.Default.TaskAlt,
                value = summary.undatedTodoBacklog,
                label = stringResource(Res.string.analytics_backlog),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SummaryFact(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: Int,
    label: String,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Column {
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

private data class SummaryStatus(
    val label: String,
    val value: Int,
    val color: Color,
)

@Composable
private fun Float.formatChange(): String {
    val value = roundToInt()
    val prefix = if (value > 0) "+" else ""
    return "$prefix$value% ${stringResource(Res.string.analytics_previous_period)}"
}
