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

package ru.aleshin.studyassistant.core.data.mappers.schedules

import kotlinx.datetime.DayOfWeek
import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.utils.sync.MultipleSyncMapper
import ru.aleshin.studyassistant.core.database.models.classes.ClassEntity
import ru.aleshin.studyassistant.core.database.models.schedule.BaseScheduleDetailsEntity
import ru.aleshin.studyassistant.core.database.models.schedule.BaseScheduleEntity
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.DateVersion
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.MediatedBaseSchedule
import ru.aleshin.studyassistant.core.remote.models.classes.ClassPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.BaseScheduleDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.BaseSchedulePojo
import ru.aleshin.studyassistant.core.remote.models.schedule.MediatedBaseSchedulePojo

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
// Remote

fun BaseSchedule.mapToRemoteData(userId: UID) = BaseSchedulePojo(
    id = uid,
    userId = userId,
    dateVersionFrom = dateVersion.from.toEpochMilliseconds(),
    dateVersionTo = dateVersion.to.toEpochMilliseconds(),
    weekDayOfWeek = dayOfWeek.name,
    week = week.name,
    classes = classes.map { it.mapToRemoteData().toJson() },
    updatedAt = updatedAt,
)

fun MediatedBaseSchedule.mapToRemoteData() = MediatedBaseSchedulePojo(
    uid = uid,
    dateVersionFrom = dateVersion.from.toEpochMilliseconds(),
    dateVersionTo = dateVersion.to.toEpochMilliseconds(),
    weekDayOfWeek = dayOfWeek.name,
    week = week.name,
    classes = classes.map { it.mapToRemoteData() },
)

fun BaseScheduleDetailsPojo.mapToDomain() = BaseSchedule(
    uid = uid,
    dateVersion = DateVersion(
        from = dateVersionFrom.mapEpochTimeToInstant(),
        to = dateVersionTo.mapEpochTimeToInstant(),
    ),
    dayOfWeek = DayOfWeek.valueOf(weekDayOfWeek),
    week = NumberOfRepeatWeek.valueOf(week),
    classes = classes.map { it.mapToDomain() },
    updatedAt = updatedAt,
)

fun MediatedBaseSchedulePojo.mapToDomain() = MediatedBaseSchedule(
    uid = uid,
    dateVersion = DateVersion(
        from = dateVersionFrom.mapEpochTimeToInstant(),
        to = dateVersionTo.mapEpochTimeToInstant(),
    ),
    dayOfWeek = DayOfWeek.valueOf(weekDayOfWeek),
    week = NumberOfRepeatWeek.valueOf(week),
    classes = classes.map { it.mapToDomain() },
)

// Local

fun BaseSchedule.mapToLocalData() = BaseScheduleEntity(
    uid = uid,
    dateVersionFrom = dateVersion.from.toEpochMilliseconds(),
    dateVersionTo = dateVersion.to.toEpochMilliseconds(),
    weekDayOfWeek = dayOfWeek.name,
    week = week.name,
    classes = classes.map { it.mapToLocalData().toJson() },
    updatedAt = updatedAt,
    isCacheData = 0L,
)

fun BaseScheduleDetailsEntity.mapToDomain() = BaseSchedule(
    uid = uid,
    dateVersion = DateVersion(
        from = dateVersionFrom.mapEpochTimeToInstant(),
        to = dateVersionTo.mapEpochTimeToInstant(),
    ),
    dayOfWeek = DayOfWeek.valueOf(weekDayOfWeek),
    week = NumberOfRepeatWeek.valueOf(week),
    classes = classes.map { it.mapToDomain() },
    updatedAt = updatedAt,
)

// Combined

fun BaseScheduleEntity.convertToRemote(userId: UID) = BaseSchedulePojo(
    id = uid,
    userId = userId,
    dateVersionFrom = dateVersionFrom,
    dateVersionTo = dateVersionTo,
    weekDayOfWeek = weekDayOfWeek,
    week = week,
    classes = classes.map { it.fromJson<ClassEntity>().mapToRemote().toJson() },
    updatedAt = updatedAt,
)

fun BaseSchedulePojo.convertToLocal() = BaseScheduleEntity(
    uid = id,
    dateVersionFrom = dateVersionFrom,
    dateVersionTo = dateVersionTo,
    weekDayOfWeek = weekDayOfWeek,
    week = week,
    classes = classes.map { it.fromJson<ClassPojo>().mapToLocal().toJson() },
    updatedAt = updatedAt,
    isCacheData = 1L,
)

// SyncMapper

class BaseScheduleSyncMapper : MultipleSyncMapper<BaseScheduleEntity, BaseSchedulePojo>(
    localToRemote = { userId -> convertToRemote(userId) },
    remoteToLocal = { convertToLocal() },
)