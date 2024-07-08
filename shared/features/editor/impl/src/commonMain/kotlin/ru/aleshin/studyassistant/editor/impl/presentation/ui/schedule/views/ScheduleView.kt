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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.views

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.forEachWith
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.theme.EditorThemeRes

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
@Composable
internal fun ScheduleView(
    modifier: Modifier = Modifier,
    dayOfWeek: DayOfWeek,
    schedule: BaseScheduleUi?,
    onCreateClass: () -> Unit,
    onEditClass: (ClassUi) -> Unit,
    onDeleteClass: (ClassUi) -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            ScheduleViewHeader(
                weekDayOfWeek = dayOfWeek,
                numberOfClasses = schedule?.classes?.size ?: 0,
            )
            ScheduleViewContent(
                classes = schedule?.classes,
                onEditClass = onEditClass,
                onDeleteClass = onDeleteClass,
            )
            ScheduleViewFooter(
                onCreateClass = onCreateClass,
            )
        }
    }
}

@Composable
internal fun ScheduleViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
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
                repeat(Placeholder.SCHEDULE_CLASSES_ITEMS) {
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
private fun ScheduleViewHeader(
    modifier: Modifier = Modifier,
    weekDayOfWeek: DayOfWeek,
    numberOfClasses: Int,
) {
    Surface(
        modifier = modifier.fillMaxWidth().height(56.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            Text(
                text = weekDayOfWeek.mapToSting(StudyAssistantRes.strings),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                modifier = Modifier.animateContentSize(),
                text = buildString {
                    append(EditorThemeRes.strings.quantityOfClassesTitle)
                    append(" ", numberOfClasses.toString())
                },
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.labelMedium,
            )
        }
    }
}

@Composable
private fun ScheduleViewContent(
    modifier: Modifier = Modifier,
    classes: List<ClassUi>?,
    onEditClass: (ClassUi) -> Unit,
    onDeleteClass: (ClassUi) -> Unit,
) {
    Column(
        modifier = modifier
            .animateContentSize()
            .padding(
                top = if (classes?.isNotEmpty() == true) 6.dp else 0.dp,
                start = 6.dp,
                end = 6.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        classes?.forEachWith {
            CommonClassView(
                onClick = { onEditClass(this) },
                number = number,
                timeRange = timeRange,
                subject = subject,
                office = office,
                organization = organization,
                trailingIcon = {
                    IconButton(
                        modifier = Modifier.size(28.dp),
                        onClick = { onDeleteClass(this) },
                    ) {
                        Icon(
                            modifier = Modifier.size(18.dp),
                            painter = painterResource(EditorThemeRes.icons.clearCircular),
                            tint = MaterialTheme.colorScheme.error,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ScheduleViewFooter(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onCreateClass: () -> Unit,
) {
    Row(modifier = modifier.padding(8.dp)) {
        OutlinedButton(
            onClick = onCreateClass,
            modifier = Modifier.height(40.dp).fillMaxWidth(),
            enabled = enabled,
            shape = MaterialTheme.shapes.large,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = EditorThemeRes.strings.addTitle,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}