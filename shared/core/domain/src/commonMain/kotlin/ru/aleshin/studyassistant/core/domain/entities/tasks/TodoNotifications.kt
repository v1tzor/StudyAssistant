/*
 * Copyright 2026 Stanislav Aleshin
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
