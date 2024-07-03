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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.common

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import entities.tasks.HomeworkStatus
import extensions.toMinutesAndHoursString
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import theme.StudyAssistantRes
import theme.tokens.contentColorFor
import views.HorizontalLeftTimeProgress
import views.MediumDragHandle
import kotlin.math.roundToInt

/**
 * @author Stanislav Aleshin on 15.06.2024.
 */
@Composable
@ExperimentalMaterial3Api
internal fun ClassBottomSheet(
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    ),
    currentTime: Instant,
    activeClass: ActiveClassUi?,
    classModel: ClassDetailsUi,
    classDate: Instant,
    onAddHomework: (ClassDetailsUi, Instant) -> Unit,
    onEditHomework: (HomeworkDetailsUi) -> Unit,
    onAgainHomework: (HomeworkDetailsUi) -> Unit,
    onCompleteHomework: (HomeworkDetailsUi) -> Unit,
    onDismissRequest: () -> Unit,
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        dragHandle = { MediumDragHandle() },
        contentWindowInsets = { WindowInsets.navigationBars },
        tonalElevation = 0.dp,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ClassBottomSheetHeader(
                activeClass = activeClass,
                classModel = classModel,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = ScheduleThemeRes.strings.classSheetTaskTitle,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium,
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }
            val coroutineScope = rememberCoroutineScope()
            ClassBottomSheetFooter(
                currentTime = currentTime,
                homework = classModel.homework,
                onAddHomework = {
                    coroutineScope.launch { sheetState.hide() }
                    onAddHomework(classModel, classDate)
                },
                onEditHomework = {
                    coroutineScope.launch { sheetState.hide() }
                    onEditHomework(it)
                },
                onCompleteHomework = onCompleteHomework,
                onAgainHomework = onAgainHomework,
            )
        }
    }
}

