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
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsLoadDistributionUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsWorkloadChart
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsWorkload
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_average_workload
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_chart_workload_desc
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_no_overloads
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_overload_days
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_peak_load
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_workload_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_workload_value
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.ui.views.shortDayMonthFormat

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsWorkloadSection(
    distribution: AnalyticsLoadDistributionUi,
    modifier: Modifier = Modifier,
) {
    val descriptions = distribution.buckets.associateWith { bucket ->
        stringResource(
            Res.string.analytics_chart_workload_desc,
            formatAnalyticsWorkload(bucket.workload),
            bucket.classesCount,
            bucket.homeworkCount,
            bucket.todoCount,
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.analytics_workload_title),
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surfaceContainer,
        ) {
            Column(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 16.dp,
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AnalyticsWorkloadChart(
                    buckets = distribution.buckets,
                    description = {
                        descriptions[it].orEmpty()
                    },
                )

                WorkloadHighlights(
                    distribution = distribution,
                )

                WorkloadStatus(
                    daysAboveThreshold = distribution.daysAboveThreshold,
                    threshold = distribution.threshold,
                )
            }
        }
    }
}

@Composable
private fun WorkloadHighlights(
    distribution: AnalyticsLoadDistributionUi,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        WorkloadHighlight(
            icon = Icons.Default.Speed,
            label = stringResource(Res.string.analytics_average_workload),
            value = stringResource(
                Res.string.analytics_workload_value,
                formatAnalyticsWorkload(distribution.averageWorkload),
            ),
            modifier = Modifier.weight(1f),
        )

        distribution.peakBucket?.let { peakBucket ->
            VerticalDivider(
                modifier = Modifier.height(48.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
            WorkloadHighlight(
                icon = Icons.Default.Insights,
                label = stringResource(Res.string.analytics_peak_load),
                value = stringResource(
                    Res.string.analytics_workload_value,
                    formatAnalyticsWorkload(peakBucket.workload),
                ),
                supportingText = peakBucket.from.formatByTimeZone(
                    DateTimeComponents.Formats.shortDayMonthFormat(),
                ),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun WorkloadHighlight(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = MaterialTheme.colorScheme.primary,
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )

            supportingText?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun WorkloadStatus(
    daysAboveThreshold: Int,
    threshold: Int,
) {
    val hasOverload = daysAboveThreshold > 0

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (hasOverload) {
                Icons.Default.WarningAmber
            } else {
                Icons.Default.CheckCircleOutline
            },
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = if (hasOverload) {
                MaterialTheme.colorScheme.error
            } else {
                MaterialTheme.colorScheme.primary
            },
        )

        if (hasOverload) {
            Text(
                text = daysAboveThreshold.toString(),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.error,
            )

            Text(
                text = stringResource(
                    Res.string.analytics_overload_days,
                    threshold,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
            Text(
                text = stringResource(
                    Res.string.analytics_no_overloads,
                ),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}