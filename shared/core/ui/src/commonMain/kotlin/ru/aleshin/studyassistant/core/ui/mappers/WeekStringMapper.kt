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

package ru.aleshin.studyassistant.core.ui.mappers

import kotlinx.datetime.DayOfWeek
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.ui.theme.tokens.StudyAssistantStrings

/**
 * @author Stanislav Aleshin on 27.04.2024.
 */
fun NumberOfRepeatWeek.mapToSting(strings: StudyAssistantStrings) = when (this) {
    NumberOfRepeatWeek.ONE -> strings.oneWeekPlural
    NumberOfRepeatWeek.TWO -> strings.twoWeekPlural
    NumberOfRepeatWeek.THREE -> strings.threeWeekPlural
}

fun DayOfWeek.mapToSting(strings: StudyAssistantStrings) = when (this) {
    DayOfWeek.MONDAY -> strings.mondayTitle
    DayOfWeek.TUESDAY -> strings.tuesdayTitle
    DayOfWeek.WEDNESDAY -> strings.wednesdayTitle
    DayOfWeek.THURSDAY -> strings.thursdayTitle
    DayOfWeek.FRIDAY -> strings.fridayTitle
    DayOfWeek.SATURDAY -> strings.saturdayTitle
    DayOfWeek.SUNDAY -> strings.sundayTitle
    else -> error("Unknown day of week: $this")
}