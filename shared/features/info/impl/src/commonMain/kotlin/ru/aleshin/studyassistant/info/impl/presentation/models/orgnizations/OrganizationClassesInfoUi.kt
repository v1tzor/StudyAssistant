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

package ru.aleshin.studyassistant.info.impl.presentation.models.orgnizations

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.extensions.toHorses
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis

/**
 * @author Stanislav Aleshin on 16.06.2024.
 */
@Parcelize
internal data class OrganizationClassesInfoUi(
    val numberOfClassesInWeek: Map<NumberOfRepeatWeek, Int>,
    val classesDurationInWeek: Map<NumberOfRepeatWeek, Millis>,
) : Parcelable {

    fun numberOfClassesString(): String {
        val classesList = numberOfClassesInWeek.toList()
        val sortedClasses = if (classesList.isNotEmpty()) {
            classesList.sortedBy { it.first.isoRepeatWeekNumber }
        } else {
            listOf(Pair(NumberOfRepeatWeek.ONE, 0))
        }
        val numbers = sortedClasses.map { it.second }
        return numbers.joinToString(separator = " | ")
    }

    fun classesDurationString(): String {
        val classesDurationsList = classesDurationInWeek.toList()
        val sortedDurations = if (classesDurationsList.isNotEmpty()) {
            classesDurationsList.sortedBy { it.first.isoRepeatWeekNumber }
        } else {
            listOf(Pair(NumberOfRepeatWeek.ONE, 0L))
        }
        val hours = sortedDurations.map { it.second.toHorses() }
        return hours.joinToString(separator = " | ")
    }
}