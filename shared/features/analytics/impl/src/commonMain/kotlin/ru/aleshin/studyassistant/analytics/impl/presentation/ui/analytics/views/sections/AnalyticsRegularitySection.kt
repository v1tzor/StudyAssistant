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
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsRegularityUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsMetricView
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsRegularityChart
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsSectionCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.analyticsWeekdayTitle
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsWorkload
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_chart_regularity_desc
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_completion_days
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_days_suffix
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_longest_streak
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
        analyticsWeekdayTitle(day, short = true) to (regularity.averageWorkloadByWeekday[day] ?: 0f)
    }
    val chartDescriptions = chartValues.associateWith {
        stringResource(
            Res.string.analytics_chart_regularity_desc,
            it.first,
            formatAnalyticsWorkload(it.second),
        )
    }
    AnalyticsSectionCard(
        title = stringResource(Res.string.analytics_regularity_title),
        modifier = modifier,
    ) {
        AnalyticsRegularityChart(
            values = chartValues,
            description = { chartDescriptions[it].orEmpty() },
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnalyticsMetricView(
                stringResource(Res.string.analytics_study_days),
                regularity.studyDays.toString(),
                Modifier.weight(1f),
            )
            AnalyticsMetricView(
                stringResource(Res.string.analytics_completion_days),
                regularity.completionDays.toString(),
                Modifier.weight(1f),
            )
        }
        AnalyticsMetricView(
            label = stringResource(Res.string.analytics_longest_streak),
            value = buildString {
                append(regularity.longestCompletionStreak)
                append(" ")
                append(stringResource(Res.string.analytics_days_suffix))
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}
