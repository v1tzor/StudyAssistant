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
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsTaskDistributionUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsCompletionChart
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsMetricView
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsSectionCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsTaskDonutChart
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_chart_completion_desc
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_completion_dynamics
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_high_priority
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_homeworks
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_invalid_completion
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_late
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_medium_priority
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_on_time
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_overdue
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_practice
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_presentations
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_standard_priority
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_status_distribution
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_task_structure
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_tasks_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_tests
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_theory
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_todo_priorities
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_todos
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_upcoming
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.ui.views.shortDayMonthFormat

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsTasksSection(
    distribution: AnalyticsTaskDistributionUi,
    modifier: Modifier = Modifier,
) {
    val completionDescriptions = distribution.buckets.associateWith {
        stringResource(
            Res.string.analytics_chart_completion_desc,
            it.from.formatByTimeZone(DateTimeComponents.Formats.shortDayMonthFormat()),
            it.completedHomeworks,
            it.completedTodos,
        )
    }
    AnalyticsSectionCard(
        title = stringResource(Res.string.analytics_tasks_title),
        modifier = modifier,
    ) {
        Text(
            text = stringResource(Res.string.analytics_status_distribution),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnalyticsTaskDonutChart(
                summary = distribution.summary,
                modifier = Modifier.weight(1f),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusLegend(
                    stringResource(Res.string.analytics_on_time),
                    distribution.summary.completedOnTime,
                    MaterialTheme.colorScheme.primary,
                )
                StatusLegend(
                    stringResource(Res.string.analytics_late),
                    distribution.summary.completedLate,
                    MaterialTheme.colorScheme.tertiary,
                )
                StatusLegend(
                    stringResource(Res.string.analytics_overdue),
                    distribution.summary.overdue,
                    MaterialTheme.colorScheme.error,
                )
                StatusLegend(
                    stringResource(Res.string.analytics_upcoming),
                    distribution.summary.upcoming,
                    MaterialTheme.colorScheme.outline,
                )
            }
        }
        Text(
            text = stringResource(Res.string.analytics_completion_dynamics),
            style = MaterialTheme.typography.titleMedium,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LineLegend(
                stringResource(Res.string.analytics_homeworks),
                MaterialTheme.colorScheme.primary,
            )
            LineLegend(
                stringResource(Res.string.analytics_todos),
                MaterialTheme.colorScheme.tertiary,
            )
        }
        AnalyticsCompletionChart(
            buckets = distribution.buckets,
            description = { completionDescriptions[it].orEmpty() },
        )
        Text(
            text = stringResource(Res.string.analytics_task_structure),
            style = MaterialTheme.typography.titleMedium,
        )
        MetricPair(
            stringResource(Res.string.analytics_tests),
            distribution.testsCount,
            stringResource(Res.string.analytics_theory),
            distribution.theoreticalPartsCount,
        )
        MetricPair(
            stringResource(Res.string.analytics_practice),
            distribution.practicalPartsCount,
            stringResource(Res.string.analytics_presentations),
            distribution.presentationPartsCount,
        )
        Text(
            text = stringResource(Res.string.analytics_todo_priorities),
            style = MaterialTheme.typography.titleMedium,
        )
        MetricPair(
            stringResource(Res.string.analytics_standard_priority),
            distribution.standardTodos,
            stringResource(Res.string.analytics_medium_priority),
            distribution.mediumPriorityTodos,
        )
        AnalyticsMetricView(
            label = stringResource(Res.string.analytics_high_priority),
            value = distribution.highPriorityTodos.toString(),
            modifier = Modifier.fillMaxWidth(),
        )
        if (distribution.summary.missingCompleteDate > 0) {
            AnalyticsMetricView(
                label = stringResource(Res.string.analytics_invalid_completion),
                value = distribution.summary.missingCompleteDate.toString(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LineLegend(label: String, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = color, content = {})
        Text(label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun StatusLegend(label: String, value: Int, color: Color) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Surface(modifier = Modifier.size(10.dp), shape = CircleShape, color = color, content = {})
        Text("$label · $value", style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun MetricPair(
    firstLabel: String,
    firstValue: Int,
    secondLabel: String,
    secondValue: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnalyticsMetricView(firstLabel, firstValue.toString(), Modifier.weight(1f))
        AnalyticsMetricView(secondLabel, secondValue.toString(), Modifier.weight(1f))
    }
}
