/*
 * Copyright 2023 Stanislav Aleshin
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
package functional

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import extensions.dateTime
import extensions.epochTimeDuration
import extensions.shiftDay
import extensions.startThisDay
import kotlinx.datetime.DateTimePeriod
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.periodUntil
import platform.InstantParceler

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
@Parcelize
data class TimeRange(
    @TypeParceler<Instant, InstantParceler> val from: Instant,
    @TypeParceler<Instant, InstantParceler> val to: Instant
) : Parcelable {

    fun containsDate(instant: Instant) = instant in from..to

    fun containsTime(instant: Instant): Boolean {
        val targetTime = instant.dateTime().time
        val fromTime = from.dateTime().time
        val toTime = to.dateTime().time
        return targetTime in fromTime..toTime
    }

    fun timeEquals(other: TimeRange?): Boolean {
        val fromTime = from.dateTime().time
        val endTime = from.dateTime().time
        val otherFromTime = other?.from?.dateTime()?.time
        val otherEndTime = other?.from?.dateTime()?.time

        return fromTime.epochTimeDuration() == otherFromTime?.epochTimeDuration() &&
            endTime.epochTimeDuration() == otherEndTime?.epochTimeDuration()
    }

    fun periodDuration(timeZone: TimeZone = TimeZone.currentSystemDefault()): DateTimePeriod {
        return from.periodUntil(to, timeZone)
    }

    fun periodDates(timeZone: TimeZone = TimeZone.currentSystemDefault()): List<Instant> {
        val days = from.daysUntil(to, timeZone)
        val dateList = mutableListOf<Instant>().apply {
            for (shiftValue in 0..days) add(from.startThisDay().shiftDay(shiftValue, timeZone))
        }
        return dateList.toList()
    }
}