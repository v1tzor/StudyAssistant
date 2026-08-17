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

package ru.aleshin.studyassistant.analytics.impl.domain.common

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.analytics.impl.domain.calculators.AnalyticsRangeCalculator
import ru.aleshin.studyassistant.analytics.impl.domain.entities.AnalyticsRangeSelection
import ru.aleshin.studyassistant.core.domain.entities.settings.AnalyticsPeriod
import ru.aleshin.studyassistant.core.domain.entities.settings.AnalyticsSettings

/**
 * @author Stanislav Aleshin on 17.08.2026.
 */
internal fun AnalyticsSettings.toRangeSelection(
    calculator: AnalyticsRangeCalculator,
    currentTime: Instant,
): AnalyticsRangeSelection {
    val from = customFrom
    val to = customTo
    return if (period == AnalyticsPeriod.CUSTOM && from != null && to != null && from <= to) {
        calculator.selectCustom(
            from = Instant.fromEpochMilliseconds(from),
            to = Instant.fromEpochMilliseconds(to),
            currentTime = currentTime,
        )
    } else {
        val resolvedPeriod = period.takeUnless { it == AnalyticsPeriod.CUSTOM } ?: AnalyticsPeriod.MONTH
        calculator.selectPeriod(
            period = resolvedPeriod,
            anchor = currentTime,
            currentTime = currentTime,
        )
    }
}
