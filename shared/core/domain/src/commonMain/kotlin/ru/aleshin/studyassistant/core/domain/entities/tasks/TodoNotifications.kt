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

package ru.aleshin.studyassistant.core.domain.entities.tasks

import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.extensions.shiftDay
import ru.aleshin.studyassistant.core.common.extensions.shiftMinutes

/**
 * @author Stanislav Aleshin on 31.08.2024.
 */
data class TodoNotifications(
    val beforeStart: Boolean = true,
    val fifteenMinutesBefore: Boolean = false,
    val oneHourBefore: Boolean = false,
    val threeHourBefore: Boolean = false,
    val oneDayBefore: Boolean = false,
    val oneWeekBefore: Boolean = false,
) {
    fun toTypes() = mutableListOf<TodoNotificationType>().apply {
        if (beforeStart) add(TodoNotificationType.START)
        if (fifteenMinutesBefore) add(TodoNotificationType.FIFTEEN_MINUTES_BEFORE)
        if (oneHourBefore) add(TodoNotificationType.ONE_HOUR_BEFORE)
        if (threeHourBefore) add(TodoNotificationType.THREE_HOUR_BEFORE)
        if (oneDayBefore) add(TodoNotificationType.ONE_DAY_BEFORE)
        if (oneWeekBefore) add(TodoNotificationType.ONE_WEEK_BEFORE)
    }.toList()
}

enum class TodoNotificationType(val idAmount: Long) {
    START(0),
    FIFTEEN_MINUTES_BEFORE(60L),
    ONE_HOUR_BEFORE(10L),
    THREE_HOUR_BEFORE(20L),
    ONE_DAY_BEFORE(30L),
    ONE_WEEK_BEFORE(50L);

    fun fetchNotifyTrigger(deadline: Instant) = when (this) {
        START -> deadline
        FIFTEEN_MINUTES_BEFORE -> deadline.shiftMinutes(-15)
        ONE_HOUR_BEFORE -> deadline.shiftMinutes(-60)
        THREE_HOUR_BEFORE -> deadline.shiftMinutes(-180)
        ONE_DAY_BEFORE -> deadline.shiftDay(-1)
        ONE_WEEK_BEFORE -> deadline.shiftDay(-7)
    }
}