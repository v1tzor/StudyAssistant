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

import entities.classes.Class
import entities.common.DayOfNumberedWeek
import entities.common.NumberOfRepeatWeek
import functional.UID
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.Instant
import randomUUID

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
data class BaseSchedule(
    val uid: UID,
    val dateVersion: DateVersion,
    val dayOfWeek: DayOfWeek,
    val week: NumberOfRepeatWeek = NumberOfRepeatWeek.ONE,
    val classes: List<Class>,
) {
    companion object {
        fun createActual(
            currentDate: Instant,
            dayOfNumberedWeek: DayOfNumberedWeek,
            classes: List<Class> = emptyList(),
        ): BaseSchedule {
            return BaseSchedule(
                uid = randomUUID(),
                dateVersion = DateVersion.createNewVersion(currentDate),
                dayOfWeek = dayOfNumberedWeek.dayOfWeek,
                week = dayOfNumberedWeek.week,
                classes = classes,
            )
        }
    }
}
