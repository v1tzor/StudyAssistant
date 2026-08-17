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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import ru.aleshin.studyassistant.analytics.impl.presentation.models.EmployeeAnalyticsUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.analyticsWeekdayTitle
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsDuration
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_classes
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_employees_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_frequent_day
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_not_specified
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_organizations_count
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_show_all
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_show_less
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_subject_count
import ru.aleshin.studyassistant.core.ui.views.menu.AvatarView

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsEmployeesSection(
    employees: List<EmployeeAnalyticsUi>,
    onTargetClick: (AnalyticsTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (employees.isEmpty()) return

    var showAll by rememberSaveable { mutableStateOf(false) }
    val displayed = remember(employees, showAll) {
        if (showAll) employees else employees.take(VISIBLE_ITEMS)
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(Res.string.analytics_employees_title),
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MaterialTheme.typography.titleLarge,
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            displayed.forEach { analytics ->
                EmployeeAnalyticsItem(
                    analytics = analytics,
                    onClick = {
                        analytics.employee?.let { employee ->
                            onTargetClick(AnalyticsTarget.Employee(employee.uid))
                        }
                    },
                )
            }
        }

        if (employees.size > VISIBLE_ITEMS) {
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
private fun EmployeeAnalyticsItem(
    analytics: EmployeeAnalyticsUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val employee = analytics.employee

    Surface(
        onClick = onClick,
        enabled = employee != null,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            EmployeeHeader(
                analytics = analytics,
            )
            analytics.mostFrequentDay?.let { day ->
                EmployeeFrequentDay(
                    day = analyticsWeekdayTitle(day),
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            EmployeeMetrics(
                analytics = analytics,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        }
    }
}

@Composable
private fun EmployeeHeader(
    analytics: EmployeeAnalyticsUi,
) {
    val employee = analytics.employee

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (employee != null) {
            AvatarView(
                firstName = employee.firstName,
                secondName = employee.secondName,
                imageUrl = employee.avatar,
                modifier = Modifier.size(48.dp),
                style = MaterialTheme.typography.labelLarge,
            )
        } else {
            Surface(
                modifier = Modifier.size(48.dp),
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

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = employee?.officialName() ?: stringResource(Res.string.analytics_not_specified),
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

        if (employee != null) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EmployeeFrequentDay(
    day: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
            )

            Text(
                text = buildString {
                    append(stringResource(Res.string.analytics_frequent_day))
                    append(" · ")
                    append(day)
                },
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun EmployeeMetrics(
    analytics: EmployeeAnalyticsUi,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        EmployeeMetric(
            icon = Icons.Default.School,
            value = analytics.classesCount,
            label = stringResource(Res.string.analytics_classes),
            modifier = Modifier.weight(1f),
        )
        EmployeeMetric(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            value = analytics.subjectsCount,
            label = stringResource(Res.string.analytics_subject_count),
            modifier = Modifier.weight(1f),
        )
        EmployeeMetric(
            icon = Icons.Default.Business,
            value = analytics.organizationsCount,
            label = stringResource(Res.string.analytics_organizations_count),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun EmployeeMetric(
    icon: ImageVector,
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(18.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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

private const val VISIBLE_ITEMS = 5