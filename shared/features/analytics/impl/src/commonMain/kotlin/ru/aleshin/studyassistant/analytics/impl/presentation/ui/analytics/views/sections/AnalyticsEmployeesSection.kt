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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsTarget
import ru.aleshin.studyassistant.analytics.impl.presentation.models.EmployeeAnalyticsUi
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.AnalyticsSectionCard
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.analyticsWeekdayTitle
import ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views.formatAnalyticsDuration
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_classes
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_employees_title
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_frequent_day
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_not_specified
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
    val displayed = if (showAll) employees else employees.take(VISIBLE_ITEMS)
    AnalyticsSectionCard(
        title = stringResource(Res.string.analytics_employees_title),
        modifier = modifier,
    ) {
        displayed.forEachIndexed { index, analytics ->
            EmployeeRow(
                analytics = analytics,
                onClick = analytics.employee?.let { employee ->
                    { onTargetClick(AnalyticsTarget.Employee(employee.uid)) }
                },
            )
            if (index != displayed.lastIndex) HorizontalDivider()
        }
        if (employees.size > VISIBLE_ITEMS) {
            TextButton(
                onClick = { showAll = !showAll },
                modifier = Modifier.align(Alignment.End),
            ) {
                Text(
                    stringResource(
                        if (showAll) Res.string.analytics_show_less else Res.string.analytics_show_all,
                    ),
                )
            }
        }
    }
}

@Composable
private fun EmployeeRow(
    analytics: EmployeeAnalyticsUi,
    onClick: (() -> Unit)?,
) {
    val employee = analytics.employee
    Row(
        modifier = Modifier.fillMaxWidth().then(
            if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier,
        ),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (employee?.avatar != null) {
            AvatarView(
                firstName = employee.firstName,
                secondName = employee.secondName,
                imageUrl = employee.avatar,
                modifier = Modifier.size(44.dp),
            )
        } else {
            Surface(
                modifier = Modifier.size(44.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            ) {
                androidx.compose.foundation.layout.Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = employee?.officialName()
                        ?: stringResource(Res.string.analytics_not_specified),
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = formatAnalyticsDuration(analytics.plannedDuration),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
            Text(
                text = buildString {
                    append(stringResource(Res.string.analytics_classes), ": ", analytics.classesCount)
                    append(" · ")
                    append(stringResource(Res.string.analytics_subject_count), ": ", analytics.subjectsCount)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            analytics.mostFrequentDay?.let { day ->
                Text(
                    text = buildString {
                        append(stringResource(Res.string.analytics_frequent_day), ": ")
                        append(analyticsWeekdayTitle(day))
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private const val VISIBLE_ITEMS = 5
