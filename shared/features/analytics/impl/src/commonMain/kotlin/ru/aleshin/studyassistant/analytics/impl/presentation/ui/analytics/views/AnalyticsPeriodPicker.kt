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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.format.DateTimeComponents
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.presentation.models.AnalyticsRangeSelectionUi
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_month_april
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_month_august
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_month_december
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_month_february
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_month_january
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_month_july
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_month_june
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_month_march
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_month_may
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_month_november
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_month_october
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_month_september
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_next_period_desc
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_period_custom
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_period_month
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_period_week
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_period_year
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_previous_period_desc
import ru.aleshin.studyassistant.core.common.extensions.formatByTimeZone
import ru.aleshin.studyassistant.core.domain.entities.settings.AnalyticsPeriod
import ru.aleshin.studyassistant.core.ui.views.dayMonthYearFormat
import ru.aleshin.studyassistant.core.ui.views.shortDayMonthFormat

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun AnalyticsPeriodPicker(
    modifier: Modifier = Modifier,
    selection: AnalyticsRangeSelectionUi,
    onPeriodChange: (AnalyticsPeriod) -> Unit,
    onRangeClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
) {
    val localizedMonthNames = analyticsMonthNames()
    val rangeTitle = remember(selection.range, selection.period, localizedMonthNames) {
        when (selection.period) {
            AnalyticsPeriod.WEEK -> {
                val format = DateTimeComponents.Formats.shortDayMonthFormat()
                val from = selection.range.from.formatByTimeZone(format)
                val to = selection.range.to.formatByTimeZone(format)
                "$from – $to"
            }
            AnalyticsPeriod.MONTH -> selection.range.from.formatByTimeZone(
                DateTimeComponents.Format {
                    monthName(localizedMonthNames)
                    char(' ')
                    year()
                },
            )
            AnalyticsPeriod.YEAR -> selection.range.from.formatByTimeZone(
                DateTimeComponents.Format { year() },
            )
            AnalyticsPeriod.CUSTOM -> {
                val format = DateTimeComponents.Formats.dayMonthYearFormat()
                val from = selection.range.from.formatByTimeZone(format)
                val to = selection.range.to.formatByTimeZone(format)
                if (from == to) from else "$from – $to"
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxWidth().height(40.dp),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.background,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RangePeriodChip(
                selectedPeriod = selection.period,
                onSelectPeriod = onPeriodChange,
            )
            Spacer(modifier = Modifier.weight(1f))
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .widthIn(min = 48.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clickable(role = Role.Button, onClick = onRangeClick)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = rangeTitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            AnalyticsRangeArrow(
                isPrevious = true,
                contentDescription = stringResource(Res.string.analytics_previous_period_desc),
                onClick = onPreviousClick,
            )
            AnalyticsRangeArrow(
                isPrevious = false,
                contentDescription = stringResource(Res.string.analytics_next_period_desc),
                onClick = onNextClick,
            )
        }
    }
}

@Composable
private fun RangePeriodChip(
    modifier: Modifier = Modifier,
    selectedPeriod: AnalyticsPeriod,
    onSelectPeriod: (AnalyticsPeriod) -> Unit,
) {
    Box(modifier = modifier) {
        var isMenuExpanded by remember { mutableStateOf(false) }

        Row(
            modifier = Modifier
                .clip(MaterialTheme.shapes.large)
                .clickable(role = Role.Button) { isMenuExpanded = true }
                .height(40.dp)
                .padding(start = 12.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(18.dp),
                imageVector = Icons.Default.DateRange,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = periodTitle(selectedPeriod),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(4.dp))
            val dropdownIconRotationAnim = animateFloatAsState(
                targetValue = if (isMenuExpanded) 0f else 180f
            )
            Icon(
                modifier = Modifier
                    .size(18.dp)
                    .graphicsLayer {
                        rotationZ = dropdownIconRotationAnim.value
                    },
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        DropdownMenu(
            expanded = isMenuExpanded,
            onDismissRequest = { isMenuExpanded = false },
        ) {
            AnalyticsPeriod.entries.forEach { period ->
                DropdownMenuItem(
                    text = { Text(periodTitle(period)) },
                    onClick = {
                        isMenuExpanded = false
                        onSelectPeriod(period)
                    },
                )
            }
        }
    }
}

@Composable
private fun AnalyticsRangeArrow(
    isPrevious: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .width(40.dp)
            .height(40.dp)
            .semantics {
                this.contentDescription = contentDescription
                role = Role.Button
            }
            .clickable(onClick = onClick),
        contentAlignment = if (isPrevious) Alignment.CenterEnd else Alignment.CenterStart,
    ) {
        Box(
            modifier = Modifier.size(32.dp),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                imageVector = if (isPrevious) {
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft
                } else {
                    Icons.AutoMirrored.Filled.KeyboardArrowRight
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun periodTitle(period: AnalyticsPeriod): String = when (period) {
    AnalyticsPeriod.WEEK -> stringResource(Res.string.analytics_period_week)
    AnalyticsPeriod.MONTH -> stringResource(Res.string.analytics_period_month)
    AnalyticsPeriod.YEAR -> stringResource(Res.string.analytics_period_year)
    AnalyticsPeriod.CUSTOM -> stringResource(Res.string.analytics_period_custom)
}

@Composable
private fun analyticsMonthNames() = MonthNames(
    january = stringResource(Res.string.analytics_month_january),
    february = stringResource(Res.string.analytics_month_february),
    march = stringResource(Res.string.analytics_month_march),
    april = stringResource(Res.string.analytics_month_april),
    may = stringResource(Res.string.analytics_month_may),
    june = stringResource(Res.string.analytics_month_june),
    july = stringResource(Res.string.analytics_month_july),
    august = stringResource(Res.string.analytics_month_august),
    september = stringResource(Res.string.analytics_month_september),
    october = stringResource(Res.string.analytics_month_october),
    november = stringResource(Res.string.analytics_month_november),
    december = stringResource(Res.string.analytics_month_december),
)
