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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.bar.StackedVerticalBarPlot
import io.github.koalaplot.core.bar.solidBar
import io.github.koalaplot.core.legend.FlowLegend
import io.github.koalaplot.core.style.KoalaPlotTheme
import io.github.koalaplot.core.style.LineStyle
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.util.VerticalRotation
import io.github.koalaplot.core.util.rotateVertically
import io.github.koalaplot.core.xygraph.AxisModel
import io.github.koalaplot.core.xygraph.AxisStyle
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.XYGraphScope
import io.github.koalaplot.core.xygraph.rememberAxisStyle
import io.github.koalaplot.core.xygraph.rememberFloatLinearAxisModel
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkScopeUi
import ru.aleshin.studyassistant.tasks.impl.presentation.theme.TasksThemeRes

/**
 * @author Stanislav Aleshin on 30.06.2024.
 */
@Composable
@OptIn(ExperimentalKoalaPlotApi::class)
internal fun HomeworkTasksChart(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    homeworkScope: HomeworkScopeUi?,
) {
    val taskTypes = listOf(
        TasksThemeRes.strings.theoreticalTasksBarName,
        TasksThemeRes.strings.practicalTasksBarName,
        TasksThemeRes.strings.presentationsTasksBarName,
    )
    val taskTypeColors = listOf(
        StudyAssistantRes.colors.accents.green,
        StudyAssistantRes.colors.accents.orange,
        StudyAssistantRes.colors.accents.red,
    )
    val dateList = remember(homeworkScope) {
        homeworkScope?.theoreticalTasks?.map { it.key.dateTime().dayOfMonth.toString() } ?: listOf("")
    }
    val dailyTasks = mutableMapOf<Instant, Float>()
    val numberOfTheoryTasks = remember(homeworkScope) {
        homeworkScope?.theoreticalTasks?.map {
            dailyTasks[it.key] = (dailyTasks[it.key] ?: 0f) + it.value
            return@map it.value.toFloat()
        }
    }
    val numberOfPracticeTasks = remember(homeworkScope) {
        homeworkScope?.practicalTasks?.map {
            dailyTasks[it.key] = (dailyTasks[it.key] ?: 0f) + it.value
            return@map it.value.toFloat()
        }
    }
    val numberOfPresentationTasks = remember(homeworkScope) {
        homeworkScope?.presentationTasks?.map {
            dailyTasks[it.key] = (dailyTasks[it.key] ?: 0f) + it.value
            return@map it.value.toFloat()
        }
    }

    val numberOfTasks = listOf(
        numberOfTheoryTasks ?: emptyList(),
        numberOfPracticeTasks ?: emptyList(),
        numberOfPresentationTasks ?: emptyList(),
    )

    val maxValue = dailyTasks.maxOfOrNull { it.value }
    val range = 0f..(maxValue?.takeIf { it > 0f } ?: 1f)

    Column(
        modifier = modifier.fillMaxWidth().height(180.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        XYGraph(
            xAxisModel = CategoryAxisModel(
                categories = dateList,
                firstCategoryIsZero = true,
                minimumMajorTickSpacing = 8.dp,
            ),
            yAxisModel = rememberFloatLinearAxisModel(
                range = range,
                minimumMajorTickIncrement = if (range.endInclusive - range.start > 2f) {
                    2f
                } else {
                    (range.endInclusive - range.start) * 0.1f
                },
            ),
            modifier = Modifier.weight(1f),
            xAxisLabels = {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurface,
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
            if (!isLoading && homeworkScope != null) {
                StackedVerticalBarPlot(barWidth = 0.9f) {
                    taskTypes.forEachIndexed { typeIndex, _ ->
                        series(defaultBar = solidBar(taskTypeColors[typeIndex])) {
                            dateList.forEachIndexed { dateIndex, date ->
                                item(date, numberOfTasks[typeIndex][dateIndex])
                            }
                        }
                    }
                }
            }
        }
        FlowLegend(
            itemCount = taskTypes.size,
            symbol = {
                Symbol(
                    shape = MaterialTheme.shapes.full,
                    fillBrush = SolidColor(taskTypeColors[it]),
                )
            },
            label = {
                Text(
                    text = taskTypes[it],
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
        )
    }
}

@Composable
public fun <X, Y> XYGraph(
    xAxisModel: AxisModel<X>,
    yAxisModel: AxisModel<Y>,
    modifier: Modifier = Modifier,
    xAxisStyle: AxisStyle = rememberAxisStyle(),
    xAxisLabels: (X) -> String = { it.toString() },
    xAxisTitle: String? = null,
    yAxisStyle: AxisStyle = rememberAxisStyle(),
    yAxisLabels: (Y) -> String = { it.toString() },
    yAxisTitle: String? = null,
    horizontalMajorGridLineStyle: LineStyle? = KoalaPlotTheme.axis.majorGridlineStyle,
    horizontalMinorGridLineStyle: LineStyle? = KoalaPlotTheme.axis.minorGridlineStyle,
    verticalMajorGridLineStyle: LineStyle? = KoalaPlotTheme.axis.majorGridlineStyle,
    verticalMinorGridLineStyle: LineStyle? = KoalaPlotTheme.axis.minorGridlineStyle,
    panZoomEnabled: Boolean = true,
    content: @Composable XYGraphScope<X, Y>.() -> Unit
) {
    XYGraph(
        xAxisModel = xAxisModel,
        yAxisModel = yAxisModel,
        modifier = modifier,
        xAxisStyle = rememberAxisStyle(),
        xAxisLabels = {
            Text(
                xAxisLabels(it),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        },
        xAxisTitle = {
            if (xAxisTitle != null) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        xAxisTitle,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        },
        yAxisStyle = rememberAxisStyle(),
        yAxisLabels = {
            Text(
                yAxisLabels(it),
                color = MaterialTheme.colorScheme.onBackground,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        },
        yAxisTitle = {
            if (yAxisTitle != null) {
                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Text(
                        yAxisTitle,
                        color = MaterialTheme.colorScheme.onBackground,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.rotateVertically(VerticalRotation.COUNTER_CLOCKWISE)
                            .padding(bottom = KoalaPlotTheme.sizes.gap),
                    )
                }
            }
        },
        horizontalMajorGridLineStyle = KoalaPlotTheme.axis.majorGridlineStyle,
        horizontalMinorGridLineStyle = KoalaPlotTheme.axis.minorGridlineStyle,
        verticalMajorGridLineStyle = KoalaPlotTheme.axis.majorGridlineStyle,
        verticalMinorGridLineStyle = KoalaPlotTheme.axis.minorGridlineStyle,
        panZoomEnabled = true,
        content = content,
    )
}
