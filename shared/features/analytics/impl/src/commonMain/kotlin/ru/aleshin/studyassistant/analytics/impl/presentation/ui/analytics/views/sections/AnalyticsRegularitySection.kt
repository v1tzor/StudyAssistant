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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CrisisAlert
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsRegularityUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsRegularityChart
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsSectionCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.SectionLabel
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.analyticsWeekdayTitle
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsWorkload
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_chart_regularity_desc
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_completion_days
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_days_suffix
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_frequent_day
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_longest_streak
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_regularity_chart_label
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_regularity_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_study_days

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsRegularitySection(
    regularity: AnalyticsRegularityUi,
    modifier: Modifier = Modifier,
) {
    val chartValues = DayOfWeek.entries.map { day ->
        analyticsWeekdayTitle(dayOfWeek = day, short = true) to (regularity.averageWorkloadByWeekday[day] ?: 0f)
    }
    val chartDescriptions = chartValues.associateWith {
        stringResource(
            Res.string.analytics_chart_regularity_desc,
            it.first,
            formatAnalyticsWorkload(it.second),
        )
    }
    val mostActiveDay = DayOfWeek.entries.maxByOrNull { day ->
        regularity.averageWorkloadByWeekday[day] ?: 0f
    }?.takeIf { day ->
        (regularity.averageWorkloadByWeekday[day] ?: 0f) > 0f
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.analytics_regularity_title),
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SectionLabel(
                    title = stringResource(Res.string.analytics_regularity_chart_label),
                    icon = Icons.Default.ViewWeek,
                )

                AnalyticsRegularityChart(
                    values = chartValues,
                    description = {
                        chartDescriptions[it].orEmpty()
                    },
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    mostActiveDay?.let { day ->
                        RegularityActiveDay(
                            modifier = Modifier.weight(1f),
                            day = analyticsWeekdayTitle(day),
                            workload = regularity.averageWorkloadByWeekday[day] ?: 0f,
                        )
                    }
                    RegularityStudyDays(
                        modifier = Modifier.weight(1f),
                        studyDays = regularity.studyDays.toString(),
                    )
                }
            }
        }
        AnalyticsSectionCard {
            RegularityMetrics(regularity = regularity)
        }
    }
}

@Composable
private fun RegularityStudyDays(
    modifier: Modifier = Modifier,
    studyDays: String,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(Res.string.analytics_study_days),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = studyDays,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun RegularityActiveDay(
    modifier: Modifier = Modifier,
    day: String,
    workload: Float,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.CrisisAlert,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = day,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = buildString {
                    append(stringResource(Res.string.analytics_frequent_day))
                    append(" · ")
                    append(formatAnalyticsWorkload(workload))
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun RegularityMetrics(
    regularity: AnalyticsRegularityUi,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RegularityMetric(
            icon = Icons.Default.TaskAlt,
            value = regularity.completionDays.toString(),
            label = stringResource(
                Res.string.analytics_completion_days,
            ),
            modifier = Modifier.weight(1f),
        )
        VerticalDivider(
            modifier = Modifier.height(44.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
        )
        RegularityMetric(
            icon = Icons.Default.LocalFireDepartment,
            value = buildString {
                append(regularity.longestCompletionStreak)
                append(" ")
                append(stringResource(Res.string.analytics_days_suffix))
            },
            label = stringResource(Res.string.analytics_longest_streak),
            accent = regularity.longestCompletionStreak > 1,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun RegularityMetric(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    accent: Boolean = false,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (accent) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}