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

import androidx.compose.runtime.Composable
import kotlinx.datetime.DayOfWeek
import org.jetbrains.compose.resources.stringResource
import ru.aleshin.studyassistant.analytics.impl.resources.Res
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_decimal_separator
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_hours_minutes
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_minutes
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_not_available
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_percent
import ru.aleshin.studyassistant.analytics.impl.resources.analytics_percent_symbol
import ru.aleshin.studyassistant.core.ui.views.dayOfWeekNames
import ru.aleshin.studyassistant.core.ui.views.dayOfWeekShortNames
import kotlin.math.roundToInt

/**
 * @author Stanislav Aleshin on 09.08.2026.
 */
@Composable
internal fun formatAnalyticsDuration(duration: Long): String {
    val minutes = duration.coerceAtLeast(0L) / MILLIS_IN_MINUTE
    val hours = minutes / MINUTES_IN_HOUR
    val remainingMinutes = minutes % MINUTES_IN_HOUR
    return if (hours > 0L) {
        stringResource(Res.string.analytics_hours_minutes, hours, remainingMinutes)
    } else {
        stringResource(Res.string.analytics_minutes, remainingMinutes)
    }
}

@Composable
internal fun formatAnalyticsRate(rate: Float?): String {
    return rate?.takeIf { it.isFinite() }?.let {
        formatAnalyticsPercentage((it * PERCENT_FACTOR).roundToInt())
    } ?: stringResource(Res.string.analytics_not_available)
}

@Composable
internal fun formatAnalyticsPercentage(value: Int): String {
    val percent = buildString {
        append(value)
        append(stringResource(Res.string.analytics_percent_symbol))
    }
    return stringResource(Res.string.analytics_percent, percent)
}

@Composable
internal fun formatAnalyticsWorkload(value: Float): String {
    val tenths = ((value.takeIf { it.isFinite() } ?: 0f) * WORKLOAD_SCALE)
        .roundToInt()
        .coerceAtLeast(0)
    return buildString {
        append(tenths / WORKLOAD_SCALE)
        append(stringResource(Res.string.analytics_decimal_separator))
        append(tenths % WORKLOAD_SCALE)
    }
}

@Composable
internal fun analyticsWeekdayTitle(dayOfWeek: DayOfWeek, short: Boolean = false): String {
    val names = if (short) dayOfWeekShortNames() else dayOfWeekNames()
    return names.names[dayOfWeek.ordinal]
}

private const val MILLIS_IN_MINUTE = 60_000L
private const val MINUTES_IN_HOUR = 60L
private const val PERCENT_FACTOR = 100f
private const val WORKLOAD_SCALE = 10
