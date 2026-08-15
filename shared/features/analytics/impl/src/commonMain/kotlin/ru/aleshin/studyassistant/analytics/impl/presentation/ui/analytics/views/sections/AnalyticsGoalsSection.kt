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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsGoalDistributionUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsIconMetric
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsSectionCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.SectionLabel
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsDuration
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsRate
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_active_timer
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_actual_time
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_completed
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_desired_time
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_goal_homeworks
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_goal_todos
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_goals_duration
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
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.analytics_goals_title),
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        AnalyticsSectionCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier.size(104.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        progress = {
                            distribution.completionRate
                                ?.takeIf { it.isFinite() }
                                ?.coerceIn(0f, 1f)
                                ?: 0f
                        },
                        modifier = Modifier.size(104.dp),
                        strokeWidth = 10.dp,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = formatAnalyticsRate(distribution.completionRate),
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            text = stringResource(Res.string.analytics_completed),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    GoalCount(
                        label = stringResource(Res.string.analytics_planned),
                        value = distribution.planned,
                    )
                    GoalCount(
                        label = stringResource(Res.string.analytics_completed),
                        value = distribution.completed,
                    )
                    GoalCount(
                        label = stringResource(Res.string.analytics_overdue),
                        value = distribution.overdue,
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                AnalyticsIconMetric(
                    icon = Icons.Default.AssignmentTurnedIn,
                    label = stringResource(Res.string.analytics_goal_homeworks),
                    value = distribution.homeworkGoals.toString(),
                    modifier = Modifier.weight(1f),
                )
                AnalyticsIconMetric(
                    icon = Icons.Default.Flag,
                    label = stringResource(Res.string.analytics_goal_todos),
                    value = distribution.todoGoals.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
        AnalyticsSectionCard {
            SectionLabel(
                title = stringResource(Res.string.analytics_goals_duration),
                icon = Icons.Default.Timelapse,
            )
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    AnalyticsIconMetric(
                        icon = Icons.Default.Schedule,
                        label = stringResource(Res.string.analytics_desired_time),
                        value = formatAnalyticsDuration(distribution.desiredDuration),
                        modifier = Modifier.weight(1f),
                    )
                    AnalyticsIconMetric(
                        icon = Icons.Default.Timer,
                        label = stringResource(Res.string.analytics_actual_time),
                        value = formatAnalyticsDuration(distribution.actualDuration),
                        supportingText = stringResource(Res.string.analytics_active_timer).takeIf { distribution.hasActiveTimer },
                        modifier = Modifier.weight(1f),
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    val progress = remember(distribution) {
                        (distribution.actualDuration.toFloat() / distribution.desiredDuration.coerceAtLeast(1L))
                            .coerceIn(0f, 1f)
                    }
                    LinearProgressIndicator(
                        modifier = Modifier.weight(1f),
                        progress = { progress },
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    )
                    Text(
                        text = formatAnalyticsRate(progress),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun GoalCount(label: String, value: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.titleMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
