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

package ru.aleshin.studyassistant.core.domain.entities.schedules

import androidx.compose.runtime.Immutable
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.extensions.dateOfWeekDay
import ru.aleshin.studyassistant.core.common.extensions.endThisDay
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.Constants.Date.MAX_DAYS_SHIFT
import ru.aleshin.studyassistant.core.common.functional.TimeRange

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
@Immutable
@Serializable
data class DateVersion(
    val from: Instant,
    val to: Instant,
) {

    fun duration() = to - from

    fun containsDate(instant: Instant) = instant in from..to

    fun makeDeprecated(currentDate: Instant): DateVersion {
        return copy(to = currentDate.dateOfWeekDay(DayOfWeek.MONDAY).shiftDay(-1).endThisDay())
    }

    companion object {
        fun createNewVersion(currentDate: Instant) = DateVersion(
            from = currentDate.dateOfWeekDay(DayOfWeek.MONDAY).startThisDay(),
            to = currentDate.shiftDay(MAX_DAYS_SHIFT),
        )
    }
}

fun TimeRange.toDateVersion() = DateVersion(from, to)

fun DateVersion.toTimeRange() = TimeRange(from, to)