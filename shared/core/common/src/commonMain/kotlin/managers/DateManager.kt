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
package managers

import extensions.endThisDay
import extensions.startThisDay
import extensions.toMinutes
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
interface DateManager {

    fun fetchCurrentDate(): Instant
    fun fetchBeginningCurrentDay(): Instant
    fun fetchEndCurrentDay(): Instant
    fun calculateLeftTime(endTime: Instant): Long
    fun calculateProgress(startTime: Instant, endTime: Instant): Float

    class Base : DateManager {

        override fun fetchCurrentDate() = Clock.System.now()

        override fun fetchBeginningCurrentDay(): Instant {
            return fetchCurrentDate().startThisDay()
        }

        override fun fetchEndCurrentDay(): Instant {
            return fetchCurrentDate().endThisDay()
        }

        override fun calculateLeftTime(endTime: Instant): Long {
            val currentDate = fetchCurrentDate()
            val duration = endTime.toEpochMilliseconds() - currentDate.toEpochMilliseconds()
            return duration
        }

        override fun calculateProgress(startTime: Instant, endTime: Instant): Float {
            val currentTime = fetchCurrentDate().toEpochMilliseconds()
            val pastTime = ((currentTime - startTime.toEpochMilliseconds()).toMinutes()).toFloat()
            val duration = ((endTime.toEpochMilliseconds() - startTime.toEpochMilliseconds()).toMinutes()).toFloat()
            val progress = pastTime / duration

            return if (progress < 0f) 0f else if (progress > 1f) 1f else progress
        }
    }
}
