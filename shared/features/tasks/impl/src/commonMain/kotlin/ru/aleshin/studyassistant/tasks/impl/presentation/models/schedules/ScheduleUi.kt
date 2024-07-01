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

package ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules

import dev.icerock.moko.parcelize.Parcelable
import dev.icerock.moko.parcelize.Parcelize

/**
 * @author Stanislav Aleshin on 29.06.2024.
 */
@Parcelize
internal sealed class ScheduleUi : Parcelable {

    data class Base(val data: BaseScheduleUi?) : ScheduleUi()

    data class Custom(val data: CustomScheduleUi?) : ScheduleUi()

    val classes: List<ClassUi>
        get() = mapToValue(onBaseSchedule = { it?.classes }, onCustomSchedule = { it?.classes }) ?: emptyList()

    inline fun <T> mapToValue(
        onBaseSchedule: (BaseScheduleUi?) -> T,
        onCustomSchedule: (CustomScheduleUi?) -> T,
    ) = when (this) {
        is Base -> onBaseSchedule(data)
        is Custom -> onCustomSchedule(data)
    }
}