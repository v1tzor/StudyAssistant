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

package entities.schedules

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import extensions.dateOfWeekDay
import extensions.endThisDay
import extensions.shiftDay
import extensions.startThisDay
import functional.Constants.Date.MAX_DAYS_SHIFT
import kotlinx.datetime.DayOfWeek.MONDAY
import kotlinx.datetime.Instant
import platform.InstantParceler

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
@Parcelize
data class DateVersion(
    @TypeParceler<Instant, InstantParceler> val from: Instant,
    @TypeParceler<Instant, InstantParceler> val to: Instant,
) : Parcelable {
    fun makeDeprecated(currentDate: Instant): DateVersion {
        return copy(to = currentDate.dateOfWeekDay(MONDAY).shiftDay(-1).endThisDay())
    }

    companion object {
        fun createNewVersion(currentDate: Instant) = DateVersion(
            from = currentDate.dateOfWeekDay(MONDAY).startThisDay(),
            to = currentDate.shiftDay(MAX_DAYS_SHIFT),
        )
    }
}
