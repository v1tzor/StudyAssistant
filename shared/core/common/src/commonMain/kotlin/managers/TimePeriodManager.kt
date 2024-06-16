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
 * imitations under the License.
 */

package managers

import extensions.endThisDay
import extensions.shiftDay
import functional.TimePeriod
import functional.TimeRange

/**
 * @author Stanislav Aleshin on 03.09.2023.
 */
interface TimePeriodManager {

    fun splitByTimeRanges(period: TimePeriod): List<TimeRange>
    
    class Base constructor(
        private val dateManager: DateManager,
    ) : TimePeriodManager {

        override fun splitByTimeRanges(period: TimePeriod): List<TimeRange> {
            val timeRanges = mutableListOf<TimeRange>()
            val currentDate = dateManager.fetchEndCurrentInstant()
            val daysInChildPeriod = period.quantityDaysInChildPeriod()
            
            repeat(period.quantityOfChildPeriods()) { childPeriod ->
                val actualStartDate = currentDate.shiftDay(-period.toDays() + 1)
                val start = actualStartDate.shiftDay(daysInChildPeriod * childPeriod)
                val end = actualStartDate.shiftDay(daysInChildPeriod * (childPeriod + 1))
                timeRanges.add(TimeRange(start, end.endThisDay()))
            }
            
            return timeRanges
        }
    }
}
