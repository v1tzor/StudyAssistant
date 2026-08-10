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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsInsight
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsInsightUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsSectionCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsPercentage
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsWorkload
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_insight_late
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_insight_organization
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_insight_overload
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_insight_peak
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_insight_subject
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_insights_title
import kotlin.math.roundToInt

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsInsightsSection(
    insights: List<AnalyticsInsightUi>,
    modifier: Modifier = Modifier,
) {
    if (insights.isEmpty()) return
    AnalyticsSectionCard(
        title = stringResource(Res.string.analytics_insights_title),
        modifier = modifier,
    ) {
        insights.forEach { insight ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Default.Insights,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = insightTitle(insight),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
private fun insightTitle(insight: AnalyticsInsightUi): String = when (insight.type) {
    AnalyticsInsight.Type.PEAK_LOAD -> stringResource(
        Res.string.analytics_insight_peak,
        formatAnalyticsWorkload(insight.value),
    )
    AnalyticsInsight.Type.OVERLOAD_DAYS -> stringResource(
        Res.string.analytics_insight_overload,
        insight.value.roundToInt(),
    )
    AnalyticsInsight.Type.LATE_COMPLETION_SHARE -> stringResource(
        Res.string.analytics_insight_late,
        formatAnalyticsPercentage(insight.value.roundToInt()),
    )
    AnalyticsInsight.Type.ORGANIZATION_CONCENTRATION -> stringResource(
        Res.string.analytics_insight_organization,
        formatAnalyticsPercentage(insight.value.roundToInt()),
        insight.name.orEmpty(),
    )
    AnalyticsInsight.Type.SUBJECT_CONCENTRATION -> stringResource(
        Res.string.analytics_insight_subject,
        formatAnalyticsPercentage(insight.value.roundToInt()),
        insight.name.orEmpty(),
    )
}
