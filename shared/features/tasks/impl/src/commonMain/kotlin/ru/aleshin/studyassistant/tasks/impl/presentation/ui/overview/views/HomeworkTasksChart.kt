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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.views

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.bar.StackedVerticalBarPlot
import io.github.koalaplot.core.bar.verticalSolidBar
import io.github.koalaplot.core.gestures.GestureConfig
import io.github.koalaplot.core.legend.FlowLegend
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.CategoryAxisOffset
import io.github.koalaplot.core.xygraph.FloatLinearAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
import io.github.koalaplot.core.xygraph.rememberAxisContent
import kotlinx.datetime.Instant
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.equalsDay
import ru.aleshin.studyassistant.core.ui.theme.StudyAssistantRes
import ru.aleshin.studyassistant.core.ui.theme.material.full
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkScopeUi
import ru.aleshin.studyassistant.tasks.impl.resources.Res
import ru.aleshin.studyassistant.tasks.impl.resources.practical_tasks_bar_name
import ru.aleshin.studyassistant.tasks.impl.resources.presentations_tasks_bar_name
import ru.aleshin.studyassistant.tasks.impl.resources.theoretical_tasks_bar_name

/**
 * @author Stanislav Aleshin on 30.06.2024.
 */
@Composable
@OptIn(ExperimentalKoalaPlotApi::class)
internal fun HomeworkTasksChart(
    modifier: Modifier = Modifier,
    isLoading: Boolean,
    currentDate: Instant,
    homeworkScope: HomeworkScopeUi?,
) {
    val taskTypes = listOf(
        stringResource(Res.string.theoretical_tasks_bar_name),
        stringResource(Res.string.practical_tasks_bar_name),
        stringResource(Res.string.presentations_tasks_bar_name),
    )
    val taskTypeColors = listOf(
        StudyAssistantRes.colors.accents.green,
        StudyAssistantRes.colors.accents.orange,
        StudyAssistantRes.colors.accents.red,
    )
    val dateList = remember(homeworkScope) {
        homeworkScope?.theoreticalTasks?.map { it.key } ?: listOf(null)
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
            xAxisModel = remember(homeworkScope) {
                CategoryAxisModel(
                    categories = dateList,
                    categoryAxisOffset = CategoryAxisOffset.None,
                )
            },
            yAxisModel = remember(homeworkScope) {
                FloatLinearAxisModel(
                    range = range,
                    minimumMajorTickIncrement = if (range.endInclusive - range.start > 2f) {
                        2f
                    } else {
                        (range.endInclusive - range.start) * 0.1f
                    },
                )
            },
            modifier = Modifier.weight(1f),
            xAxisContent = rememberAxisContent(
                labels = { date ->
                    Text(
                        text = date?.dateTime()?.dayOfMonth?.toString() ?: "",
                        color = if (date?.equalsDay(currentDate) == true) {
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
            ),
            yAxisContent = rememberAxisContent(
                labels = { value ->
                    Text(
                        text = value.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                },
            ),
            gestureConfig = GestureConfig(
                panXEnabled = true,
                panYEnabled = true,
                zoomXEnabled = true,
                zoomYEnabled = true,
            ),
        ) {
            if (!isLoading && homeworkScope != null) {
                StackedVerticalBarPlot(barWidth = 0.9f) {
                    taskTypes.forEachIndexed { typeIndex, _ ->
                        series(defaultBar = verticalSolidBar(taskTypeColors[typeIndex])) {
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
