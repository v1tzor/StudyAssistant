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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsLoadDistributionUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsMetricView
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsSectionCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsWorkloadChart
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsWorkload
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_average_workload
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_chart_workload_desc
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
    val descriptions = distribution.buckets.associateWith {
        stringResource(
            Res.string.analytics_chart_workload_desc,
            formatAnalyticsWorkload(it.workload),
            it.classesCount,
            it.homeworkCount,
            it.todoCount,
        )
    }
    AnalyticsSectionCard(
        title = stringResource(Res.string.analytics_workload_title),
        modifier = modifier,
    ) {
        AnalyticsWorkloadChart(
            buckets = distribution.buckets,
            description = { descriptions[it].orEmpty() },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnalyticsMetricView(
                label = stringResource(Res.string.analytics_average_workload),
                value = stringResource(
                    Res.string.analytics_workload_value,
                    formatAnalyticsWorkload(distribution.averageWorkload),
                ),
                modifier = Modifier.weight(1f),
            )
            AnalyticsMetricView(
                label = stringResource(
                    Res.string.analytics_overload_days,
                    distribution.threshold,
                ),
                value = distribution.daysAboveThreshold.toString(),
                modifier = Modifier.weight(1f),
            )
        }
        distribution.peakBucket?.let { peakBucket ->
            AnalyticsMetricView(
                label = stringResource(Res.string.analytics_peak_load),
                value = stringResource(
                    Res.string.analytics_workload_value,
                    formatAnalyticsWorkload(peakBucket.workload),
                ),
                supportingText = peakBucket.from.formatByTimeZone(
                    DateTimeComponents.Formats.shortDayMonthFormat(),
                ),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
