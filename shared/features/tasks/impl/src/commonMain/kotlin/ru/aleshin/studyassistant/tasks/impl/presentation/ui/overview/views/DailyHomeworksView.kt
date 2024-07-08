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

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.tokens.monthNames
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTaskComponentUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.fetchAllTasks
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 29.06.2024.
 */
@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun DailyHomeworksView(
    modifier: Modifier = Modifier,
    date: Instant,
    isCurrent: Boolean,
    isPassed: Boolean,
    homeworks: List<HomeworkDetailsUi>,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onOpenHomeworkTask: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onRepeatHomework: (HomeworkDetailsUi) -> Unit,
    onShareHomeworks: (List<HomeworkDetailsUi>) -> Unit,
) {
    Surface(
        modifier = modifier.size(170.dp, 350.dp),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column {
            DailyHomeworksViewHeader(
                date = date,
                isHighlighted = isCurrent,
                onShare = { onShareHomeworks(homeworks) },
            )
            LazyColumn(
                modifier = Modifier.padding(4.dp).weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (homeworks.isNotEmpty()) {
                    items(homeworks, key = { it.uid }) { homework ->
                        ShortHomeworkViewItem(
                            status = homework.status,
                            subject = homework.subject,
                            theoreticalTasks = homework.theoreticalTasks.components,
                            practicalTasks = homework.practicalTasks.components,
                            presentationTasks = homework.presentationTasks.components,
                            isPassed = isPassed,
                            onDone = { onDoHomework(homework) },
                            onOpenTask = { onOpenHomeworkTask(homework) },
                            onSkip = { onSkipHomework(homework) },
                            onRepeat = { onRepeatHomework(homework) },
                        )
                    }
                } else {
                    item {
                        Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = TasksThemeRes.strings.noneTasksTitle,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.titleSmall,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun DailyHomeworksViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    Box {
        PlaceholderBox(
            modifier = modifier.size(170.dp, 350.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        )
        PlaceholderBox(
            modifier = modifier.size(170.dp, 48.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        )
    }
}

@Composable
private fun DailyHomeworksViewHeader(
    modifier: Modifier = Modifier,
    date: Instant,
    isHighlighted: Boolean,
    onShare: () -> Unit,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = if (isHighlighted) {
            MaterialTheme.colorScheme.secondaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
    ) {
        Row(
            modifier = Modifier.padding(start = 8.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val strings = StudyAssistantRes.strings
                val dateFormat = DateTimeComponents.Format {
                    dayOfMonth()
                    char(' ')
                    monthName(strings.monthNames())
                }
                Text(
                    text = date.dateTime().dayOfWeek.mapToSting(strings),
                    color = if (isHighlighted) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    style = MaterialTheme.typography.labelLarge,
                )
                Text(
                    text = date.formatByTimeZone(dateFormat),
                    color = if (isHighlighted) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            IconButton(
                modifier = Modifier.size(32.dp),
                onClick = onShare,
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    tint = if (isHighlighted) {
                        MaterialTheme.colorScheme.onSecondaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun ShortHomeworkViewItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    status: HomeworkStatus,
    subject: SubjectUi?,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
    isPassed: Boolean,
    onDone: () -> Unit,
    onOpenTask: () -> Unit,
    onSkip: () -> Unit,
    onRepeat: () -> Unit,
) {
    val dismissState = rememberSwipeToDismissBoxState(
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
            return@rememberSwipeToDismissBoxState false
        },
        positionalThreshold = { it * .4f },
    )
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.fillMaxWidth().clipToBounds(),
        backgroundContent = {
            SwipeToDismissBackground(
                dismissState = dismissState,
                shape = MaterialTheme.shapes.extraSmall,
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
        ShortHomeworkView(
            onClick = onOpenTask,
            enabled = enabled,
            status = status,
            subject = subject,
            theoreticalTasks = theoreticalTasks,
            practicalTasks = practicalTasks,
            presentationTasks = presentationTasks,
            isPassed = isPassed,
        )
    }
}

@Composable
private fun ShortHomeworkView(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    status: HomeworkStatus,
    subject: SubjectUi?,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
    isPassed: Boolean,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    Row(
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(all = 4.dp)
            .clickable(
                indication = LocalIndication.current,
                interactionSource = interactionSource,
                enabled = enabled,
                onClick = onClick,
            ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxHeight().width(4.dp),
            shape = MaterialTheme.shapes.small,
            color = subject?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.outline,
            content = { Box(modifier = Modifier.fillMaxHeight()) }
        )
        ErrorHomeworkViewContent(
            modifier = Modifier.weight(1f),
            subject = subject?.name,
            theoreticalTasks = theoreticalTasks,
            practicalTasks = practicalTasks,
            presentationTasks = presentationTasks,
        )
        when (status) {
            HomeworkStatus.COMPLETE -> Icon(
                modifier = modifier.size(18.dp),
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = if (isPassed) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    StudyAssistantRes.colors.accents.green
                },
            )
            HomeworkStatus.WAIT -> Icon(
                modifier = modifier.size(18.dp),
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = StudyAssistantRes.colors.accents.orange,
            )
            HomeworkStatus.IN_FUTURE -> Icon(
                modifier = modifier.size(18.dp),
                imageVector = Icons.Default.AccessTime,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            HomeworkStatus.NOT_COMPLETE -> Icon(
                modifier = modifier.size(18.dp),
                imageVector = Icons.Default.Error,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
            )
            HomeworkStatus.SKIPPED -> Icon(
                modifier = modifier.size(18.dp),
                imageVector = Icons.Default.Close,
                contentDescription = null,
                tint = if (isPassed) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
    }
}

@Composable
private fun ErrorHomeworkViewContent(
    modifier: Modifier = Modifier,
    subject: String?,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = subject ?: StudyAssistantRes.strings.noneTitle,
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            maxLines = 4,
            style = MaterialTheme.typography.titleSmall,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ErrorHomeworkTaskCountView(
                painter = painterResource(StudyAssistantRes.icons.theoreticalTasks),
                count = theoreticalTasks.fetchAllTasks().size,
            )
            ErrorHomeworkTaskCountView(
                painter = painterResource(StudyAssistantRes.icons.practicalTasks),
                count = practicalTasks.fetchAllTasks().size,
            )
            ErrorHomeworkTaskCountView(
                painter = painterResource(StudyAssistantRes.icons.presentationTasks),
                count = presentationTasks.fetchAllTasks().size,
            )
        }
    }
}

@Composable
private fun ErrorHomeworkTaskCountView(
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
            modifier = Modifier.size(18.dp),
            painter = painter,
            contentDescription = description,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}