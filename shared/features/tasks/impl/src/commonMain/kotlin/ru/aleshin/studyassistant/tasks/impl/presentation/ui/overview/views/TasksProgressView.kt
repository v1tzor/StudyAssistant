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

import androidx.annotation.FloatRange
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.extractAllItem
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.extensions.weekTimeRange
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 29.06.2024.
 */
@Composable
internal fun TasksProgressView(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    homeworks: Map<Instant, List<HomeworkDetailsUi>>,
    todos: List<TodoDetailsUi>,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = TasksThemeRes.strings.tasksProgressViewHeader,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleSmall,
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val tomorrowHomeworks by derivedStateOf {
                    homeworks.filter {
                        val timeRange = TimeRange(currentDate, currentDate.shiftDay(1))
                        return@filter timeRange.containsDate(it.key)
                    }
                }
                val weekHomeworks by derivedStateOf {
                    homeworks.filter {
                        val timeRange = currentDate.dateTime().weekTimeRange()
                        return@filter timeRange.containsDate(it.key)
                    }
                }
                val weekTodos by derivedStateOf {
                    todos.filter {
                        val timeRange = currentDate.dateTime().weekTimeRange()
                        val homeworkDate = it.deadline?.startThisDay()
                        return@filter if (homeworkDate != null) timeRange.containsDate(homeworkDate) else true
                    }
                }
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InfoProgressView(
                        isLoading = isLoading,
                        items = tomorrowHomeworks.values.toList().extractAllItem().map { it.completeDate != null },
                        progressIcon = painterResource(TasksThemeRes.icons.tomorrowTime),
                        infoLabel = {
                            Text(
                                text = TasksThemeRes.strings.tasksProgressTomorrowLabel,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        },
                        accentColor = StudyAssistantRes.colors.accents.orange,
                    )
                    InfoProgressView(
                        isLoading = isLoading,
                        items = weekHomeworks.values.toList().extractAllItem().map { it.completeDate != null },
                        progressIcon = painterResource(TasksThemeRes.icons.weekTime),
                        infoLabel = {
                            Text(
                                text = TasksThemeRes.strings.tasksProgressInTheWeekLabel,
                                overflow = TextOverflow.Ellipsis,
                                maxLines = 1,
                            )
                        },
                        accentColor = MaterialTheme.colorScheme.primary,
                    )
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = TasksThemeRes.strings.todosProgressTitle,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall,
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }
                InfoProgressView(
                    isLoading = isLoading,
                    items = weekTodos.map { it.completeDate != null },
                    progressIcon = painterResource(StudyAssistantRes.icons.tasksOutline),
                    infoLabel = {
                        Text(
                            text = TasksThemeRes.strings.todosProgressLabel,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1,
                        )
                    },
                    accentColor = StudyAssistantRes.colors.accents.yellow,
                )
            }
        }
    }
}

@Composable
private fun InfoProgressView(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    items: List<Boolean>,
    progressIcon: Painter?,
    infoLabel: @Composable () -> Unit,
    accentColor: Color,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val progress by derivedStateOf {
            items.count { it } / (items.size.takeIf { it != 0 }?.toFloat() ?: 1f)
        }
        InfoProgressViewIndicator(
            progress = if (isLoading) 0f else progress,
            icon = progressIcon,
            accentColor = accentColor,
        )
        Column {
            Crossfade(targetState = isLoading) { loading ->
                if (!loading) {
                    Text(
                        text = buildString {
                            append((progress * 100).toInt(), "%")
                            append(" ", "|", " ")
                            append(items.count { it }, "/", items.size)
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        overflow = TextOverflow.Ellipsis,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleSmall,
                    )
                } else {
                    PlaceholderBox(
                        modifier = Modifier.size(60.dp, 20.dp),
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    )
                }
            }
            Row(
                modifier = Modifier.animateContentSize(),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraSmall,
                    color = accentColor,
                    content = { Box(modifier = Modifier.size(12.dp)) }
                )
                CompositionLocalProvider(
                    LocalContentColor provides MaterialTheme.colorScheme.onSurface
                ) {
                    ProvideTextStyle(
                        value = MaterialTheme.typography.labelMedium,
                        content = infoLabel,
                    )
                }
            }
        }
    }
}

private const val START_ANGLE = 270f
private const val MAX_ANGLE = 360f

@Composable
private fun InfoProgressViewIndicator(
    modifier: Modifier = Modifier,
    icon: Painter?,
    @FloatRange(0.0, 1.0)
    progress: Float,
    accentColor: Color,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeWidth: Dp = 6.dp,
) {
    val animatedProgress by animateFloatAsState(targetValue = progress, animationSpec = tween())
    val stroke = with(LocalDensity.current) {
        Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
    }
    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier.fillMaxSize()) {
            drawCircularIndicator(0f, MAX_ANGLE, trackColor, stroke)
            drawCircularIndicator(START_ANGLE, animatedProgress * MAX_ANGLE, accentColor, stroke)
        }
        if (icon != null) {
            Icon(
                modifier = Modifier.size(18.dp),
                painter = icon,
                contentDescription = null,
                tint = accentColor,
            )
        }
    }
}

private fun DrawScope.drawCircularIndicator(
    startAngle: Float,
    sweep: Float,
    color: Color,
    stroke: Stroke
) {
    val diameterOffset = stroke.width / 2
    val arcDimen = size.width - 2 * diameterOffset
    drawArc(
        color = color,
        startAngle = startAngle,
        sweepAngle = sweep,
        useCenter = false,
        topLeft = Offset(diameterOffset, diameterOffset),
        size = Size(arcDimen, arcDimen),
        style = stroke
    )
}