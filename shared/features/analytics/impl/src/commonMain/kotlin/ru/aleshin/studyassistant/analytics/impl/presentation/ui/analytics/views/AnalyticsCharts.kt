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

package ru.aleshin.studyassistant.analytics.impl.presentation.ui.analytics.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.common.ChartContext
import com.himanshoe.charty.common.ChartScaffold
import com.himanshoe.charty.common.axis.AxisConfig
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.pie.PieChart
import com.himanshoe.charty.pie.config.LabelConfig
import com.himanshoe.charty.pie.config.PieChartConfig
import com.himanshoe.charty.pie.config.PieChartStyle
import com.himanshoe.charty.pie.data.PieData
import kotlinx.datetime.format.DateTimeComponents
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsLoadBucketUi
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsSummaryUi
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsTaskBucketUi
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_chart_empty
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.ui.views.shortDayMonthFormat
import kotlin.math.ceil
import kotlin.math.roundToInt

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsWorkloadChart(
    buckets: List<AnalyticsLoadBucketUi>,
    description: (AnalyticsLoadBucketUi) -> String,
    modifier: Modifier = Modifier,
) {
    if (buckets.isEmpty() || buckets.none { it.workload > 0f }) {
        AnalyticsChartPlaceholder(modifier)
        return
    }

    val labels = remember(buckets) {
        buildAnalyticsChartLabels(
            buckets.map {
                it.from.formatByTimeZone(
                    DateTimeComponents.Formats.shortDayMonthFormat(),
                )
            },
        )
    }
    val values = remember(buckets) {
        buckets.map { it.workload.takeIf(Float::isFinite) ?: 0f }
    }
    val peakIndex = remember(values) {
        values.indices.maxByOrNull { values[it] }
    }
    val averageWorkload = remember(values) {
        values.average().toFloat()
    }

    AnalyticsAdaptiveBarChart(
        values = values,
        labels = labels,
        highlightIndex = peakIndex,
        referenceValue = averageWorkload,
        description = buckets.joinToString(
            separator = ". ",
            transform = description,
        ),
        modifier = modifier,
    )
}

@Composable
internal fun AnalyticsTaskDonutChart(
    summary: AnalyticsSummaryUi,
    modifier: Modifier = Modifier,
) {
    val slices = listOf(
        Triple(
            "on-time",
            summary.completedOnTime.toFloat(),
            MaterialTheme.colorScheme.primary,
        ),
        Triple(
            "late",
            summary.completedLate.toFloat(),
            MaterialTheme.colorScheme.tertiary,
        ),
        Triple(
            "overdue",
            summary.overdue.toFloat(),
            MaterialTheme.colorScheme.error,
        ),
        Triple(
            "upcoming",
            summary.upcoming.toFloat(),
            MaterialTheme.colorScheme.outline,
        ),
    ).filter { it.second > 0f }.map { (label, value, color) ->
        PieData(
            label = label,
            value = value,
            color = color,
        )
    }

    if (slices.isEmpty()) {
        AnalyticsChartPlaceholder(modifier)
        return
    }

    PieChart(
        data = { slices },
        modifier = modifier.height(112.dp),
        config = PieChartConfig(
            style = PieChartStyle.DONUT,
            donutHoleRatio = 0.66f,
            labelConfig = LabelConfig(
                shouldShowLabels = false,
            ),
            shouldShowCenterText = false,
        ),
        onSliceClick = { _, _ -> },
    )
}

