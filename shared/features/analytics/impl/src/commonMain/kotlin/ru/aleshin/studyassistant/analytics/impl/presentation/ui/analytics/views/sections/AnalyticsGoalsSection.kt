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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsGoalDistributionUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsMetricView
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsSectionCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsDuration
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsRate
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_active_timer
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_actual_time
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_completed
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_desired_time
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_goal_homeworks
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_goal_todos
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_goals_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_overdue
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_planned

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsGoalsSection(
    distribution: AnalyticsGoalDistributionUi,
    modifier: Modifier = Modifier,
) {
    AnalyticsSectionCard(
        title = stringResource(Res.string.analytics_goals_title),
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnalyticsMetricView(
                stringResource(Res.string.analytics_planned),
                distribution.planned.toString(),
                Modifier.weight(1f),
            )
            AnalyticsMetricView(
                stringResource(Res.string.analytics_completed),
                distribution.completed.toString(),
                Modifier.weight(1f),
            )
        }
        AnalyticsMetricView(
            stringResource(Res.string.analytics_overdue),
            distribution.overdue.toString(),
            Modifier.fillMaxWidth(),
        )
        LinearProgressIndicator(
            progress = { distribution.completionRate?.coerceIn(0f, 1f) ?: 0f },
            modifier = Modifier.fillMaxWidth(),
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        Text(
            text = formatAnalyticsRate(distribution.completionRate),
            style = MaterialTheme.typography.labelLarge,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnalyticsMetricView(
                stringResource(Res.string.analytics_goal_homeworks),
                distribution.homeworkGoals.toString(),
                Modifier.weight(1f),
            )
            AnalyticsMetricView(
                stringResource(Res.string.analytics_goal_todos),
                distribution.todoGoals.toString(),
                Modifier.weight(1f),
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            AnalyticsMetricView(
                stringResource(Res.string.analytics_desired_time),
                formatAnalyticsDuration(distribution.desiredDuration),
                Modifier.weight(1f),
            )
            AnalyticsMetricView(
                stringResource(Res.string.analytics_actual_time),
                formatAnalyticsDuration(distribution.actualDuration),
                Modifier.weight(1f),
                supportingText = stringResource(Res.string.analytics_active_timer)
                    .takeIf { distribution.hasActiveTimer },
            )
        }
    }
}
