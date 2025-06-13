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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views

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
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTaskComponentUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTasksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 21.06.2024.
 */
@Composable
internal fun GoalHomeworkTasksView(
    modifier: Modifier = Modifier,
    theoreticalTasks: HomeworkTasksDetailsUi?,
    practicalTasks: HomeworkTasksDetailsUi?,
    presentationTasks: HomeworkTasksDetailsUi?,
    priority: TaskPriority?
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
            if (!theoreticalTasks?.components.isNullOrEmpty()) {
                HomeworkTaskView(
                    icon = painterResource(StudyAssistantRes.icons.theoreticalTasks),
                    title = StudyAssistantRes.strings.theoreticalTasksTitle,
                    priority = priority,
                    tasks = theoreticalTasks,
                )
            }
            if (!theoreticalTasks?.components.isNullOrEmpty() && !practicalTasks?.components.isNullOrEmpty()) {
                HorizontalDivider()
            }
            if (!practicalTasks?.components.isNullOrEmpty()) {
                HomeworkTaskView(
                    icon = painterResource(StudyAssistantRes.icons.practicalTasks),
                    title = StudyAssistantRes.strings.practicalTasksTitle,
                    priority = priority,
                    tasks = practicalTasks,
                )
            }
            if (!presentationTasks?.components.isNullOrEmpty() &&
                (!theoreticalTasks?.components.isNullOrEmpty() || !practicalTasks?.components.isNullOrEmpty())
            ) {
                HorizontalDivider()
            }
            if (!presentationTasks?.components.isNullOrEmpty()) {
                HomeworkTaskView(
                    icon = painterResource(StudyAssistantRes.icons.presentationTasks),
                    title = StudyAssistantRes.strings.presentationsTasksTitle,
                    priority = priority,
                    tasks = presentationTasks,
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
                painter = painterResource(StudyAssistantRes.icons.test),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            Text(
                text = TasksThemeRes.strings.testLabel,
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
    priority: TaskPriority?,
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
                    text = priority.mapToString(StudyAssistantRes.strings),
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