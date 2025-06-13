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

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.material3.SwipeToDismissBoxValue.Settled
import androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.domain.entities.tasks.DailyHomeworksStatus
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.HomeworksCompleteBadge
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.DailyHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTaskComponentUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.fetchAllTasks
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 29.06.2024.
 */
@Composable
internal fun DailyHomeworksView(
    modifier: Modifier = Modifier,
    date: Instant,
    currentDate: Instant,
    isPassed: Boolean,
    dailyHomeworks: DailyHomeworksUi,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onOpenHomeworkTask: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onRepeatHomework: (HomeworkDetailsUi) -> Unit,
    onShareHomeworks: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxHeight().width(170.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val homeworks = dailyHomeworks.homeworks
        val completedHomeworks = remember(homeworks) {
            homeworks.getOrElse(HomeworkStatus.COMPLETE) { emptyList() } + homeworks.getOrElse(HomeworkStatus.SKIPPED) { emptyList() }
        }
        val runningHomeworks = remember(homeworks) {
            homeworks.getOrElse(HomeworkStatus.WAIT) { emptyList() } + homeworks.getOrElse(HomeworkStatus.IN_FUTURE) { emptyList() }
        }
        val errorHomeworks = remember(homeworks) {
            homeworks.getOrElse(HomeworkStatus.NOT_COMPLETE) { emptyList() }
        }
        DailyHomeworksViewHeader(
            targetDate = date,
            currentDate = currentDate,
            totalHomeworks = remember(homeworks) { homeworks.map { it.value }.extractAllItem().count() },
            completedHomeworks = remember(homeworks) {
                homeworks[HomeworkStatus.COMPLETE]?.size ?: 0
            },
            listStatus = dailyHomeworks.dailyStatus,
        )
        LazyColumn(
            modifier = Modifier.padding(4.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (homeworks.isNotEmpty()) {
                if (completedHomeworks.isNotEmpty()) {
                    items(completedHomeworks, key = { it.uid }) { homework ->
                        ShortHomeworkViewItem(
                            modifier = Modifier.animateItem(),
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
                }
                if (completedHomeworks.isNotEmpty() && runningHomeworks.isNotEmpty()) {
                    item { DailyHomeworksHorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                }
                if (runningHomeworks.isNotEmpty()) {
                    items(runningHomeworks, key = { it.uid }) { homework ->
                        ShortHomeworkViewItem(
                            modifier = Modifier.animateItem(),
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
                }
                if ((completedHomeworks.isNotEmpty() || runningHomeworks.isNotEmpty()) && errorHomeworks.isNotEmpty()) {
                    item { DailyHomeworksHorizontalDivider(modifier = Modifier.padding(vertical = 4.dp)) }
                }
                if (errorHomeworks.isNotEmpty()) {
                    items(errorHomeworks, key = { it.uid }) { homework ->
                        ShortHomeworkViewItem(
                            modifier = Modifier.animateItem(),
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
                }
            } else {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = TasksThemeRes.strings.noneTasksTitle,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.titleSmall,
                        )
                    }
                }
            }
        }
        if (homeworks.isNotEmpty()) {
            OutlinedButton(
                onClick = onShareHomeworks,
                modifier = Modifier.fillMaxWidth().height(32.dp),
                contentPadding = PaddingValues(start = 12.dp, end = 8.dp, top = 7.dp, bottom = 7.dp),
                shape = MaterialTheme.shapes.medium,
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = TasksThemeRes.strings.shareHomeworksButtonTitle,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Icon(
                        modifier = Modifier.size(18.dp),
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                    )
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
    targetDate: Instant,
    currentDate: Instant,
    totalHomeworks: Int,
    completedHomeworks: Int,
    listStatus: DailyHomeworksStatus,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (targetDate.equalsDay(currentDate)) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Default.Today,
                contentDescription = null,
                tint = StudyAssistantRes.colors.accents.orange,
            )
        }
        Text(
            modifier = Modifier.weight(1f),
            text = buildAnnotatedString {
                withStyle(
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold
                    ).toSpanStyle()
                ) {
                    append(targetDate.dateTime().dayOfMonth.toString())
                    append(" | ")
                }
                withStyle(style = MaterialTheme.typography.titleSmall.toSpanStyle()) {
                    if (targetDate.equalsDay(currentDate)) {
                        append(StudyAssistantRes.strings.todayTitle)
                    } else if (targetDate.equalsDay(currentDate.shiftDay(1))) {
                        append(StudyAssistantRes.strings.tomorrowTitle)
                    } else {
                        append(targetDate.dateTime().dayOfWeek.mapToSting(StudyAssistantRes.strings))
                    }
                }
            },
            maxLines = 1,
        )
        HomeworksCompleteBadge(
            listStatus = listStatus,
            totalHomeworks = totalHomeworks,
            completedHomeworks = completedHomeworks,
        )
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
    val density = LocalDensity.current
    val dismissState = remember(status) {
        SwipeToDismissBoxState(
            initialValue = Settled,
            density = density,
            confirmValueChange = { dismissValue ->
                when {
                    dismissValue == EndToStart && status == HomeworkStatus.COMPLETE -> onRepeat()
                    dismissValue == EndToStart && status != HomeworkStatus.COMPLETE -> onDone()
                    dismissValue == StartToEnd && status == HomeworkStatus.SKIPPED -> onRepeat()
                    dismissValue == StartToEnd && status != HomeworkStatus.SKIPPED -> onSkip()
                    else -> {}
                }
                false
            },
            positionalThreshold = { it * .4f }
        )
    }
    SwipeToDismissBox(
        state = dismissState,
        modifier = modifier.fillMaxWidth().clipToBounds(),
        backgroundContent = {
            SwipeToDismissBackground(
                dismissState = dismissState,
                shape = MaterialTheme.shapes.medium,
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
                }
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
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
) {
    Surface(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier
            .height(IntrinsicSize.Min)
            .clip(MaterialTheme.shapes.medium)
            .background(backgroundColor),
        shape = MaterialTheme.shapes.medium,
        color = if (subject?.color != null) {
            Color(subject.color).copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surfaceContainerHigh
        },
    ) {
        Row {
            Surface(
                modifier = Modifier.fillMaxHeight().width(4.dp).padding(vertical = 8.dp),
                shape = MaterialTheme.shapes.small,
                color = subject?.color?.let { Color(it) } ?: MaterialTheme.colorScheme.outline,
                content = { Box(modifier = Modifier.fillMaxHeight()) }
            )
            ShortHomeworkViewContent(
                modifier = Modifier.weight(1f),
                subject = subject?.name,
                status = status,
                isPassed = isPassed,
                theoreticalTasks = theoreticalTasks,
                practicalTasks = practicalTasks,
                presentationTasks = presentationTasks,
            )
        }
    }
}

@Composable
private fun ShortHomeworkViewContent(
    modifier: Modifier = Modifier,
    subject: String?,
    status: HomeworkStatus,
    isPassed: Boolean,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
) {
    Column(
        modifier = modifier.padding(start = 8.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = subject ?: StudyAssistantRes.strings.noneTitle,
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
                maxLines = 2,
                style = MaterialTheme.typography.titleSmall,
            )
            Spacer(modifier = Modifier.weight(1f))
            when (status) {
                HomeworkStatus.COMPLETE -> Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (isPassed) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        StudyAssistantRes.colors.accents.green
                    },
                )
                HomeworkStatus.WAIT -> Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = StudyAssistantRes.colors.accents.orange,
                )
                HomeworkStatus.IN_FUTURE -> Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                HomeworkStatus.NOT_COMPLETE -> Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                )
                HomeworkStatus.SKIPPED -> Icon(
                    modifier = Modifier.size(18.dp),
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
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            ShortHomeworkTaskCountView(
                painter = painterResource(StudyAssistantRes.icons.theoreticalTasks),
                count = theoreticalTasks.fetchAllTasks().size,
            )
            ShortHomeworkTaskCountView(
                painter = painterResource(StudyAssistantRes.icons.practicalTasks),
                count = practicalTasks.fetchAllTasks().size,
            )
            ShortHomeworkTaskCountView(
                painter = painterResource(StudyAssistantRes.icons.presentationTasks),
                count = presentationTasks.fetchAllTasks().size,
            )
        }
    }
}

@Composable
private fun ShortHomeworkTaskCountView(
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
            tint = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = count.toString(),
            color = MaterialTheme.colorScheme.onSurface,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun DailyHomeworksHorizontalDivider(
    modifier: Modifier = Modifier,
    thickness: Dp = 1.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    Canvas(modifier.fillMaxWidth().height(thickness)) {
        drawLine(
            color = color,
            strokeWidth = thickness.toPx(),
            start = Offset(0f, (thickness / 2).toPx()),
            end = Offset(size.width, (thickness / 2).toPx()),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
        )
    }
}