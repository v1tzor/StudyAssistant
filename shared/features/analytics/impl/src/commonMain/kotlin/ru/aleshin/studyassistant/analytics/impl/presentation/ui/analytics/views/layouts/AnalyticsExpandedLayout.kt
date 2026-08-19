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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsOverviewUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsEmployeesSection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsOrganizationsSection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsRegularitySection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsSubjectsSection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsSummarySection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsTargetSection
import ru.aleshin.studyassistant.core.ui.ads.AdPlacement
import ru.aleshin.studyassistant.core.ui.ads.YandexInlineBanner
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
@Composable
internal fun AnalyticsExpandedLayout(
    modifier: Modifier = Modifier,
    data: AnalyticsOverviewUi,
    isDetails: Boolean,
    isBookPosture: Boolean,
    onTargetClick: (AnalyticsTarget) -> Unit,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = ANALYTICS_CONTENT_MAX_WIDTH)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = ANALYTICS_EXPANDED_HORIZONTAL_PADDING,
                vertical = ANALYTICS_EXPANDED_VERTICAL_PADDING,
            ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(ANALYTICS_EXPANDED_SECTION_SPACING),
        ) {
            if (isDetails) {
                analyticsExpandedDetailsItems(
                    data = data,
                    isBookPosture = isBookPosture,
                    onTargetClick = onTargetClick,
                )
            } else {
                analyticsExpandedOverviewItems(
                    data = data,
                    isBookPosture = isBookPosture,
                    onTargetClick = onTargetClick,
                )
            }
        }
    }
}

@Composable
internal fun AnalyticsExpandedLoadingLayout(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .widthIn(max = ANALYTICS_CONTENT_MAX_WIDTH)
                .fillMaxWidth(),
            contentPadding = PaddingValues(
                horizontal = ANALYTICS_EXPANDED_HORIZONTAL_PADDING,
                vertical = ANALYTICS_EXPANDED_VERTICAL_PADDING,
            ),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(ANALYTICS_EXPANDED_SECTION_SPACING),
        ) {
            itemsIndexed(
                items = EXPANDED_PLACEHOLDER_HEIGHTS,
                key = { index, _ -> "analytics_expanded_placeholder_$index" },
            ) { _, height ->
                PlaceholderBox(
                    modifier = Modifier
                        .widthIn(max = ANALYTICS_SINGLE_CONTENT_MAX_WIDTH)
                        .fillMaxWidth()
                        .height(height),
                    shape = MaterialTheme.shapes.large,
                )
            }
        }
    }
}

private fun LazyListScope.analyticsExpandedOverviewItems(
    data: AnalyticsOverviewUi,
    isBookPosture: Boolean,
    onTargetClick: (AnalyticsTarget) -> Unit,
) {
    item(key = EXPANDED_SUMMARY_KEY) {
        AnalyticsSummarySection(
            modifier = Modifier.analyticsSinglePaneWidth(),
            summary = data.summary,
            comparison = data.comparison,
        )
    }
    item(key = EXPANDED_WORKLOAD_KEY) {
        AnalyticsExpandedWorkloadInsightsRow(
            modifier = Modifier.fillMaxWidth(),
            workload = data.loadDistribution,
            insights = data.insights,
            isBookPosture = isBookPosture,
        )
    }
    item(key = EXPANDED_TASKS_KEY) {
        AnalyticsExpandedTasksSection(
            modifier = Modifier.fillMaxWidth(),
            distribution = data.taskDistribution,
            isBookPosture = isBookPosture,
        )
    }
    item(key = EXPANDED_GOALS_KEY) {
        AnalyticsExpandedGoalsSection(
            modifier = Modifier.fillMaxWidth(),
            distribution = data.goalDistribution,
            isBookPosture = isBookPosture,
        )
    }
    analyticsExpandedTrailingItems(
        data = data,
        onTargetClick = onTargetClick,
    )
}

