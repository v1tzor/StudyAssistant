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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.share.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.common.CommonClassView

/**
 * @author Stanislav Aleshin on 16.08.2024.
 */
@Composable
internal fun SharedScheduleView(
    modifier: Modifier = Modifier,
    dayOfWeek: DayOfWeek,
    classes: List<ClassUi>,
) {
    Surface(
        modifier = modifier.size(170.dp, 300.dp).animateContentSize(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            CommonScheduleViewHeader(
                dayOfWeek = dayOfWeek.mapToSting(StudyAssistantRes.strings)
            )
            CommonScheduleViewContent(
                modifier = Modifier.weight(1f),
                classes = classes
            )
        }
    }
}

@Composable
internal fun SharedScheduleViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.size(170.dp, 300.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            PlaceholderBox(
                modifier = Modifier.fillMaxWidth().height(36.dp),
                shape = MaterialTheme.shapes.medium,
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
    modifier: Modifier = Modifier,
    dayOfWeek: String,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
            Text(
                text = dayOfWeek,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun CommonScheduleViewContent(
    modifier: Modifier = Modifier,
    classes: List<ClassUi>,
) {
    Column(modifier = modifier.padding(start = 2.dp, end = 2.dp, top = 4.dp, bottom = 8.dp)) {
        if (classes.isEmpty()) {
            EmptyClassesView()
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(classes, key = { it.uid }) { classModel ->
                    CommonClassView(
                        onClick = {},
                        enabled = false,
                        number = classModel.number,
                        timeRange = classModel.timeRange,
                        subject = classModel.subject,
                        office = classModel.office,
                        organization = classModel.organization,
                    )
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}

@Composable
private fun EmptyClassesView(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(42.dp).padding(horizontal = 4.dp),
        shape = MaterialTheme.shapes.medium,
        color = Color.Transparent,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
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