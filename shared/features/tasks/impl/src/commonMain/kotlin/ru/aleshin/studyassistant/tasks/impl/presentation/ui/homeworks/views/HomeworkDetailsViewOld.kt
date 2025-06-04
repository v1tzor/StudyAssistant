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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.views

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.functional.Constants.Placeholder
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.TaskPriority
import ru.aleshin.studyassistant.core.ui.mappers.mapToString
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.ExpandedIcon
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.tasks.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTaskComponentUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.fetchAllTasks
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.HomeworkTaskTestView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.HomeworkTaskView

/**
 * @author Stanislav Aleshin on 03.07.2024.
 */
@Composable
internal fun HomeworksDetailsViewItemOld(
    modifier: Modifier = Modifier,
    subject: SubjectUi?,
    organization: OrganizationShortUi,
    status: HomeworkStatus,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
    testTopic: String?,
    priority: TaskPriority,
    completeDate: Instant?,
    onEdit: () -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    onRepeat: () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            var isExpandedContent by remember {
                mutableStateOf(status != HomeworkStatus.COMPLETE && status != HomeworkStatus.SKIPPED)
            }
            HomeworksDetailsViewHeader(
                onClick = onEdit,
                subject = subject,
                organization = organization,
                status = status,
                priority = priority,
                isExpandedContent = isExpandedContent,
                onExpanded = { isExpandedContent = it },
                onDone = onDone,
                onRepeat = onRepeat,
                onSkip = onSkip,
            )
            HomeworksDetailsViewContent(
                isExpanded = isExpandedContent,
                theoreticalTasks = theoreticalTasks,
                practicalTasks = practicalTasks,
                presentationTasks = presentationTasks,
                testTopic = testTopic,
                completeDate = completeDate,
            )
        }
    }
}

