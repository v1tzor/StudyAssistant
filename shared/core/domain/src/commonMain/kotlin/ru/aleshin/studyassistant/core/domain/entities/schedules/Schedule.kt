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

package ru.aleshin.studyassistant.core.domain.entities.schedules

import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule

/**
 * @author Stanislav Aleshin on 29.06.2024.
 */
sealed class Schedule {

    data class Base(val data: BaseSchedule?) : Schedule()

    data class Custom(val data: CustomSchedule?) : Schedule()

    inline fun <T> mapToValue(
        onBaseSchedule: (BaseSchedule?) -> T,
        onCustomSchedule: (CustomSchedule?) -> T,
    ) = when (this) {
        is Base -> onBaseSchedule(data)
        is Custom -> onCustomSchedule(data)
    }
}