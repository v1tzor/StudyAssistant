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

package ru.aleshin.studyassistant.core.database.mappers.schedules

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.database.models.classes.ClassDetailsEntity
import ru.aleshin.studyassistant.core.database.models.classes.ClassEntity
import ru.aleshin.studyassistant.core.database.models.schedule.BaseScheduleDetailsEntity
import ru.aleshin.studyassistant.core.database.models.schedule.BaseScheduleEntity
import ru.aleshin.studyassistant.sqldelight.schedules.BaseScheduleEntity as LocalBaseScheduleEntity

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun LocalBaseScheduleEntity.mapToBase() = BaseScheduleEntity(
    uid = uid,
    dateVersionFrom = date_version_from,
    dateVersionTo = date_version_to,
    weekDayOfWeek = week_day_of_week,
    week = week,
    classes = classes,
    updatedAt = updated_at,
    isCacheData = is_cache_data,
)

fun BaseScheduleEntity.mapToEntity() = LocalBaseScheduleEntity(
    uid = uid,
    date_version_from = dateVersionFrom,
    date_version_to = dateVersionTo,
    week_day_of_week = weekDayOfWeek,
    week = week,
    classes = classes,
    updated_at = updatedAt,
    is_cache_data = isCacheData,
)

fun BaseScheduleDetailsEntity.mapToBase() = BaseScheduleEntity(
    uid = uid,
    dateVersionFrom = dateVersionFrom,
    dateVersionTo = dateVersionTo,
    weekDayOfWeek = weekDayOfWeek,
    week = week,
    classes = classes.map { it.mapToBase().toJson<ClassEntity>() },
    updatedAt = updatedAt,
    isCacheData = 0L,
)

suspend fun BaseScheduleEntity.mapToDetails(
    classesMapper: suspend (ClassEntity) -> ClassDetailsEntity,
) = BaseScheduleDetailsEntity(
    uid = uid,
    dateVersionFrom = dateVersionFrom,
    dateVersionTo = dateVersionTo,
    weekDayOfWeek = weekDayOfWeek,
    week = week,
    classes = classes.map { classesMapper(it.fromJson<ClassEntity>()) },
    updatedAt = updatedAt,
)