@Composable
internal fun HomeworksDetailsViewNoneItem(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    painter = painterResource(StudyAssistantRes.icons.practicalTasks),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = TasksThemeRes.strings.noneTasksTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@Composable
internal fun HomeworksDetailsViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlaceholderBox(
            modifier = Modifier.fillMaxWidth().height(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        )
        repeat(Placeholder.HOMEWORKS) {
            PlaceholderBox(
                modifier = Modifier.fillMaxWidth().height(250.dp),
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeworksDetailsViewHeader(
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier,
    subject: SubjectUi?,
    organization: OrganizationShortUi,
    status: HomeworkStatus,
    priority: TaskPriority,
    isExpandedContent: Boolean,
    onExpanded: (Boolean) -> Unit,
    onDone: () -> Unit,
    onSkip: () -> Unit,
    onRepeat: () -> Unit,
) {
    val density = LocalDensity.current
    val dismissHeaderState = remember(status) {
        SwipeToDismissBoxState(
            initialValue = SwipeToDismissBoxValue.Settled,
            density = density,
            confirmValueChange = { dismissBoxValue ->
                when (dismissBoxValue) {
                    SwipeToDismissBoxValue.EndToStart -> if (status == HomeworkStatus.COMPLETE) {
                        onRepeat()
                    } else {
                        onDone()
                    }
                    SwipeToDismissBoxValue.StartToEnd -> if (status == HomeworkStatus.SKIPPED) {
                        onRepeat()
                    } else {
                        onSkip()
                    }
                    SwipeToDismissBoxValue.Settled -> {}
                }
                false
            },
            positionalThreshold = { it * .5f }
        )
    }
    SwipeToDismissBox(
        state = dismissHeaderState,
        modifier = modifier.fillMaxWidth().clipToBounds(),
        backgroundContent = {
            SwipeToDismissBackground(
                dismissState = dismissHeaderState,
                shape = MaterialTheme.shapes.large,
                endToStartContent = {
                    if (status == HomeworkStatus.COMPLETE) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    }
                },
                startToEndContent = {
                    if (status == HomeworkStatus.SKIPPED) {
                        Icon(imageVector = Icons.Default.AccessTime, contentDescription = null)
                    } else {
                        Icon(imageVector = Icons.Default.Close, contentDescription = null)
                    }
                },
                endToStartColor = if (status == HomeworkStatus.COMPLETE) {
                    StudyAssistantRes.colors.accents.orangeContainer
                } else {
                    StudyAssistantRes.colors.accents.greenContainer
                },
                startToEndColor = if (status == HomeworkStatus.SKIPPED) {
                    StudyAssistantRes.colors.accents.orangeContainer
                } else {
                    MaterialTheme.colorScheme.errorContainer
                },
                settledColor = MaterialTheme.colorScheme.surfaceContainerLow,
            )
        },
    ) {
        Surface(
            onClick = onClick,
            enabled = enabled,
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(modifier = Modifier.height(IntrinsicSize.Min)) {
                Surface(
                    modifier = Modifier.padding(vertical = 12.dp).fillMaxHeight().width(5.dp),
                    shape = MaterialTheme.shapes.small,
                    color = subject?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.outline,
                    content = { Box(modifier = Modifier.fillMaxHeight()) }
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 12.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
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
                        Text(
                            text = subject?.name ?: StudyAssistantRes.strings.noneTitle,
                            color = MaterialTheme.colorScheme.onSurface,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 2,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                modifier = modifier.size(18.dp),
                                painter = painterResource(StudyAssistantRes.icons.organizationGeo),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            Text(
                                text = organization.shortName,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        when (status) {
                            HomeworkStatus.COMPLETE -> Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = StudyAssistantRes.colors.accents.green,
                            )
                            HomeworkStatus.WAIT -> Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = StudyAssistantRes.colors.accents.orange,
                            )
                            HomeworkStatus.IN_FUTURE -> Icon(
                                imageVector = Icons.Default.AccessTime,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                            )
                            HomeworkStatus.NOT_COMPLETE -> Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                            )
                            HomeworkStatus.SKIPPED -> Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        IconButton(
                            modifier = Modifier.size(32.dp),
                            onClick = { onExpanded(!isExpandedContent) },
                        ) {
                            ExpandedIcon(isExpanded = isExpandedContent)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeworksDetailsViewContent(
    modifier: Modifier = Modifier,
    isExpanded: Boolean,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
    testTopic: String?,
    completeDate: Instant?,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 12.dp, end = 12.dp, bottom = 12.dp, top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isExpanded) {
            if (testTopic != null) {
                HomeworkTaskTestView(
                    topic = testTopic,
                )
            }
            if (theoreticalTasks.isNotEmpty()) {
                HomeworkTaskView(
                    icon = painterResource(StudyAssistantRes.icons.theoreticalTasks),
                    title = StudyAssistantRes.strings.theoreticalTasksTitle,
                    tasks = theoreticalTasks,
                )
            }
            if (practicalTasks.isNotEmpty()) {
                HomeworkTaskView(
                    icon = painterResource(StudyAssistantRes.icons.practicalTasks),
                    title = StudyAssistantRes.strings.practicalTasksTitle,
                    tasks = practicalTasks,
                )
            }
            if (presentationTasks.isNotEmpty()) {
                HomeworkTaskView(
                    icon = painterResource(StudyAssistantRes.icons.presentationTasks),
                    title = StudyAssistantRes.strings.presentationsTasksTitle,
                    tasks = presentationTasks,
                )
            }
        } else {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                HomeworkTaskCountView(
                    painter = painterResource(StudyAssistantRes.icons.theoreticalTasks),
                    count = theoreticalTasks.fetchAllTasks().size,
                )
                HomeworkTaskCountView(
                    painter = painterResource(StudyAssistantRes.icons.practicalTasks),
                    count = practicalTasks.fetchAllTasks().size,
                )
                HomeworkTaskCountView(
                    painter = painterResource(StudyAssistantRes.icons.presentationTasks),
                    count = presentationTasks.fetchAllTasks().size,
                )
                Spacer(modifier = Modifier.weight(1f))
                if (completeDate != null) {
                    val dateTimeFormat = DateTimeComponents.Format {
                        hour()
                        char(':')
                        minute()
                        chars(" | ")
                        dayOfMonth()
                        char('.')
                        monthNumber()
                    }
                    Text(
                        text = completeDate.formatByTimeZone(dateTimeFormat),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun HomeworkTaskCountView(
    modifier: Modifier = Modifier,
    painter: Painter,
    count: Int,
    description: String? = null,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painter,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.labelLarge,
        )
    }
}