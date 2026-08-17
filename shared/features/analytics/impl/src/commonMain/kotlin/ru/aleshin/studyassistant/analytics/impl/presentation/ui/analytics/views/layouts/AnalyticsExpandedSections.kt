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

package ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.layouts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsGoalDistributionUi
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsInsightUi
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsLoadDistributionUi
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsTaskDistributionUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsSectionCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsGoalsDurationCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsGoalsProgressCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsInsightsList
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsInvalidCompletionCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsTaskDynamicsCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsTaskStatusCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsTaskStructureCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsTodoPrioritiesCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsWorkloadCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsWorkloadSection
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_goals_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_insights_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_tasks_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_workload_title

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun AnalyticsExpandedWorkloadInsightsRow(
    modifier: Modifier = Modifier,
    workload: AnalyticsLoadDistributionUi,
    insights: List<AnalyticsInsightUi>,
    isBookPosture: Boolean,
) {
    if (insights.isEmpty()) {
        AnalyticsWorkloadSection(
            modifier = modifier
                .widthIn(max = ANALYTICS_SINGLE_CONTENT_MAX_WIDTH)
                .fillMaxWidth(),
            distribution = workload,
        )
        return
    }

    val weights = analyticsWideSplitWeights(isBookPosture)
    AnalyticsSplitRow(
        modifier = modifier,
        leadingWeight = weights.first,
        trailingWeight = weights.second,
        spacing = analyticsSplitSpacing(isBookPosture),
        leading = {
            AnalyticsPaneTitle(stringResource(Res.string.analytics_workload_title))
            AnalyticsWorkloadCard(
                modifier = Modifier.weight(1f),
                distribution = workload,
            )
        },
        trailing = {
            AnalyticsPaneTitle(stringResource(Res.string.analytics_insights_title))
            AnalyticsSectionCard(modifier = Modifier.weight(1f)) {
                AnalyticsInsightsList(insights = insights)
            }
        },
    )
}

@Composable
internal fun AnalyticsExpandedTasksSection(
    modifier: Modifier = Modifier,
    distribution: AnalyticsTaskDistributionUi,
    isBookPosture: Boolean,
) {
    val weights = analyticsBalancedSplitWeights()
    val spacing = analyticsSplitSpacing(isBookPosture)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnalyticsPaneTitle(stringResource(Res.string.analytics_tasks_title))
        AnalyticsSplitRow(
            leadingWeight = weights.first,
            trailingWeight = weights.second,
            spacing = spacing,
            leading = {
                AnalyticsTaskStatusCard(
                    modifier = Modifier.weight(1f),
                    distribution = distribution,
                )
            },
            trailing = {
                AnalyticsTaskDynamicsCard(
                    modifier = Modifier.weight(1f),
                    distribution = distribution,
                )
            },
        )
        AnalyticsSplitRow(
            leadingWeight = weights.first,
            trailingWeight = weights.second,
            spacing = spacing,
            leading = {
                AnalyticsTaskStructureCard(
                    modifier = Modifier.weight(1f),
                    distribution = distribution,
                )
            },
            trailing = {
                AnalyticsTodoPrioritiesCard(
                    modifier = Modifier.weight(1f),
                    distribution = distribution,
                )
            },
        )
        if (distribution.summary.missingCompleteDate > 0) {
            AnalyticsInvalidCompletionCard(distribution = distribution)
        }
    }
}

@Composable
internal fun AnalyticsExpandedGoalsSection(
    modifier: Modifier = Modifier,
    distribution: AnalyticsGoalDistributionUi,
    isBookPosture: Boolean,
) {
    val weights = analyticsWideSplitWeights(isBookPosture)
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AnalyticsPaneTitle(stringResource(Res.string.analytics_goals_title))
        AnalyticsSplitRow(
            leadingWeight = weights.first,
            trailingWeight = weights.second,
            spacing = analyticsSplitSpacing(isBookPosture),
            leading = {
                AnalyticsGoalsProgressCard(
                    modifier = Modifier.weight(1f),
                    distribution = distribution,
                )
            },
            trailing = {
                AnalyticsGoalsDurationCard(
                    modifier = Modifier.weight(1f),
                    distribution = distribution,
                )
            },
        )
    }
}

@Composable
private fun AnalyticsPaneTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(horizontal = 4.dp),
        style = MaterialTheme.typography.titleLarge,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
    )
}