@Composable
internal fun AnalyticsCompletionChart(
    buckets: List<AnalyticsTaskBucketUi>,
    description: (AnalyticsTaskBucketUi) -> String,
    modifier: Modifier = Modifier,
) {
    if (
        buckets.isEmpty() ||
        buckets.none { it.completedHomeworks > 0 || it.completedTodos > 0 }
    ) {
        AnalyticsChartPlaceholder(modifier)
        return
    }

    val labels = remember(buckets) {
        buildAnalyticsChartLabels(
            buckets.map {
                it.from.formatByTimeZone(
                    DateTimeComponents.Formats.shortDayMonthFormat(),
                )
            },
        )
    }
    val homeworkValues = remember(buckets) {
        buckets.map { it.completedHomeworks.toFloat() }
    }
    val todoValues = remember(buckets) {
        buckets.map { it.completedTodos.toFloat() }
    }
    val maxValue = remember(homeworkValues, todoValues) {
        maxOf(
            homeworkValues.maxOrNull() ?: 0f,
            todoValues.maxOrNull() ?: 0f,
        )
    }
    val axisScale = remember(maxValue) {
        calculateAdaptiveAxisScale(maxValue)
    }
    val primary = MaterialTheme.colorScheme.primary
    val tertiary = MaterialTheme.colorScheme.tertiary

    ChartScaffold(
        modifier = modifier
            .fillMaxWidth()
            .height(148.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = buckets.joinToString(
                    separator = ". ",
                    transform = description,
                )
            },
        xLabels = labels,
        yAxisConfig = AxisConfig(
            minValue = 0f,
            maxValue = axisScale.max,
            steps = axisScale.steps,
            drawAxisAtZero = false,
        ),
        config = analyticsScaffoldConfig(),
    ) { chartContext ->
        drawAnalyticsLineSeries(
            values = homeworkValues,
            chartContext = chartContext,
            color = primary,
        )
        drawAnalyticsLineSeries(
            values = todoValues,
            chartContext = chartContext,
            color = tertiary,
        )
    }
}

@Composable
internal fun AnalyticsRegularityChart(
    values: List<Pair<String, Float>>,
    description: (Pair<String, Float>) -> String,
    modifier: Modifier = Modifier,
) {
    if (values.isEmpty() || values.none { it.second > 0f }) {
        AnalyticsChartPlaceholder(modifier)
        return
    }

    val highlightIndex = remember(values) {
        values.indices.maxByOrNull { values[it].second }
    }

    AnalyticsAdaptiveBarChart(
        values = values.map { it.second },
        labels = values.map { it.first },
        highlightIndex = highlightIndex,
        description = values.joinToString(
            separator = ". ",
            transform = description,
        ),
        modifier = modifier,
        barWidthFraction = 0.54f,
    )
}

@Composable
private fun AnalyticsAdaptiveBarChart(
    values: List<Float>,
    labels: List<String>,
    description: String,
    modifier: Modifier = Modifier,
    highlightIndex: Int? = null,
    referenceValue: Float? = null,
    barWidthFraction: Float = when {
        values.size <= 7 -> 0.58f
        values.size <= 14 -> 0.52f
        else -> 0.46f
    },
) {
    val safeValues = remember(values) {
        values.map { it.takeIf(Float::isFinite) ?: 0f }
    }

    if (safeValues.isEmpty() || safeValues.none { it > 0f }) {
        AnalyticsChartPlaceholder(modifier)
        return
    }

    val peak = remember(safeValues) {
        safeValues.maxOrNull() ?: 0f
    }
    val axisScale = remember(peak) {
        calculateAdaptiveAxisScale(peak)
    }
    val primary = MaterialTheme.colorScheme.primary
    val highlight = MaterialTheme.colorScheme.tertiary
    val referenceColor = MaterialTheme.colorScheme.onSurfaceVariant

    ChartScaffold(
        modifier = modifier
            .fillMaxWidth()
            .height(164.dp)
            .semantics(mergeDescendants = true) {
                contentDescription = description
            },
        xLabels = labels,
        yAxisConfig = AxisConfig(
            minValue = 0f,
            maxValue = axisScale.max,
            steps = axisScale.steps,
            drawAxisAtZero = false,
        ),
        config = analyticsScaffoldConfig(),
    ) { chartContext ->
        referenceValue
            ?.takeIf {
                it > 0f && it < axisScale.max
            }
            ?.let { value ->
                val y = chartContext.convertValueToYPosition(value)

                drawLine(
                    color = referenceColor.copy(alpha = 0.55f),
                    start = Offset(chartContext.left, y),
                    end = Offset(chartContext.right, y),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(
                            6.dp.toPx(),
                            5.dp.toPx(),
                        ),
                    ),
                )
            }

        val barWidth = chartContext.calculateBarWidth(
            totalBars = safeValues.size,
            widthFraction = barWidthFraction,
        )

        safeValues.forEachIndexed { index, value ->
            if (value <= 0f) return@forEachIndexed

            val left = chartContext.calculateBarLeftPosition(
                index = index,
                totalBars = safeValues.size,
                barWidthFraction = barWidthFraction,
            )
            val top = chartContext.convertValueToYPosition(value)
            val height = (chartContext.bottom - top).coerceAtLeast(1.dp.toPx())

            drawRoundRect(
                color = if (index == highlightIndex) {
                    highlight
                } else {
                    primary.copy(alpha = 0.76f)
                },
                topLeft = Offset(
                    x = left,
                    y = chartContext.bottom - height,
                ),
                size = Size(
                    width = barWidth,
                    height = height,
                ),
                cornerRadius = CornerRadius(
                    x = 4.dp.toPx(),
                    y = 4.dp.toPx(),
                ),
            )
        }
    }
}

