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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsComparisonUi
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsSummaryUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsMetricView
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsSectionCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsDuration
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsRate
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_backlog
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_classes
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_commitments
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_completed
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
    AnalyticsSectionCard(
        title = stringResource(Res.string.analytics_summary_title),
        modifier = modifier,
    ) {
        MetricRow(
            firstLabel = stringResource(Res.string.analytics_scheduled_hours),
            firstValue = formatAnalyticsDuration(summary.plannedDuration),
            firstSupporting = comparison.plannedDurationPercent?.formatChange(),
            secondLabel = stringResource(Res.string.analytics_classes),
            secondValue = summary.classesCount.toString(),
        )
        MetricRow(
            firstLabel = stringResource(Res.string.analytics_commitments),
            firstValue = (summary.homeworkCount + summary.todoCount).toString(),
            firstSupporting = comparison.commitmentsPercent?.formatChange(),
            secondLabel = stringResource(Res.string.analytics_completed),
            secondValue = summary.completedCount.toString(),
        )
        MetricRow(
            firstLabel = stringResource(Res.string.analytics_on_time),
            firstValue = formatAnalyticsRate(summary.onTimeRate),
            firstSupporting = comparison.onTimeRatePoints?.formatChange(),
            secondLabel = stringResource(Res.string.analytics_overdue),
            secondValue = summary.overdue.toString(),
            secondSupporting = comparison.overduePercent?.formatChange(),
        )
        StatusBar(summary)
        StatusLegend(summary)
        MetricRow(
            firstLabel = stringResource(Res.string.analytics_upcoming),
            firstValue = summary.upcoming.toString(),
            secondLabel = stringResource(Res.string.analytics_backlog),
            secondValue = summary.undatedTodoBacklog.toString(),
        )
    }
}

@Composable
private fun StatusLegend(summary: AnalyticsSummaryUi) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusLegendItem(
                label = stringResource(Res.string.analytics_on_time),
                value = summary.completedOnTime,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
            )
            StatusLegendItem(
                label = stringResource(Res.string.analytics_late),
                value = summary.completedLate,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatusLegendItem(
                label = stringResource(Res.string.analytics_overdue),
                value = summary.overdue,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.weight(1f),
            )
            StatusLegendItem(
                label = stringResource(Res.string.analytics_upcoming),
                value = summary.upcoming,
                color = MaterialTheme.colorScheme.outlineVariant,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatusLegendItem(
    label: String,
    value: Int,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(
            modifier = Modifier.size(10.dp),
            shape = CircleShape,
            color = color,
            content = {},
        )
        Text(
            text = "$label · $value",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun MetricRow(
    firstLabel: String,
    firstValue: String,
    secondLabel: String,
    secondValue: String,
    firstSupporting: String? = null,
    secondSupporting: String? = null,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnalyticsMetricView(
            label = firstLabel,
            value = firstValue,
            supportingText = firstSupporting,
            modifier = Modifier.weight(1f),
        )
        AnalyticsMetricView(
            label = secondLabel,
            value = secondValue,
            supportingText = secondSupporting,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatusBar(summary: AnalyticsSummaryUi) {
    val total = (summary.completedOnTime + summary.completedLate + summary.overdue + summary.upcoming)
        .coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth().height(12.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        listOf(
            summary.completedOnTime to MaterialTheme.colorScheme.primary,
            summary.completedLate to MaterialTheme.colorScheme.tertiary,
            summary.overdue to MaterialTheme.colorScheme.error,
            summary.upcoming to MaterialTheme.colorScheme.outlineVariant,
        ).forEach { (value, color) ->
            if (value > 0) {
                Surface(
                    modifier = Modifier.weight(value.toFloat() / total).height(12.dp),
                    shape = MaterialTheme.shapes.small,
                    color = color,
                    content = {},
                )
            }
        }
    }
}

@Composable
private fun Float.formatChange(): String {
    val value = roundToInt()
    val prefix = if (value > 0) "+" else ""
    return "$prefix$value% ${stringResource(Res.string.analytics_previous_period)}"
}
