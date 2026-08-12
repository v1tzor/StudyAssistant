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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.presentation.models.OrganizationAnalyticsUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsDuration
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsRate
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_classes
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_homeworks
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_on_time
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_organizations_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_show_all
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_show_less
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsOrganizationsSection(
    organizations: List<OrganizationAnalyticsUi>,
    onTargetClick: (AnalyticsTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (organizations.isEmpty()) return

    var showAll by rememberSaveable { mutableStateOf(false) }
    val displayed = remember(organizations, showAll) {
        if (showAll) organizations else organizations.take(VISIBLE_ITEMS)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.analytics_organizations_title),
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            displayed.forEach { analytics ->
                OrganizationAnalyticsItem(
                    analytics = analytics,
                    onClick = { onTargetClick(AnalyticsTarget.Organization(analytics.organization.uid)) },
                )
            }
        }

        if (organizations.size > VISIBLE_ITEMS) {
            TextButton(
                onClick = { showAll = !showAll },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = stringResource(
                        if (showAll) {
                            Res.string.analytics_show_less
                        } else {
                            Res.string.analytics_show_all
                        },
                    ),
                )
            }
        }
    }
}

@Composable
private fun OrganizationAnalyticsItem(
    analytics: OrganizationAnalyticsUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OrganizationHeader(
                analytics = analytics,
            )
            OrganizationWorkload(
                workloadShare = analytics.workloadShare,
            )
            OrganizationMetrics(
                analytics = analytics,
            )
        }
    }
}

@Composable
private fun OrganizationHeader(
    analytics: OrganizationAnalyticsUi,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AvatarView(
            firstName = analytics.organization.shortName,
            secondName = null,
            imageUrl = analytics.organization.avatar,
            modifier = Modifier.size(44.dp),
            style = MaterialTheme.typography.labelLarge,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = analytics.organization.shortName,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = formatAnalyticsDuration(analytics.plannedDuration),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun OrganizationWorkload(
    workloadShare: Float,
) {
    val progress = workloadShare.coerceIn(0f, 1f)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.primary,
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

@Composable
private fun OrganizationMetrics(
    analytics: OrganizationAnalyticsUi,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        OrganizationMetric(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.School,
            value = analytics.classesCount.toString(),
            label = stringResource(Res.string.analytics_classes),
        )
        OrganizationMetric(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Assignment,
            value = analytics.homeworkCount.toString(),
            label = stringResource(Res.string.analytics_homeworks),
        )
        OrganizationMetric(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.CheckCircle,
            value = formatAnalyticsRate(analytics.onTimeRate),
            label = stringResource(Res.string.analytics_on_time),
            highlighted = analytics.onTimeRate != null,
        )
    }
}

@Composable
private fun OrganizationMetric(
    icon: ImageVector,
    value: String,
    label: String,
    modifier: Modifier = Modifier,
    highlighted: Boolean = false,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = if (highlighted) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
        )

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge,
                color = if (highlighted) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private const val VISIBLE_ITEMS = 5
