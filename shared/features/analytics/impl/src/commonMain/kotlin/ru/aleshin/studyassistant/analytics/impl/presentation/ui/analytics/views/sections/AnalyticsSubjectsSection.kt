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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.WarningAmber
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.presentation.models.SubjectAnalyticsUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsDuration
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_classes
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_homeworks
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_not_specified
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_on_time
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_overdue
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_show_all
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_show_less
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_subjects_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_tests

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsSubjectsSection(
    subjects: List<SubjectAnalyticsUi>,
    onTargetClick: (AnalyticsTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (subjects.isEmpty()) return
    var showAll by rememberSaveable { mutableStateOf(false) }
    val displayed = remember(subjects, showAll) {
        if (showAll) subjects else subjects.take(VISIBLE_ITEMS)
    }
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.analytics_subjects_title),
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.titleLarge,
        )
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            displayed.forEach { analytics ->
                SubjectAnalyticsItem(
                    analytics = analytics,
                    onClick = {
                        analytics.subject?.let { subject ->
                            onTargetClick(AnalyticsTarget.Subject(subject.uid))
                        }
                    },
                )
            }
        }
        if (subjects.size > VISIBLE_ITEMS) {
            TextButton(
                onClick = { showAll = !showAll },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    text = if (showAll) {
                        stringResource(Res.string.analytics_show_less)
                    } else {
                        stringResource(Res.string.analytics_show_all)
                    },
                )
            }
        }
    }
}

@Composable
private fun SubjectAnalyticsItem(
    modifier: Modifier = Modifier,
    analytics: SubjectAnalyticsUi,
    onClick: () -> Unit,
) {
    val subjectColor = analytics.subject?.let { Color(it.color) } ?: MaterialTheme.colorScheme.primary

    Surface(
        onClick = onClick,
        enabled = analytics.subject != null,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(modifier = Modifier.fillMaxWidth().heightIn(min = 112.dp)) {
            Box(modifier = Modifier.width(4.dp).heightIn(min = 112.dp).background(subjectColor))
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(
                    modifier = Modifier.padding(start = 12.dp, top = 12.dp, end = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    SubjectHeader(
                        analytics = analytics,
                        subjectColor = subjectColor,
                        isClickEnabled = analytics.subject != null,
                    )
                    WorkloadIndicator(
                        progress = analytics.workloadProgress,
                        color = subjectColor,
                    )
                    SubjectMetrics(
                        analytics = analytics,
                    )
                }
                if (analytics.completedOnTime > 0 || analytics.overdue > 0) {
                    Row(
                        modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (analytics.completedOnTime > 0) {
                            StatusMetric(
                                modifier = Modifier.weight(1f, fill = false),
                                icon = Icons.Default.CheckCircle,
                                value = analytics.completedOnTime,
                                label = stringResource(Res.string.analytics_on_time),
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }
                        if (analytics.overdue > 0) {
                            StatusMetric(
                                modifier = Modifier.weight(1f, fill = false),
                                icon = Icons.Default.WarningAmber,
                                value = analytics.overdue,
                                label = stringResource(Res.string.analytics_overdue),
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(1.dp))
                }
            }
        }
    }
}

@Composable
private fun SubjectHeader(
    analytics: SubjectAnalyticsUi,
    subjectColor: Color,
    isClickEnabled: Boolean,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            shape = CircleShape,
            color = subjectColor.copy(alpha = 0.14f),
        ) {
            Box(
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Default.School,
                    contentDescription = null,
                    tint = subjectColor,
                    modifier = Modifier.size(21.dp),
                )
            }
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = analytics.subject?.name ?: stringResource(Res.string.analytics_not_specified),
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
                overflow = TextOverflow.Ellipsis,
            )
        }

        if (isClickEnabled) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
private fun WorkloadIndicator(
    progress: Float,
    color: Color,
) {
    val safeProgress = progress.takeIf { it.isFinite() }?.coerceIn(0f, 1f) ?: 0f
    LinearProgressIndicator(
        progress = { safeProgress },
        modifier = Modifier.fillMaxWidth().height(6.dp),
        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        color = color,
    )
}

@Composable
private fun SubjectMetrics(
    analytics: SubjectAnalyticsUi,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubjectMetric(
            icon = Icons.Default.School,
            value = analytics.classesCount,
            label = stringResource(Res.string.analytics_classes),
            modifier = Modifier.weight(1f),
        )
        SubjectMetric(
            icon = Icons.AutoMirrored.Filled.Assignment,
            value = analytics.homeworkCount,
            label = stringResource(Res.string.analytics_homeworks),
            modifier = Modifier.weight(1f),
        )
        SubjectMetric(
            icon = Icons.Default.Quiz,
            value = analytics.testsCount,
            label = stringResource(Res.string.analytics_tests),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun SubjectMetric(
    icon: ImageVector,
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun StatusMetric(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: Int,
    label: String,
    color: Color,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = color.copy(alpha = 0.10f),
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = 10.dp,
                vertical = 6.dp,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "$value $label",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

private const val VISIBLE_ITEMS = 5