@Composable
private fun ClassBottomSheetHeader(
    modifier: Modifier = Modifier,
    activeClass: ActiveClassUi?,
    classModel: ClassDetailsUi,
) {
    Row(
        modifier = modifier.fillMaxWidth().height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxHeight().width(5.dp),
            shape = RoundedCornerShape(topEnd = 100.dp, bottomEnd = 100.dp),
            color = classModel.subject?.color?.let { Color(it) }
                ?: MaterialTheme.colorScheme.outlineVariant,
            content = { Box(modifier = Modifier.fillMaxHeight()) }
        )
        Column(
            modifier = Modifier.animateContentSize().padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SheetClassView(
                subject = classModel.subject,
                office = classModel.office,
                organization = classModel.organization,
                teacher = classModel.teacher,
                location = classModel.location,
            )
            if (activeClass != null && activeClass.uid == classModel.uid && activeClass.isStarted) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.surfaceContainer,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        val leftTime = activeClass.duration.toMinutesAndHoursString()
                        val progress = ((activeClass.progress ?: 0f) * 100f).roundToInt().toString()
                        Text(
                            text = leftTime,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelMedium,
                        )
                        HorizontalLeftTimeProgress(
                            modifier = Modifier.weight(1f).height(20.dp),
                            leftTimeProgress = activeClass.progress,
                            trackHeight = 5.dp,
                            thumbWidth = 6.dp,
                        )
                        Text(
                            text = buildString {
                                append(progress, "%")
                            },
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ClassBottomSheetFooter(
    modifier: Modifier = Modifier,
    currentTime: Instant,
    homework: HomeworkDetailsUi?,
    onAddHomework: () -> Unit,
    onEditHomework: (HomeworkDetailsUi) -> Unit,
    onAgainHomework: (HomeworkDetailsUi) -> Unit,
    onCompleteHomework: (HomeworkDetailsUi) -> Unit,
) {
    Column(
        modifier = modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        if (homework != null) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (homework.test != null) {
                    TestHomeworkView(testTopic = homework.test)
                }
                SheetHomeworkView(
                    theoreticalTasks = homework.theoreticalTasks,
                    practicalTasks = homework.practicalTasks,
                    presentationTasks = homework.presentationTasks,
                    priority = homework.priority,
                )
            }
            ClassBottomSheetActions(
                homeworkStatus = homework.status,
                onEditHomework = { onEditHomework(homework) },
                onAddHomework = onAddHomework,
                onCompleteHomework = { onCompleteHomework(homework) },
                onAgainHomework = { onAgainHomework(homework) }
            )
        } else {
            NoneHomeworkView()
            ClassBottomSheetActions(
                homeworkStatus = null,
                onAddHomework = onAddHomework,
            )
        }
    }
}

@Composable
private fun ClassBottomSheetActions(
    modifier: Modifier = Modifier,
    homeworkStatus: HomeworkStatus?,
    onAddHomework: () -> Unit,
    onEditHomework: () -> Unit = {},
    onAgainHomework: () -> Unit = {},
    onCompleteHomework: () -> Unit = {},
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (homeworkStatus != null) {
            FilledTonalIconButton(
                modifier = Modifier.size(40.dp),
                onClick = onEditHomework,
            ) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }
            when (homeworkStatus) {
                HomeworkStatus.COMPLETE -> {
                    ClassBottomSheetActionView(
                        modifier = Modifier.weight(1f),
                        text = ScheduleThemeRes.strings.homeworkIsCompleteTitle,
                        color = StudyAssistantRes.colors.accents.green,
                    )
                    ClassBottomSheetActionButton(
                        onClick = onAgainHomework,
                        text = ScheduleThemeRes.strings.againHomeworkTitle,
                    )
                }
                HomeworkStatus.WAIT -> {
                    ClassBottomSheetActionView(
                        modifier = Modifier.weight(1f),
                        text = ScheduleThemeRes.strings.homeworkInProgressTitle,
                        color = StudyAssistantRes.colors.accents.orange,
                    )
                    ClassBottomSheetActionButton(
                        modifier = Modifier.weight(1f),
                        onClick = onCompleteHomework,
                        text = ScheduleThemeRes.strings.completeHomeworkTitle,
                    )
                }
                HomeworkStatus.IN_FUTURE -> {
                    ClassBottomSheetActionView(
                        modifier = Modifier.weight(1f),
                        text = ScheduleThemeRes.strings.homeworkIsSetTitle,
                        color = StudyAssistantRes.colors.accents.yellow,
                    )
                    ClassBottomSheetActionButton(
                        onClick = onCompleteHomework,
                        text = ScheduleThemeRes.strings.completeHomeworkTitle,
                    )
                }
                HomeworkStatus.NOT_COMPLETE -> {
                    ClassBottomSheetActionView(
                        modifier = Modifier.weight(1f),
                        text = ScheduleThemeRes.strings.homeworkIsNotCompleteTitle,
                        color = StudyAssistantRes.colors.accents.red,
                    )
                    ClassBottomSheetActionButton(
                        onClick = onCompleteHomework,
                        text = ScheduleThemeRes.strings.completeHomeworkTitle,
                    )
                }
                HomeworkStatus.SKIPPED -> {
                    ClassBottomSheetActionView(
                        modifier = Modifier.weight(1f),
                        text = ScheduleThemeRes.strings.homeworkIsSkippedTitle,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                    )
                    ClassBottomSheetActionButton(
                        onClick = onCompleteHomework,
                        text = ScheduleThemeRes.strings.completeHomeworkTitle,
                    )
                }
            }
        } else {
            ClassBottomSheetActionButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onAddHomework,
                text = ScheduleThemeRes.strings.addHomeworkTitle,
            )
        }
    }
}

@Composable
private fun ClassBottomSheetActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.contentColorFor(containerColor),
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 10.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        ),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            overflow = TextOverflow.Ellipsis,
            maxLines = 1,
        )
    }
}

@Composable
private fun ClassBottomSheetActionView(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    contentColor: Color = StudyAssistantRes.colors.accents.contentColorFor(color),
) {
    Surface(
        modifier = modifier.defaultMinSize(minHeight = 40.dp),
        shape = MaterialTheme.shapes.medium,
        color = color,
        contentColor = contentColor,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                overflow = TextOverflow.Ellipsis,
                maxLines = 1,
            )
        }
    }
}