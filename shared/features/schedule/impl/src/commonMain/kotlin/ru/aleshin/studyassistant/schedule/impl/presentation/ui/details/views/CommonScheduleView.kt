/*
 * Copyright 2024 Stanislav Aleshin
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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import extensions.forEachWith
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import mappers.mapToSting
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import theme.StudyAssistantRes
import theme.tokens.monthNames
import views.PlaceholderBox
import views.SmallInfoBadge

/**
 * @author Stanislav Aleshin on 09.06.2024.
 */
@Composable
internal fun CommonScheduleView(
    modifier: Modifier = Modifier,
    date: LocalDate,
    isCurrentDay: Boolean,
    activeClass: ActiveClassUi?,
    classes: List<ClassDetailsUi>,
    userScrollEnabled: Boolean = false,
    onOpenSchedule: () -> Unit,
    onClassClick: (ClassDetailsUi) -> Unit,
) {
    val scrollState = rememberScrollState()
    val coreStrings = StudyAssistantRes.strings
    val dateFormat = LocalDate.Format {
        dayOfMonth()
        char(' ')
        monthName(coreStrings.monthNames())
    }
    Surface(
        modifier = modifier.fillMaxWidth().animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            CommonScheduleViewHeader(
                onClick = onOpenSchedule,
                dayOfWeek = date.dayOfWeek.mapToSting(coreStrings),
                date = date.format(dateFormat),
                isHighlighted = isCurrentDay,
            )
            CommonScheduleViewContent(
                modifier = if (userScrollEnabled) Modifier.weight(1f) else Modifier.wrapContentSize(),
                scrollState = if (userScrollEnabled) scrollState else null,
                activeClass = activeClass,
                classes = classes,
                onClassClick = onClassClick,
            )
        }
    }
}

@Composable
internal fun CommonScheduleViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            PlaceholderBox(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                repeat(3) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PlaceholderBox(
                            modifier = Modifier.height(52.dp).width(4.dp),
                            shape = MaterialTheme.shapes.large,
                            color = MaterialTheme.colorScheme.surfaceContainerHighest,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            PlaceholderBox(
                                modifier = Modifier.size(90.dp, 16.dp),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                            )
                            PlaceholderBox(
                                modifier = Modifier.height(32.dp).fillMaxWidth(),
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.surfaceContainer,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CommonScheduleViewHeader(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    dayOfWeek: String,
    date: String,
    isHighlighted: Boolean,
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        color = if (isHighlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)) {
            Text(
                text = dayOfWeek,
                color = if (isHighlighted) {
                    MaterialTheme.colorScheme.onPrimaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
            )
            Text(
                text = date,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}

@Composable
private fun CommonScheduleViewContent(
    modifier: Modifier = Modifier,
    scrollState: ScrollState?,
    activeClass: ActiveClassUi?,
    classes: List<ClassDetailsUi>,
    onClassClick: (ClassDetailsUi) -> Unit,
) {
    Column(
        modifier = modifier.padding(start = 6.dp, end = 6.dp, top = 8.dp, bottom = 12.dp).then(
            if (scrollState != null) Modifier.verticalScroll(scrollState) else Modifier
        ),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (classes.isEmpty()) {
            EmptyClassesView()
        } else {
            classes.forEachWith {
                CommonClassView(
                    onClick = { onClassClick(this) },
                    highlightContent = activeClass?.isStarted?.takeIf { activeClass.uid == this.uid } ?: false,
                    number = number,
                    timeRange = timeRange,
                    subject = subject,
                    office = office,
                    organization = organization,
                    headerBadge = if (homework?.test != null) {
                        {
                            SmallInfoBadge(
                                containerColor = StudyAssistantRes.colors.accents.redContainer,
                                contentColor = StudyAssistantRes.colors.accents.red,
                                content = {
                                    Text(
                                        text = ScheduleThemeRes.strings.testLabel,
                                        overflow = TextOverflow.Ellipsis,
                                        maxLines = 1,
                                    )
                                },
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun EmptyClassesView(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(32.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = ScheduleThemeRes.strings.emptyClassesTitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}