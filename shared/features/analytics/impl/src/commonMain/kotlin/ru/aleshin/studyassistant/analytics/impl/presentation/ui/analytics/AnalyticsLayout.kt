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

package ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsOverviewUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsEmployeesSection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsGoalsSection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsInsightsSection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsOrganizationsSection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsRegularitySection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsSubjectsSection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsSummarySection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsTargetSection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsTasksSection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.sections.AnalyticsWorkloadSection
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_empty_body
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_empty_title
import ru.aleshin.studyassistant.core.ui.utils.isBookPosture
import ru.aleshin.studyassistant.core.ui.utils.isCompactHeight
import ru.aleshin.studyassistant.core.ui.utils.isCompactWidth
import ru.aleshin.studyassistant.core.ui.utils.isTabletopPosture
import ru.aleshin.studyassistant.core.ui.utils.useExpandedLayout
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsOverviewLayout(
    data: AnalyticsOverviewUi,
    adaptiveInfo: WindowAdaptiveInfo,
    onTargetClick: (AnalyticsTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnalyticsGrid(
        adaptiveInfo = adaptiveInfo,
        modifier = modifier,
    ) {
        item(key = SUMMARY_KEY, span = fullSpan()) {
            AnalyticsSummarySection(data.summary, data.comparison)
        }
        item(key = WORKLOAD_KEY, span = fullSpan()) {
            AnalyticsWorkloadSection(data.loadDistribution)
        }
        if (data.insights.isNotEmpty()) {
            item(key = INSIGHTS_KEY, span = fullSpan()) {
                AnalyticsInsightsSection(data.insights)
            }
        }
        item(key = TASKS_KEY, span = fullSpan()) {
            AnalyticsTasksSection(data.taskDistribution)
        }
        item(key = GOALS_KEY) {
            AnalyticsGoalsSection(data.goalDistribution)
        }
        if (data.organizations.isNotEmpty()) {
            item(key = ORGANIZATIONS_KEY) {
                AnalyticsOrganizationsSection(data.organizations, onTargetClick)
            }
        }
        if (data.subjects.isNotEmpty()) {
            item(key = SUBJECTS_KEY) {
                AnalyticsSubjectsSection(data.subjects, onTargetClick)
            }
        }
        if (data.employees.isNotEmpty()) {
            item(key = EMPLOYEES_KEY) {
                AnalyticsEmployeesSection(data.employees, onTargetClick)
            }
        }
        item(key = REGULARITY_KEY) {
            AnalyticsRegularitySection(data.regularity)
        }
    }
}

@Composable
internal fun AnalyticsDetailsLayout(
    data: AnalyticsOverviewUi,
    adaptiveInfo: WindowAdaptiveInfo,
    onTargetClick: (AnalyticsTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val details = data.targetDetails ?: return
    AnalyticsGrid(
        adaptiveInfo = adaptiveInfo,
        modifier = modifier,
    ) {
        item(key = TARGET_KEY, span = fullSpan()) {
            AnalyticsTargetSection(details, data.summary)
        }
        item(key = WORKLOAD_KEY, span = fullSpan()) {
            AnalyticsWorkloadSection(data.loadDistribution)
        }
        if (details.target !is AnalyticsTarget.Employee) {
            item(key = TASKS_KEY, span = fullSpan()) {
                AnalyticsTasksSection(data.taskDistribution)
            }
        }
        when (details.target) {
            is AnalyticsTarget.Organization -> {
                if (data.subjects.isNotEmpty()) {
                    item(key = SUBJECTS_KEY) {
                        AnalyticsSubjectsSection(data.subjects, onTargetClick)
                    }
                }
                if (data.employees.isNotEmpty()) {
                    item(key = EMPLOYEES_KEY) {
                        AnalyticsEmployeesSection(data.employees, onTargetClick)
                    }
                }
            }
            is AnalyticsTarget.Subject -> {
                if (data.organizations.isNotEmpty()) {
                    item(key = ORGANIZATIONS_KEY) {
                        AnalyticsOrganizationsSection(data.organizations, onTargetClick)
                    }
                }
                if (data.employees.isNotEmpty()) {
                    item(key = EMPLOYEES_KEY) {
                        AnalyticsEmployeesSection(data.employees, onTargetClick)
                    }
                }
            }
            is AnalyticsTarget.Employee -> {
                if (data.organizations.isNotEmpty()) {
                    item(key = ORGANIZATIONS_KEY) {
                        AnalyticsOrganizationsSection(data.organizations, onTargetClick)
                    }
                }
                if (data.subjects.isNotEmpty()) {
                    item(key = SUBJECTS_KEY) {
                        AnalyticsSubjectsSection(data.subjects, onTargetClick)
                    }
                }
            }
        }
        if (details.target !is AnalyticsTarget.Employee) {
            item(key = GOALS_KEY) {
                AnalyticsGoalsSection(data.goalDistribution)
            }
        }
        item(key = REGULARITY_KEY) {
            AnalyticsRegularitySection(data.regularity)
        }
        if (data.insights.isNotEmpty()) {
            item(key = INSIGHTS_KEY) {
                AnalyticsInsightsSection(data.insights)
            }
        }
    }
}

@Composable
private fun AnalyticsGrid(
    adaptiveInfo: WindowAdaptiveInfo,
    modifier: Modifier = Modifier,
    content: LazyGridScope.() -> Unit,
) {
    val useTwoColumns = !adaptiveInfo.isCompactHeight &&
        !adaptiveInfo.isTabletopPosture &&
        (adaptiveInfo.useExpandedLayout || adaptiveInfo.isBookPosture || !adaptiveInfo.isCompactWidth)
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(if (useTwoColumns) 2 else 1),
            modifier = Modifier.fillMaxSize().widthIn(max = MAX_CONTENT_WIDTH),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(
                if (adaptiveInfo.isBookPosture) 32.dp else 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            content = content,
        )
    }
}

@Composable
internal fun AnalyticsLoadingLayout(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier.fillMaxSize().widthIn(max = COMPACT_CONTENT_WIDTH),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            itemsIndexed(
                items = PLACEHOLDER_HEIGHTS,
                key = { index, _ -> "analytics_placeholder_$index" },
            ) { _, height ->
                PlaceholderBox(
                    modifier = Modifier.fillMaxWidth().height(height),
                    shape = MaterialTheme.shapes.large,
                )
            }
        }
    }
}

@Composable
internal fun AnalyticsEmptyLayout(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            androidx.compose.foundation.layout.Column(
                modifier = Modifier.fillMaxWidth().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    stringResource(Res.string.analytics_empty_title),
                    style = MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center,
                )
                Text(
                    stringResource(Res.string.analytics_empty_body),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

private fun fullSpan(): LazyGridItemSpanScope.() -> GridItemSpan = { GridItemSpan(maxLineSpan) }

private val MAX_CONTENT_WIDTH = 1120.dp
private val COMPACT_CONTENT_WIDTH = 720.dp
private val PLACEHOLDER_HEIGHTS = listOf(220.dp, 280.dp, 340.dp, 260.dp)
private const val TARGET_KEY = "target"
private const val SUMMARY_KEY = "summary"
private const val INSIGHTS_KEY = "insights"
private const val WORKLOAD_KEY = "workload"
private const val TASKS_KEY = "tasks"
private const val GOALS_KEY = "goals"
private const val ORGANIZATIONS_KEY = "organizations"
private const val SUBJECTS_KEY = "subjects"
private const val EMPLOYEES_KEY = "employees"
private const val REGULARITY_KEY = "regularity"
