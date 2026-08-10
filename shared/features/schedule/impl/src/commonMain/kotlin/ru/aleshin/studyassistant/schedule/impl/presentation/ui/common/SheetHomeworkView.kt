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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.common

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.presentation.models.tasks.HomeworkTaskComponentUi
import ru.aleshin.studyassistant.core.presentation.models.tasks.HomeworkTasksDetailsUi
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.schedule.impl.resources.Res
import ru.aleshin.studyassistant.schedule.impl.resources.none_homework_title
import ru.aleshin.studyassistant.schedule.impl.resources.test_label
import ru.aleshin.studyassistant.core.ui.resources.Res as CoreRes
import ru.aleshin.studyassistant.core.ui.resources.ic_alert_circle as core_ic_alert_circle
import ru.aleshin.studyassistant.core.ui.resources.ic_book_study as core_ic_book_study
import ru.aleshin.studyassistant.core.ui.resources.ic_presentation as core_ic_presentation
import ru.aleshin.studyassistant.core.ui.resources.ic_tasks_circular as core_ic_tasks_circular
import ru.aleshin.studyassistant.core.ui.resources.practical_tasks_title as core_practical_tasks_title
import ru.aleshin.studyassistant.core.ui.resources.presentations_tasks_title as core_presentations_tasks_title
import ru.aleshin.studyassistant.core.ui.resources.theoretical_tasks_title as core_theoretical_tasks_title

/**
 * @author Stanislav Aleshin on 21.06.2024.
 */
@Composable
internal fun SheetHomeworkView(
    modifier: Modifier = Modifier,
    theoreticalTasks: HomeworkTasksDetailsUi,
    practicalTasks: HomeworkTasksDetailsUi,
    presentationTasks: HomeworkTasksDetailsUi,
    priority: TaskPriority
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            if (theoreticalTasks.components.isNotEmpty()) {
                HomeworkTaskView(
                    icon = painterResource(CoreRes.drawable.core_ic_book_study),
                    title = stringResource(CoreRes.string.core_theoretical_tasks_title),
                    priority = priority,
                    tasks = theoreticalTasks,
                )
            }
            if (theoreticalTasks.components.isNotEmpty() && practicalTasks.components.isNotEmpty()) {
                HorizontalDivider()
            }
            if (practicalTasks.components.isNotEmpty()) {
                HomeworkTaskView(
                    icon = painterResource(CoreRes.drawable.core_ic_tasks_circular),
                    title = stringResource(CoreRes.string.core_practical_tasks_title),
                    priority = priority,
                    tasks = practicalTasks,
                )
            }
            if (presentationTasks.components.isNotEmpty() &&
                (theoreticalTasks.components.isNotEmpty() || practicalTasks.components.isNotEmpty())
            ) {
                HorizontalDivider()
            }
            if (presentationTasks.components.isNotEmpty()) {
                HomeworkTaskView(
                    icon = painterResource(CoreRes.drawable.core_ic_presentation),
                    title = stringResource(CoreRes.string.core_presentations_tasks_title),
                    priority = priority,
                    tasks = presentationTasks,
                )
            }
        }
    }
}

@Composable
internal fun NoneHomeworkView(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Box(modifier = Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(CoreRes.drawable.core_ic_book_study),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = stringResource(Res.string.none_homework_title),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
internal fun TestHomeworkView(
    modifier: Modifier = Modifier,
    testTopic: String,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.errorContainer,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = painterResource(CoreRes.drawable.core_ic_alert_circle),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = stringResource(Res.string.test_label),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                modifier = Modifier.weight(1f),
                text = testTopic,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.End,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.labelLarge,
            )
        }
    }
}

@Composable
private fun HomeworkTaskView(
    modifier: Modifier = Modifier,
    icon: Painter,
    title: String,
    priority: TaskPriority,
    tasks: HomeworkTasksDetailsUi,
) {
    Column(
        modifier = modifier.padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
            )
            if (priority == TaskPriority.MEDIUM || priority == TaskPriority.HIGH) {
                Text(
                    text = priority.mapToString(),
                    color = if (priority == TaskPriority.MEDIUM) {
                        StudyAssistantRes.colors.accents.orange
                    } else {
                        StudyAssistantRes.colors.accents.red
                    },
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
        HomeworkTaskRow(tasks = tasks)
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
private fun HomeworkTaskRow(
    modifier: Modifier = Modifier,
    tasks: HomeworkTasksDetailsUi,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        tasks.components.forEach { homeworkTask ->
            when (homeworkTask) {
                is HomeworkTaskComponentUi.Label -> {
                    Text(
                        modifier = Modifier.padding(vertical = 2.dp),
                        text = buildString { append(homeworkTask.text, ": ") },
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                is HomeworkTaskComponentUi.Tasks -> homeworkTask.taskList.forEach { taskText ->
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    ) {
                        Box(modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)) {
                            Text(
                                text = taskText,
                                color = MaterialTheme.colorScheme.onSurface,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
        }
    }
}