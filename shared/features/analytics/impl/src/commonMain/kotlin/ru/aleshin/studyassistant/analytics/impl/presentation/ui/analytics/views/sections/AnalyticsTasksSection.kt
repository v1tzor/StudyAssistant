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
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsTaskDistributionUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsCompletionChart
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsDistributionRow
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsSectionCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsTaskDonutChart
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.SectionLabel
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.analytics_tasks_title),
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        AnalyticsTaskStatusCard(distribution = distribution)
        AnalyticsTaskDynamicsCard(distribution = distribution)
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Top,
        ) {
            AnalyticsTaskStructureCard(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                distribution = distribution,
            )
            AnalyticsTodoPrioritiesCard(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                distribution = distribution,
            )
            if (distribution.summary.missingCompleteDate > 0) {
                AnalyticsInvalidCompletionCard(distribution = distribution)
            }
        }
    }
}

@Composable
internal fun AnalyticsTaskStatusCard(
    modifier: Modifier = Modifier,
    distribution: AnalyticsTaskDistributionUi,
) {
    AnalyticsSectionCard(modifier = modifier) {
        SectionLabel(
            title = stringResource(Res.string.analytics_status_distribution),
            icon = Icons.Default.PieChart,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AnalyticsTaskDonutChart(
                summary = distribution.summary,
                modifier = Modifier.weight(0.8f),
            )
            Column(
                modifier = Modifier.weight(1.2f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                StatusLegend(
                    label = stringResource(Res.string.analytics_on_time),
                    value = distribution.summary.completedOnTime,
                    color = MaterialTheme.colorScheme.primary,
                )
                StatusLegend(
                    label = stringResource(Res.string.analytics_late),
                    value = distribution.summary.completedLate,
                    color = MaterialTheme.colorScheme.tertiary,
                )
                StatusLegend(
                    label = stringResource(Res.string.analytics_overdue),
                    value = distribution.summary.overdue,
                    color = MaterialTheme.colorScheme.error,
                )
                StatusLegend(
                    label = stringResource(Res.string.analytics_upcoming),
                    value = distribution.summary.upcoming,
                    color = MaterialTheme.colorScheme.outline,
                )
            }
        }
    }
}

@Composable
internal fun AnalyticsTaskDynamicsCard(
    modifier: Modifier = Modifier,
    distribution: AnalyticsTaskDistributionUi,
) {
    val completionDescriptions = distribution.buckets.associateWith {
        stringResource(
            Res.string.analytics_chart_completion_desc,
            it.from.formatByTimeZone(DateTimeComponents.Formats.shortDayMonthFormat()),
            it.completedHomeworks,
            it.completedTodos,
        )
    }
    AnalyticsSectionCard(modifier = modifier) {
        SectionLabel(
            title = stringResource(Res.string.analytics_completion_dynamics),
            icon = Icons.AutoMirrored.Filled.ShowChart,
        )
        AnalyticsCompletionChart(
            buckets = distribution.buckets,
            description = { completionDescriptions[it].orEmpty() },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            LineLegend(
                label = stringResource(Res.string.analytics_homeworks),
                color = MaterialTheme.colorScheme.primary,
            )
            LineLegend(
                label = stringResource(Res.string.analytics_todos),
                color = MaterialTheme.colorScheme.tertiary,
            )
        }
    }
}

@Composable
internal fun AnalyticsTaskStructureCard(
    modifier: Modifier = Modifier,
    distribution: AnalyticsTaskDistributionUi,
) {
    AnalyticsSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel(
                title = stringResource(Res.string.analytics_task_structure),
                icon = Icons.Default.Checklist,
            )
            DistributionGroup(
                entries = listOf(
                    Triple(
                        stringResource(Res.string.analytics_tests),
                        distribution.testsCount,
                        MaterialTheme.colorScheme.primary,
                    ),
                    Triple(
                        stringResource(Res.string.analytics_theory),
                        distribution.theoreticalPartsCount,
                        MaterialTheme.colorScheme.secondary,
                    ),
                    Triple(
                        stringResource(Res.string.analytics_practice),
                        distribution.practicalPartsCount,
                        MaterialTheme.colorScheme.tertiary,
                    ),
                    Triple(
                        stringResource(Res.string.analytics_presentations),
                        distribution.presentationPartsCount,
                        MaterialTheme.colorScheme.outline,
                    ),
                ),
            )
        }
    }
}

@Composable
internal fun AnalyticsTodoPrioritiesCard(
    modifier: Modifier = Modifier,
    distribution: AnalyticsTaskDistributionUi,
) {
    AnalyticsSectionCard(modifier = modifier) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            SectionLabel(
                title = stringResource(Res.string.analytics_todo_priorities),
                icon = Icons.Default.Flag,
            )
            DistributionGroup(
                entries = listOf(
                    Triple(
                        stringResource(Res.string.analytics_standard_priority),
                        distribution.standardTodos,
                        MaterialTheme.colorScheme.primary,
                    ),
                    Triple(
                        stringResource(Res.string.analytics_medium_priority),
                        distribution.mediumPriorityTodos,
                        MaterialTheme.colorScheme.tertiary,
                    ),
                    Triple(
                        stringResource(Res.string.analytics_high_priority),
                        distribution.highPriorityTodos,
                        MaterialTheme.colorScheme.error,
                    ),
                ),
            )
        }
    }
}

@Composable
internal fun AnalyticsInvalidCompletionCard(
    modifier: Modifier = Modifier,
    distribution: AnalyticsTaskDistributionUi,
) {
    AnalyticsSectionCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.ErrorOutline,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = buildString {
                    append(stringResource(Res.string.analytics_invalid_completion))
                    append(" · ")
                    append(distribution.summary.missingCompleteDate)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun DistributionGroup(
    modifier: Modifier = Modifier,
    entries: List<Triple<String, Int, Color>>
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val total = entries.sumOf { it.second }
        entries.forEach { (label, value, color) ->
            AnalyticsDistributionRow(
                label = label,
                value = value,
                total = total,
                color = color,
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
        Surface(
            modifier = Modifier.size(8.dp),
            shape = CircleShape,
            color = color,
            content = {}
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun StatusLegend(label: String, value: Int, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.size(8.dp),
                shape = CircleShape,
                color = color,
                content = {},
            )
            Text(
                modifier = Modifier.weight(1f),
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelLarge,
            maxLines = 1,
        )
    }
}
