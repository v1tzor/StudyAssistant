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
import extensions.epochDuration
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import platform.InstantParceler

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
@Parcelize
data class TimeRange(
    @TypeParceler<Instant, InstantParceler> val from: Instant,
    @TypeParceler<Instant, InstantParceler> val to: Instant
) : Parcelable {

    fun timeEquals(other: TimeRange?): Boolean {
        val fromTime = from.toLocalDateTime(TimeZone.UTC).time
        val endTime = from.toLocalDateTime(TimeZone.UTC).time
        val otherFromTime = other?.from?.toLocalDateTime(TimeZone.UTC)?.time
        val otherEndTime = other?.from?.toLocalDateTime(TimeZone.UTC)?.time

        return fromTime.epochDuration() == otherFromTime?.epochDuration() &&
            endTime.epochDuration() == otherEndTime?.epochDuration()
    }
}
