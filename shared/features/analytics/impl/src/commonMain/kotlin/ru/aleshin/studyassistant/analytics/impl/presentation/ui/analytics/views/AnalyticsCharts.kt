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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.himanshoe.charty.bar.BarChart
import com.himanshoe.charty.bar.config.BarChartConfig
import com.himanshoe.charty.bar.data.BarData
import com.himanshoe.charty.color.ChartyColor
import com.himanshoe.charty.common.config.ChartScaffoldConfig
import com.himanshoe.charty.common.config.CornerRadius
import com.himanshoe.charty.common.tooltip.TooltipConfig
import com.himanshoe.charty.line.MultilineChart
import com.himanshoe.charty.line.config.LineChartConfig
import com.himanshoe.charty.line.data.LineGroup
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
    val primary = MaterialTheme.colorScheme.primary
    val labels = remember(buckets) {
        buildAnalyticsChartLabels(
            buckets.map {
                it.from.formatByTimeZone(DateTimeComponents.Formats.shortDayMonthFormat())
            },
        )
    }
    val data = remember(buckets, primary) {
        buckets.mapIndexed { index, bucket ->
            BarData(
                label = labels[index],
                value = bucket.workload,
                color = ChartyColor.Solid(primary),
            )
        }
    }
    val bucketsByLabel = remember(buckets, labels) {
        labels.mapIndexed { index, label -> label to buckets[index] }.toMap()
    }
    val tooltip = rememberAnalyticsTooltip()
    BarChart(
        data = { data },
        modifier = modifier.fillMaxWidth().height(220.dp).semantics(mergeDescendants = true) {
            contentDescription = buckets.joinToString(separator = ". ", transform = description)
        },
        color = ChartyColor.Solid(primary),
        barConfig = BarChartConfig(
            barWidthFraction = 0.56f,
            cornerRadius = CornerRadius.Medium,
            tooltipConfig = tooltip,
            tooltipFormatter = { point -> bucketsByLabel[point.label]?.let(description) ?: point.label },
        ),
        scaffoldConfig = analyticsScaffoldConfig(),
        onBarClick = {},
    )
}

@Composable
internal fun AnalyticsTaskDonutChart(
    summary: AnalyticsSummaryUi,
    modifier: Modifier = Modifier,
) {
    val slices = listOf(
        Triple("on-time", summary.completedOnTime.toFloat(), MaterialTheme.colorScheme.primary),
        Triple("late", summary.completedLate.toFloat(), MaterialTheme.colorScheme.tertiary),
        Triple("overdue", summary.overdue.toFloat(), MaterialTheme.colorScheme.error),
        Triple("upcoming", summary.upcoming.toFloat(), MaterialTheme.colorScheme.outline),
    ).filter { it.second > 0f }.map { (label, value, color) ->
        PieData(label, value, color)
    }
    if (slices.isEmpty()) {
        AnalyticsChartPlaceholder(modifier)
        return
    }
    PieChart(
        data = { slices },
        modifier = modifier.height(190.dp),
        config = PieChartConfig(
            style = PieChartStyle.DONUT,
            donutHoleRatio = 0.62f,
            labelConfig = LabelConfig(shouldShowLabels = false),
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
    if (buckets.isEmpty() || buckets.none { it.completedHomeworks > 0 || it.completedTodos > 0 }) {
        AnalyticsChartPlaceholder(modifier)
        return
    }
    val labels = remember(buckets) {
        buildAnalyticsChartLabels(
            buckets.map {
                it.from.formatByTimeZone(DateTimeComponents.Formats.shortDayMonthFormat())
            },
        )
    }
    val data = remember(buckets, labels) {
        buckets.mapIndexed { index, bucket ->
            LineGroup(
                label = labels[index],
                values = listOf(bucket.completedHomeworks.toFloat(), bucket.completedTodos.toFloat()),
            )
        }
    }
    val bucketsByLabel = remember(buckets, labels) {
        labels.mapIndexed { index, label -> label to buckets[index] }.toMap()
    }
    val colors = ChartyColor.Gradient(
        listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary),
    )
    MultilineChart(
        data = { data },
        modifier = modifier.fillMaxWidth().height(220.dp).semantics(mergeDescendants = true) {
            contentDescription = buckets.joinToString(separator = ". ", transform = description)
        },
        colors = colors,
        lineConfig = LineChartConfig(
            smoothCurve = true,
            showPoints = true,
            tooltipConfig = rememberAnalyticsTooltip(),
            tooltipFormatter = { point ->
                bucketsByLabel[point.label]?.let(description) ?: point.label
            },
        ),
        scaffoldConfig = analyticsScaffoldConfig(),
        onPointClick = {},
    )
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
    val primary = MaterialTheme.colorScheme.secondary
    val data = remember(values, primary) {
        values.map { BarData(it.first, it.second, ChartyColor.Solid(primary)) }
    }
    BarChart(
        data = { data },
        modifier = modifier.fillMaxWidth().height(200.dp).semantics(mergeDescendants = true) {
            contentDescription = values.joinToString(separator = ". ", transform = description)
        },
        color = ChartyColor.Solid(primary),
        barConfig = BarChartConfig(
            barWidthFraction = 0.56f,
            cornerRadius = CornerRadius.Medium,
            tooltipConfig = rememberAnalyticsTooltip(),
        ),
        scaffoldConfig = analyticsScaffoldConfig(),
        onBarClick = {},
    )
}

@Composable
private fun AnalyticsChartPlaceholder(modifier: Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHighest,
                shape = RoundedCornerShape(12.dp),
            ),
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        Text(
            text = stringResource(Res.string.analytics_chart_empty),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun analyticsScaffoldConfig(): ChartScaffoldConfig {
    return ChartScaffoldConfig(
        axisColor = MaterialTheme.colorScheme.onSurfaceVariant,
        gridColor = MaterialTheme.colorScheme.outlineVariant,
        labelTextStyle = MaterialTheme.typography.labelSmall.copy(
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
    )
}

@Composable
private fun rememberAnalyticsTooltip(): TooltipConfig {
    val shape = MaterialTheme.shapes.small
    val background = MaterialTheme.colorScheme.inverseSurface
    val style = MaterialTheme.typography.labelSmall.copy(
        color = MaterialTheme.colorScheme.inverseOnSurface,
    )
    return remember(shape, background, style) {
        TooltipConfig(shape = shape, backgroundColor = background, textStyle = style)
    }
}

private fun buildAnalyticsChartLabels(labels: List<String>): List<String> {
    val stride = if (labels.size <= MAX_VISIBLE_CHART_LABELS) {
        1
    } else {
        ceil((labels.size - 1).toDouble() / (MAX_VISIBLE_CHART_LABELS - 1)).toInt()
    }
    return labels.mapIndexed { index, label ->
        val identity = WORD_JOINER.repeat(index + 1)
        if (index == 0 || index == labels.lastIndex || index % stride == 0) {
            label + identity
        } else {
            identity
        }
    }
}

private const val MAX_VISIBLE_CHART_LABELS = 7
private const val WORD_JOINER = "\u2060"
