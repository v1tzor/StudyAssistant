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

package entities.common

import extensions.isoWeekNumber
import kotlinx.datetime.LocalDate

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
enum class NumberOfRepeatWeek {
    ONE, TWO, THREE
}

val NumberOfRepeatWeek.isoRepeatWeekNumber: Int get() = ordinal + 1

fun NumberOfRepeatWeek(isoRepeatWeekNumber: Int): NumberOfRepeatWeek {
    require(isoRepeatWeekNumber in 1..3)
    return NumberOfRepeatWeek.entries[isoRepeatWeekNumber - 1]
}

fun LocalDate.numberOfRepeatWeek(repeat: NumberOfRepeatWeek): NumberOfRepeatWeek {
    val isoWeekNumber = isoWeekNumber()
    val isoRepeatWeekNumber = isoWeekNumber - (isoWeekNumber / repeat.isoRepeatWeekNumber) * repeat.isoRepeatWeekNumber
    return NumberOfRepeatWeek(if (isoRepeatWeekNumber == 0) repeat.isoRepeatWeekNumber else isoRepeatWeekNumber)
}
