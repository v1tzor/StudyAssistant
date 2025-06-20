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

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue.EndToStart
import androidx.compose.material3.SwipeToDismissBoxValue.Settled
import androidx.compose.material3.SwipeToDismissBoxValue.StartToEnd
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.unit.sp
import kotlinx.datetime.Instant
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.functional.Constants
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalType
import ru.aleshin.studyassistant.core.domain.entities.tasks.HomeworkStatus
import ru.aleshin.studyassistant.core.ui.mappers.mapToSting
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.SwipeToDismissBackground
import ru.aleshin.studyassistant.core.ui.views.dayMonthFormat
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalCreateModelUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.subjects.SubjectUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.DailyHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkTaskComponentUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.convertToBase
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.fetchAllTasks
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.CompactHomeworkTaskView
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.DeleteGoalWarningDialog
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.common.GoalCreatorDialog

/**
 * @author Stanislav Aleshin on 29.06.2024.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DailyHomeworksDetailsView(
    modifier: Modifier = Modifier,
    isPaidUser: Boolean,
    date: Instant,
    currentDate: Instant,
    isPassed: Boolean,
    dailyHomeworks: DailyHomeworksUi,
    onAddHomework: (Instant) -> Unit,
    onDoHomework: (HomeworkDetailsUi) -> Unit,
    onOpenHomeworkTask: (HomeworkDetailsUi) -> Unit,
    onSkipHomework: (HomeworkDetailsUi) -> Unit,
    onRepeatHomework: (HomeworkDetailsUi) -> Unit,
    onScheduleGoal: (GoalCreateModelUi) -> Unit,
    onDeleteGoal: (GoalShortUi) -> Unit,
    onShareHomeworks: () -> Unit,
    onOpenBillingScreen: () -> Unit,
) {
    Column(
        modifier = modifier.fillMaxHeight().width(182.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        val homeworks = dailyHomeworks.homeworks
        val completedHomeworks = remember(homeworks) {
            homeworks.getOrElse(HomeworkStatus.COMPLETE) { emptyList() } +
                homeworks.getOrElse(HomeworkStatus.SKIPPED) { emptyList() }
        }
        val runningHomeworks = remember(homeworks) {
            homeworks.getOrElse(HomeworkStatus.WAIT) { emptyList() } +
                homeworks.getOrElse(HomeworkStatus.IN_FUTURE) { emptyList() }
        }
        val errorHomeworks = remember(homeworks) {
            homeworks.getOrElse(HomeworkStatus.NOT_COMPLETE) { emptyList() }
        }
        DailyHomeworksViewHeader(
            targetDate = date,
            currentDate = currentDate,
            onShare = onShareHomeworks,
        )
        LazyColumn(
            modifier = Modifier.padding(4.dp).weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (completedHomeworks.isNotEmpty()) {
                items(completedHomeworks, key = { it.uid }) { homework ->
                    DetailsHomeworkViewItem(
                        modifier = Modifier.animateItem(),
                        status = homework.status,
                        subject = homework.subject,
                        linkedGoal = homework.linkedGoal,
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
                item { DailyHomeworksHorizontalDivider() }
            }
            if (runningHomeworks.isNotEmpty()) {
                items(runningHomeworks, key = { it.uid }) { homework ->
                    var goalCreatorState by rememberSaveable { mutableStateOf(false) }
                    var deleteWarningDialogState by rememberSaveable { mutableStateOf(false) }

                    DetailsHomeworkViewItem(
                        modifier = Modifier.animateItem(),
                        status = homework.status,
                        subject = homework.subject,
                        theoreticalTasks = homework.theoreticalTasks.components,
                        practicalTasks = homework.practicalTasks.components,
                        presentationTasks = homework.presentationTasks.components,
                        linkedGoal = homework.linkedGoal,
                        isPassed = isPassed,
                        onDone = { onDoHomework(homework) },
                        onOpenTask = { onOpenHomeworkTask(homework) },
                        onSkip = { onSkipHomework(homework) },
                        onRepeat = { onRepeatHomework(homework) },
                        onDeleteGoal = { deleteWarningDialogState = true },
                        onScheduleGoal = {
                            if (isPaidUser) goalCreatorState = true else onOpenBillingScreen()
                        },
                    )

                    if (deleteWarningDialogState) {
                        DeleteGoalWarningDialog(
                            onDismiss = { deleteWarningDialogState = false },
                            onDelete = {
                                homework.linkedGoal?.let { onDeleteGoal(it) }
                                deleteWarningDialogState = false
                            },
                        )
                    }
                    if (goalCreatorState) {
                        GoalCreatorDialog(
                            contentType = GoalType.HOMEWORK,
                            currentDate = currentDate,
                            contentHomework = homework.convertToBase(),
                            contentTodo = null,
                            onDismiss = { goalCreatorState = false },
                            onCreate = {
                                goalCreatorState = false
                                onScheduleGoal(it)
                            },
                        )
                    }
                }
            }
            if ((completedHomeworks.isNotEmpty() || runningHomeworks.isNotEmpty()) && errorHomeworks.isNotEmpty()) {
                item { DailyHomeworksHorizontalDivider() }
            }
            if (errorHomeworks.isNotEmpty()) {
                items(errorHomeworks, key = { it.uid }) { homework ->
                    var goalCreatorState by rememberSaveable { mutableStateOf(false) }
                    var deleteWarningDialogState by rememberSaveable { mutableStateOf(false) }

                    DetailsHomeworkViewItem(
                        modifier = Modifier.animateItem(),
                        status = homework.status,
                        subject = homework.subject,
                        theoreticalTasks = homework.theoreticalTasks.components,
                        practicalTasks = homework.practicalTasks.components,
                        presentationTasks = homework.presentationTasks.components,
                        linkedGoal = homework.linkedGoal,
                        isPassed = isPassed,
                        onDone = { onDoHomework(homework) },
                        onOpenTask = { onOpenHomeworkTask(homework) },
                        onSkip = { onSkipHomework(homework) },
                        onRepeat = { onRepeatHomework(homework) },
                        onDeleteGoal = { deleteWarningDialogState = true },
                        onScheduleGoal = {
                            if (isPaidUser) goalCreatorState = true else onOpenBillingScreen()
                        },
                    )

                    if (deleteWarningDialogState) {
                        DeleteGoalWarningDialog(
                            onDismiss = { deleteWarningDialogState = false },
                            onDelete = {
                                homework.linkedGoal?.let { onDeleteGoal(it) }
                                deleteWarningDialogState = false
                            },
                        )
                    }
                    if (goalCreatorState) {
                        GoalCreatorDialog(
                            contentType = GoalType.HOMEWORK,
                            currentDate = currentDate,
                            contentHomework = homework.convertToBase(),
                            contentTodo = null,
                            onDismiss = { goalCreatorState = false },
                            onCreate = {
                                goalCreatorState = false
                                onScheduleGoal(it)
                            },
                        )
                    }
                }
            }
            if (completedHomeworks.isNotEmpty() || runningHomeworks.isNotEmpty() || errorHomeworks.isNotEmpty()) {
                item { DailyHomeworksHorizontalDivider() }
            }
            item {
                AddHomeworkButton(onClick = { onAddHomework(date) })
            }
        }
    }
}

@Composable
internal fun DailyHomeworksDetailsViewPlaceholder(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlaceholderBox(
            modifier = Modifier.size(182.dp, 28.dp),
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        )
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(Constants.Placeholder.HOMEWORKS) {
                PlaceholderBox(
                    modifier = Modifier.size(182.dp, 160.dp),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
            }
        }
    }
}

@Composable
private fun DailyHomeworksViewHeader(
    modifier: Modifier = Modifier,
    currentDate: Instant,
    targetDate: Instant,
    onShare: () -> Unit,
) {
    var isOpenActionMenu by rememberSaveable { mutableStateOf(false) }
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = buildAnnotatedString {
                withStyle(
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                    ).toSpanStyle()
                ) {
                    append(targetDate.dateTime().dayOfWeek.mapToSting(StudyAssistantRes.strings))
                }
                withStyle(
                    style = MaterialTheme.typography.labelLarge.copy(
                        color = when (targetDate.equalsDay(currentDate)) {
                            true -> MaterialTheme.colorScheme.tertiary
                            false -> MaterialTheme.colorScheme.primary
                        },
                        fontWeight = FontWeight.Bold,
                    ).toSpanStyle()
                ) {
                    append(" • ")
                    if (targetDate.equalsDay(currentDate)) {
                        append(StudyAssistantRes.strings.todayTitle)
                    } else if (targetDate.equalsDay(currentDate.shiftDay(1))) {
                        append(StudyAssistantRes.strings.tomorrowTitle)
                    } else {
                        val format =
                            DateTimeComponents.Formats.dayMonthFormat(StudyAssistantRes.strings)
                        append(targetDate.formatByTimeZone(format))
                    }
                }
            },
            maxLines = 1,
        )
        Box {
            IconButton(
                onClick = { isOpenActionMenu = true },
                modifier = Modifier.size(28.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            DailyHomeworksDetailsMenu(
                expanded = isOpenActionMenu,
                onDismiss = { isOpenActionMenu = false },
                onShare = onShare,
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun DetailsHomeworkViewItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    status: HomeworkStatus,
    subject: SubjectUi?,
    linkedGoal: GoalShortUi?,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
    isPassed: Boolean,
    onScheduleGoal: () -> Unit = {},
    onDeleteGoal: () -> Unit = {},
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
        DetailsHomeworkView(
            enabled = enabled,
            status = status,
            subject = subject,
            linkedGoal = linkedGoal,
            theoreticalTasks = theoreticalTasks,
            practicalTasks = practicalTasks,
            presentationTasks = presentationTasks,
            isPassed = isPassed,
            onClick = onOpenTask,
            onDeleteGoal = onDeleteGoal,
            onScheduleGoal = onScheduleGoal,
        )
    }
}

@Composable
private fun DetailsHomeworkView(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    status: HomeworkStatus,
    subject: SubjectUi?,
    linkedGoal: GoalShortUi?,
    isPassed: Boolean,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    onClick: () -> Unit,
    onScheduleGoal: () -> Unit,
    onDeleteGoal: () -> Unit,
) {
    Row(
        modifier = modifier
            .animateContentSize()
            .height(IntrinsicSize.Min)
            .background(backgroundColor)
            .clip(RoundedCornerShape(2.dp))
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        val indicatorColor = subject?.color?.let { Color(it) }
        Surface(
            modifier = Modifier.fillMaxHeight().width(4.dp),
            shape = MaterialTheme.shapes.small,
            color = indicatorColor ?: MaterialTheme.colorScheme.outline,
            content = { Box(modifier = Modifier.fillMaxHeight()) }
        )
        ShortHomeworkViewContent(
            modifier = Modifier.weight(1f),
            subject = subject?.name,
            linkedGoal = linkedGoal,
            status = status,
            isPassed = isPassed,
            theoreticalTasks = theoreticalTasks,
            practicalTasks = practicalTasks,
            presentationTasks = presentationTasks,
            onScheduleGoal = onScheduleGoal,
            onDeleteGoal = onDeleteGoal,
        )
    }
}

@Composable
private fun ShortHomeworkViewContent(
    modifier: Modifier = Modifier,
    subject: String?,
    status: HomeworkStatus,
    linkedGoal: GoalShortUi?,
    isPassed: Boolean,
    theoreticalTasks: List<HomeworkTaskComponentUi>,
    practicalTasks: List<HomeworkTaskComponentUi>,
    presentationTasks: List<HomeworkTaskComponentUi>,
    onScheduleGoal: () -> Unit,
    onDeleteGoal: () -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
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
                lineHeight = 18.sp,
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
        if (status == HomeworkStatus.COMPLETE || status == HomeworkStatus.NOT_COMPLETE) {
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
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (theoreticalTasks.isNotEmpty()) {
                    CompactHomeworkTaskView(
                        icon = painterResource(StudyAssistantRes.icons.theoreticalTasks),
                        tasks = theoreticalTasks,
                    )
                }
                if (practicalTasks.isNotEmpty()) {
                    CompactHomeworkTaskView(
                        icon = painterResource(StudyAssistantRes.icons.practicalTasks),
                        tasks = practicalTasks,
                    )
                }
                if (presentationTasks.isNotEmpty()) {
                    CompactHomeworkTaskView(
                        icon = painterResource(StudyAssistantRes.icons.presentationTasks),
                        tasks = presentationTasks,
                    )
                }
            }
        }
        if (linkedGoal == null && status != HomeworkStatus.COMPLETE && status != HomeworkStatus.SKIPPED) {
            ScheduleGoalButton(onClick = onScheduleGoal)
        } else if (linkedGoal != null && status != HomeworkStatus.COMPLETE && status != HomeworkStatus.SKIPPED) {
            CancelGoalButton(
                onClick = onDeleteGoal,
                targetDay = linkedGoal.targetDate,
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
    startPadding: Dp = 12.dp,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
) {
    Canvas(modifier.fillMaxWidth().height(thickness)) {
        drawLine(
            color = color,
            strokeWidth = thickness.toPx(),
            start = Offset(startPadding.toPx(), (thickness / 2).toPx()),
            end = Offset(size.width, (thickness / 2).toPx()),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(3.dp.toPx(), 3.dp.toPx()))
        )
    }
}