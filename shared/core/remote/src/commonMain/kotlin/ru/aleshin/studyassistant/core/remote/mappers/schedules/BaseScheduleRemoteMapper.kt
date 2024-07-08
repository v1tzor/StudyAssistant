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

package ru.aleshin.studyassistant.core.remote.mappers.schedules

import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.models.classes.ClassDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.classes.ClassPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.BaseScheduleDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.BaseSchedulePojo

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun BaseScheduleDetailsPojo.mapToBase() = BaseSchedulePojo(
    uid = uid,
    dateVersionFrom = dateVersionFrom,
    dateVersionTo = dateVersionTo,
    weekDayOfWeek = weekDayOfWeek,
    week = week,
    classes = mutableMapOf<UID, ClassPojo>().apply {
        classes.forEach { put(it.uid, it.mapToBase()) }
    },
)

suspend fun BaseSchedulePojo.mapToDetails(
    classMapper: suspend (ClassPojo) -> ClassDetailsPojo,
) = BaseScheduleDetailsPojo(
    uid = uid,
    dateVersionFrom = dateVersionFrom,
    dateVersionTo = dateVersionTo,
    weekDayOfWeek = weekDayOfWeek,
    week = week,
    classes = classes.map { classMapper(it.value) },
)