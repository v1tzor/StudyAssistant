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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.views

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.line.AreaBaseline
import io.github.koalaplot.core.line.AreaPlot
import io.github.koalaplot.core.style.AreaStyle
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.DefaultPoint
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.painterResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.common.extensions.toMinutesAndHoursString
import ru.aleshin.studyassistant.core.common.functional.Constants.Animations.FADE_SLOW
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.bottomSide
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.core.ui.views.PlaceholderBox
import ru.aleshin.studyassistant.core.ui.views.SmallInfoBadge
import ru.aleshin.studyassistant.schedule.impl.presentation.models.analysis.DailyAnalysisUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.theme.ScheduleThemeRes
import kotlin.math.roundToInt

/**
 * @author Stanislav Aleshin on 13.06.2024.
 */
@Composable
internal fun OverviewTopSheet(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    selectedDate: Instant?,
    weekAnalysis: List<DailyAnalysisUi>?,
    activeClass: ActiveClassUi?,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge.bottomSide,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val currentAnalysis = weekAnalysis?.find { it.date.equalsDay(selectedDate) }

            Column(
                modifier = Modifier.weight(0.6f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OverviewTopSheetChart(
                    selectedDate = selectedDate,
                    weekAnalysis = weekAnalysis
                )
                OverviewTopSheetClassTime(
                    isLoading = isLoading,
                    activeClass = activeClass,
                    homeworksProgressList = currentAnalysis?.numberOfHomeworks ?: emptyList(),
                    tasksProgressList = currentAnalysis?.numberOfTasks ?: emptyList(),
                )
            }
            OverviewTopSheetAnalysis(
                isLoading = isLoading,
                modifier = Modifier.weight(0.4f),
                analysis = currentAnalysis,
            )
        }
    }
}

@Composable
private fun OverviewTopSheetChart(
    modifier: Modifier = Modifier,
    selectedDate: Instant?,
    weekAnalysis: List<DailyAnalysisUi>?,
) {
    val xAxisDate = remember(weekAnalysis) {
        weekAnalysis?.map { it.date } ?: listOf(null)
    }
    val yAxisData = remember(weekAnalysis) {
        weekAnalysis?.map { it.generalAssessment } ?: listOf(0f)
    }
    val axisPoints = remember(weekAnalysis) {
        buildList {
            for (i in 0..(weekAnalysis?.lastIndex ?: 0)) {
                add(DefaultPoint(xAxisDate[i], yAxisData[i]))
            }
        }
    }
    XYGraph(
        xAxisModel = remember(weekAnalysis) {
            CategoryAxisModel(
                categories = xAxisDate,
                firstCategoryIsZero = true,
            )
        },
        yAxisModel = remember(weekAnalysis) {
            FloatLinearAxisModel(
                range = 0f..10f,
                minorTickCount = 1,
                minimumMajorTickSpacing = 16.dp,
            )
        },
        modifier = modifier.fillMaxWidth().height(130.dp),
        xAxisLabels = { date ->
            Text(
                text = date?.dateTime()?.dayOfMonth?.toString() ?: "",
                color = if (date?.equalsDay(selectedDate) == true) {
                    StudyAssistantRes.colors.accents.red
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                softWrap = false,
                overflow = TextOverflow.Visible,
                modifier = Modifier.padding(top = 2.dp)
            )
        },
        yAxisLabels = {
            Text(
                text = it.toString(),
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp)
            )
        },
        xAxisTitle = {},
        yAxisTitle = {},
    ) {
        AreaPlot(
            data = axisPoints,
            lineStyle = LineStyle(
                brush = SolidColor(MaterialTheme.colorScheme.primary),
                strokeWidth = 2.dp
            ),
            areaStyle = AreaStyle(
                brush = SolidColor(MaterialTheme.colorScheme.primaryContainer),
                alpha = 0.5f,
            ),
            areaBaseline = AreaBaseline.ConstantLine(0f),
        )
    }
}