private fun LazyListScope.analyticsExpandedDetailsItems(
    data: AnalyticsOverviewUi,
    isBookPosture: Boolean,
    onTargetClick: (AnalyticsTarget) -> Unit,
) {
    val details = data.targetDetails ?: return
    item(key = EXPANDED_TARGET_KEY) {
        AnalyticsTargetSection(
            modifier = Modifier.analyticsSinglePaneWidth(),
            details = details,
            summary = data.summary,
        )
    }
    item(key = EXPANDED_WORKLOAD_KEY) {
        AnalyticsExpandedWorkloadInsightsRow(
            modifier = Modifier.fillMaxWidth(),
            workload = data.loadDistribution,
            insights = data.insights,
            isBookPosture = isBookPosture,
        )
    }
    if (details.target !is AnalyticsTarget.Employee) {
        item(key = EXPANDED_TASKS_KEY) {
            AnalyticsExpandedTasksSection(
                modifier = Modifier.fillMaxWidth(),
                distribution = data.taskDistribution,
                isBookPosture = isBookPosture,
            )
        }
        item(key = EXPANDED_GOALS_KEY) {
            AnalyticsExpandedGoalsSection(
                modifier = Modifier.fillMaxWidth(),
                distribution = data.goalDistribution,
                isBookPosture = isBookPosture,
            )
        }
    }
    analyticsExpandedTrailingItems(
        data = data,
        onTargetClick = onTargetClick,
        detailsTarget = details.target,
    )
}

private fun LazyListScope.analyticsExpandedTrailingItems(
    data: AnalyticsOverviewUi,
    onTargetClick: (AnalyticsTarget) -> Unit,
    detailsTarget: AnalyticsTarget? = null,
) {
    val showOrganizations = data.organizations.isNotEmpty() &&
        detailsTarget !is AnalyticsTarget.Organization
    val showSubjects = data.subjects.isNotEmpty() &&
        detailsTarget !is AnalyticsTarget.Subject
    val showEmployees = data.employees.isNotEmpty() &&
        detailsTarget !is AnalyticsTarget.Employee

    if (showOrganizations) {
        item(key = EXPANDED_ORGANIZATIONS_KEY) {
            AnalyticsOrganizationsSection(
                modifier = Modifier.analyticsSinglePaneWidth(),
                organizations = data.organizations,
                onTargetClick = onTargetClick,
            )
        }
    }
    if (showSubjects) {
        item(key = EXPANDED_SUBJECTS_KEY) {
            AnalyticsSubjectsSection(
                modifier = Modifier.analyticsSinglePaneWidth(),
                subjects = data.subjects,
                onTargetClick = onTargetClick,
            )
        }
    }
    if (showEmployees) {
        item(key = EXPANDED_EMPLOYEES_KEY) {
            AnalyticsEmployeesSection(
                modifier = Modifier.analyticsSinglePaneWidth(),
                employees = data.employees,
                onTargetClick = onTargetClick,
            )
        }
    }
    item(key = EXPANDED_REGULARITY_KEY) {
        AnalyticsRegularitySection(
            modifier = Modifier.analyticsSinglePaneWidth(),
            regularity = data.regularity,
        )
    }
    item(key = EXPANDED_BANNER_KEY) {
        YandexInlineBanner(
            modifier = Modifier.fillMaxWidth(),
            placement = AdPlacement.ANALYTICS,
        )
    }
}

private fun Modifier.analyticsSinglePaneWidth(): Modifier {
    return widthIn(max = ANALYTICS_SINGLE_CONTENT_MAX_WIDTH).fillMaxWidth()
}

private val EXPANDED_PLACEHOLDER_HEIGHTS = listOf(220.dp, 280.dp, 340.dp, 260.dp)
private const val EXPANDED_BANNER_KEY = "banner"
private const val EXPANDED_TARGET_KEY = "target"
private const val EXPANDED_SUMMARY_KEY = "summary"
private const val EXPANDED_WORKLOAD_KEY = "workload"
private const val EXPANDED_TASKS_KEY = "tasks"
private const val EXPANDED_GOALS_KEY = "goals"
private const val EXPANDED_ORGANIZATIONS_KEY = "organizations"
private const val EXPANDED_SUBJECTS_KEY = "subjects"
private const val EXPANDED_EMPLOYEES_KEY = "employees"
private const val EXPANDED_REGULARITY_KEY = "regularity"
