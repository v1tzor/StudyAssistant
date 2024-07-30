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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.github.koalaplot.core.Symbol
import io.github.koalaplot.core.bar.StackedVerticalBarPlot
import io.github.koalaplot.core.bar.solidBar
import io.github.koalaplot.core.legend.FlowLegend
import io.github.koalaplot.core.util.ExperimentalKoalaPlotApi
import io.github.koalaplot.core.xygraph.CategoryAxisModel
import io.github.koalaplot.core.xygraph.XYGraph
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

    Column(
        modifier = modifier.fillMaxWidth().height(180.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        XYGraph(
            xAxisModel = CategoryAxisModel(
                categories = dateList,
                firstCategoryIsZero = true,
            ),
            yAxisModel = rememberFloatLinearAxisModel(
                range = 0f..(maxValue?.takeIf { it > 0f } ?: 1f),
                minorTickCount = 0,
            ),
            modifier = Modifier.weight(1f),
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