@Composable
private fun OverviewTopSheetClassTime(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    activeClass: ActiveClassUi?,
    homeworksProgressList: List<Boolean>,
    tasksProgressList: List<Boolean>,
) {
    Crossfade(
        modifier = modifier,
        targetState = isLoading,
        animationSpec = tween(FADE_SLOW),
    ) { loading ->
        if (!loading) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                val homeworksProgress by derivedStateOf {
                    homeworksProgressList.count { it } / homeworksProgressList.size.toFloat()
                }
                val tasksProgress by derivedStateOf {
                    tasksProgressList.count { it } / tasksProgressList.size.toFloat()
                }
                val progress = when {
                    activeClass?.progress != null -> activeClass.progress
                    homeworksProgressList.isNotEmpty() -> homeworksProgress
                    tasksProgressList.isNotEmpty() -> tasksProgress
                    else -> 0f
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = when {
                            activeClass?.isStarted == true -> ScheduleThemeRes.strings.untilEndClassTitle
                            activeClass?.isStarted == false -> ScheduleThemeRes.strings.untilStartClassTitle
                            homeworksProgressList.isNotEmpty() -> ScheduleThemeRes.strings.tasksProgressTitle
                            tasksProgressList.isNotEmpty() -> ScheduleThemeRes.strings.tasksProgressTitle
                            else -> ScheduleThemeRes.strings.completeScheduleTitle
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = when {
                            activeClass != null -> activeClass.duration.toMinutesAndHoursString()
                            homeworksProgressList.isNotEmpty() -> buildString {
                                append((homeworksProgress * 100).toInt(), "%")
                            }
                            tasksProgressList.isNotEmpty() -> buildString {
                                append((tasksProgress * 100).toInt(), "%")
                            }
                            else -> ""
                        },
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
                LinearProgressIndicator(
                    modifier = Modifier.height(10.dp).fillMaxWidth(1f).clip(MaterialTheme.shapes.full),
                    progress = { progress },
                    trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    strokeCap = StrokeCap.Square,
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PlaceholderBox(
                    modifier = Modifier.height(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
                PlaceholderBox(
                    modifier = Modifier.height(10.dp).fillMaxWidth(),
                    shape = MaterialTheme.shapes.full,
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                )
            }
        }
    }
}

@Composable
private fun OverviewTopSheetAnalysis(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    analysis: DailyAnalysisUi?,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Column(
            modifier = Modifier.padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = ScheduleThemeRes.strings.analysisDayTitle,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                )
                Crossfade(
                    targetState = isLoading,
                    animationSpec = tween(FADE_SLOW),
                ) { loading ->
                    if (!loading) {
                        val assessment = ((analysis?.generalAssessment ?: 0f) * 10f).roundToInt() / 10f
                        SmallInfoBadge(
                            containerColor = when (assessment) {
                                in 0f..2f -> StudyAssistantRes.colors.accents.greenContainer
                                in 3f..5f -> StudyAssistantRes.colors.accents.yellowContainer
                                in 6f..8f -> StudyAssistantRes.colors.accents.orangeContainer
                                else -> StudyAssistantRes.colors.accents.redContainer
                            },
                            contentColor = when (assessment) {
                                in 0f..2f -> StudyAssistantRes.colors.accents.green
                                in 3f..5f -> StudyAssistantRes.colors.accents.yellow
                                in 6f..8f -> StudyAssistantRes.colors.accents.orange
                                else -> StudyAssistantRes.colors.accents.red
                            },
                        ) {
                            Text(
                                text = assessment.toString(),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    } else {
                        PlaceholderBox(
                            modifier = Modifier.size(21.dp, 16.dp),
                            shape = MaterialTheme.shapes.extraSmall,
                            color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        )
                    }
                }
            }
            OverviewTopSheetAnalysisItem(
                isLoading = isLoading,
                icon = painterResource(StudyAssistantRes.icons.homeworks),
                label = ScheduleThemeRes.strings.analysisHomeworksLabel,
                value = buildString {
                    append(analysis?.numberOfHomeworks?.count { it })
                    append("/")
                    append(analysis?.numberOfHomeworks?.size)
                },
                valueColor = if (analysis?.numberOfHomeworks?.count { it } == analysis?.numberOfHomeworks?.size) {
                    if (analysis?.numberOfHomeworks?.isEmpty() == true) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        StudyAssistantRes.colors.accents.green
                    }
                } else {
                    StudyAssistantRes.colors.accents.red
                },
            )
            OverviewTopSheetAnalysisItem(
                isLoading = isLoading,
                icon = painterResource(StudyAssistantRes.icons.testsOutline),
                label = ScheduleThemeRes.strings.analysisTestsLabel,
                value = analysis?.numberOfTests?.toString(),
                valueColor = if (analysis?.numberOfTests == 0) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    StudyAssistantRes.colors.accents.red
                },
            )
            OverviewTopSheetAnalysisItem(
                isLoading = isLoading,
                icon = painterResource(StudyAssistantRes.icons.classesList),
                label = ScheduleThemeRes.strings.analysisClassesLabel,
                value = analysis?.numberOfClasses.toString(),
                valueColor = MaterialTheme.colorScheme.onSurface,
            )
            OverviewTopSheetAnalysisItem(
                isLoading = isLoading,
                icon = painterResource(StudyAssistantRes.icons.movements),
                label = ScheduleThemeRes.strings.analysisMovementLabel,
                value = analysis?.numberOfMovements?.toString(),
                valueColor = if ((analysis?.numberOfTests ?: 0) <= 2) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    StudyAssistantRes.colors.accents.red
                },
            )
            OverviewTopSheetAnalysisItem(
                isLoading = isLoading,
                icon = painterResource(StudyAssistantRes.icons.tasksOutline),
                label = ScheduleThemeRes.strings.analysisTasksLabel,
                value = buildString {
                    append(analysis?.numberOfTasks?.count { it })
                    append("/")
                    append(analysis?.numberOfTasks?.size)
                },
                valueColor = if (analysis?.numberOfTasks?.count { it } == analysis?.numberOfTasks?.size) {
                    if (analysis?.numberOfTasks?.isEmpty() == true) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        StudyAssistantRes.colors.accents.green
                    }
                } else {
                    StudyAssistantRes.colors.accents.red
                },
            )
        }
    }
}

@Composable
private fun OverviewTopSheetAnalysisItem(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    icon: Painter,
    label: String,
    value: String?,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            modifier = Modifier.size(18.dp),
            painter = icon,
            contentDescription = null,
            tint = color,
        )
        Text(
            modifier = Modifier.weight(1f),
            text = label,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelMedium,
        )
        Crossfade(
            modifier = Modifier.animateContentSize(),
            targetState = isLoading,
            animationSpec = tween(FADE_SLOW),
        ) { loading ->
            if (!loading) {
                Text(
                    text = value ?: "-",
                    color = valueColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelLarge,
                )
            } else {
                PlaceholderBox(
                    modifier = Modifier.size(15.dp, 16.dp),
                    shape = MaterialTheme.shapes.extraSmall,
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                )
            }
        }
    }
}