private fun DrawScope.drawAnalyticsLineSeries(
    values: List<Float>,
    chartContext: ChartContext,
    color: Color,
) {
    if (values.isEmpty()) return

    val path = Path()

    values.forEachIndexed { index, value ->
        val point = Offset(
            x = chartContext.calculateCenteredXPosition(
                index = index,
                totalItems = values.size,
            ),
            y = chartContext.convertValueToYPosition(value),
        )

        if (index == 0) {
            path.moveTo(
                x = point.x,
                y = point.y,
            )
        } else {
            path.lineTo(
                x = point.x,
                y = point.y,
            )
        }
    }

    drawPath(
        path = path,
        color = color,
        style = Stroke(
            width = 2.dp.toPx(),
        ),
    )

    values.forEachIndexed { index, value ->
        if (value <= 0f) return@forEachIndexed

        drawCircle(
            color = color,
            radius = 3.dp.toPx(),
            center = Offset(
                x = chartContext.calculateCenteredXPosition(
                    index = index,
                    totalItems = values.size,
                ),
                y = chartContext.convertValueToYPosition(value),
            ),
        )
    }
}

@Composable
private fun AnalyticsChartPlaceholder(
    modifier: Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(92.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerHighest,
    ) {
        Box(
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(Res.string.analytics_chart_empty),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun analyticsScaffoldConfig(): ChartScaffoldConfig {
    return ChartScaffoldConfig(
        showAxis = true,
        showGrid = true,
        showLabels = true,
        axisColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(
            alpha = 0.65f,
        ),
        gridColor = MaterialTheme.colorScheme.outlineVariant.copy(
            alpha = 0.42f,
        ),
        axisThickness = 1f,
        gridThickness = 0.6f,
        labelTextStyle = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

private fun calculateAdaptiveAxisScale(
    peak: Float,
): AnalyticsAxisScale {
    if (!peak.isFinite() || peak <= 0f) {
        return AnalyticsAxisScale(
            max = 1f,
            steps = 4,
        )
    }

    val paddedPeak = peak * 1.12f

    val step = when {
        paddedPeak <= 0.5f -> 0.1f
        paddedPeak <= 1.25f -> 0.25f
        paddedPeak <= 2.5f -> 0.5f
        paddedPeak <= 5f -> 1f
        paddedPeak <= 10f -> 2f
        paddedPeak <= 20f -> 5f
        else -> 10f
    }

    val max = ceil(paddedPeak / step) * step

    return AnalyticsAxisScale(
        max = max,
        steps = (max / step)
            .roundToInt()
            .coerceIn(2, 5),
    )
}

private fun buildAnalyticsChartLabels(
    labels: List<String>,
): List<String> {
    val stride = if (labels.size <= MAX_VISIBLE_CHART_LABELS) {
        1
    } else {
        ceil(
            (labels.size - 1).toDouble() /
                    (MAX_VISIBLE_CHART_LABELS - 1),
        ).toInt()
    }

    return labels.mapIndexed { index, label ->
        val identity = WORD_JOINER.repeat(index + 1)

        if (
            index == 0 ||
            index == labels.lastIndex ||
            index % stride == 0
        ) {
            label + identity
        } else {
            identity
        }
    }
}

private data class AnalyticsAxisScale(
    val max: Float,
    val steps: Int,
)

private const val MAX_VISIBLE_CHART_LABELS = 7
private const val WORD_JOINER = "\u2060"
