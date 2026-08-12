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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsSummaryUi
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsTargetDetailsUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsIconMetric
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsSection
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsDuration
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsRate
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_classes
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_commitments
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_detail_employee
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_detail_organization
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_detail_subject
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_not_specified
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_on_time_rate
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_scheduled_hours
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView

/**
 * @author Stanislav Aleshin on 11.08.2026.
 */
@Composable
internal fun AnalyticsTargetSection(
    details: AnalyticsTargetDetailsUi,
    summary: AnalyticsSummaryUi,
    modifier: Modifier = Modifier,
) {
    AnalyticsSection(
        title = when (details.target) {
            is AnalyticsTarget.Organization -> stringResource(Res.string.analytics_detail_organization)
            is AnalyticsTarget.Subject -> stringResource(Res.string.analytics_detail_subject)
            is AnalyticsTarget.Employee -> stringResource(Res.string.analytics_detail_employee)
        },
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TargetAvatar(details)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = targetName(details),
                    style = MaterialTheme.typography.titleLarge,
                )
                if (details.target !is AnalyticsTarget.Employee) {
                    Text(
                        text = buildString {
                            append(stringResource(Res.string.analytics_on_time_rate))
                            append(" · ")
                            append(formatAnalyticsRate(summary.onTimeRate))
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AnalyticsIconMetric(
                icon = Icons.Default.Schedule,
                label = stringResource(Res.string.analytics_scheduled_hours),
                value = formatAnalyticsDuration(summary.plannedDuration),
                modifier = Modifier.weight(1f),
            )
            AnalyticsIconMetric(
                icon = Icons.Default.Event,
                label = stringResource(Res.string.analytics_classes),
                value = summary.classesCount.toString(),
                modifier = Modifier.weight(1f),
            )
        }
        if (details.target !is AnalyticsTarget.Employee) {
            AnalyticsIconMetric(
                icon = Icons.AutoMirrored.Filled.Assignment,
                label = stringResource(Res.string.analytics_commitments),
                value = (summary.homeworkCount + summary.todoCount).toString(),
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun TargetAvatar(details: AnalyticsTargetDetailsUi) {
    when (details.target) {
        is AnalyticsTarget.Organization -> AvatarView(
            firstName = details.organization?.shortName.orEmpty(),
            secondName = null,
            imageUrl = details.organization?.avatar,
            modifier = Modifier.size(52.dp),
            style = MaterialTheme.typography.titleMedium,
        )
        is AnalyticsTarget.Subject -> Surface(
            modifier = Modifier.size(52.dp),
            shape = CircleShape,
            color = details.subject?.let { Color(it.color) } ?: MaterialTheme.colorScheme.outline,
            content = {},
        )
        is AnalyticsTarget.Employee -> {
            val employee = details.employee
            if (employee?.avatar != null) {
                AvatarView(
                    firstName = employee.firstName,
                    secondName = employee.secondName,
                    imageUrl = employee.avatar,
                    modifier = Modifier.size(52.dp),
                )
            } else {
                Surface(
                    modifier = Modifier.size(52.dp),
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun targetName(details: AnalyticsTargetDetailsUi): String = when (details.target) {
    is AnalyticsTarget.Organization -> details.organization?.shortName
    is AnalyticsTarget.Subject -> details.subject?.name
    is AnalyticsTarget.Employee -> details.employee?.fullName()
}.orEmpty().ifEmpty { stringResource(Res.string.analytics_not_specified) }
