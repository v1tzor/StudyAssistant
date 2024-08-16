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
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.database.models.schedule.BaseScheduleDetailsEntity
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.core.domain.entities.schedules.DateVersion
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.BaseSchedule
import ru.aleshin.studyassistant.core.domain.entities.schedules.base.MediatedBaseSchedule
import ru.aleshin.studyassistant.core.remote.models.classes.ClassPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.BaseScheduleDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.BaseSchedulePojo
import ru.aleshin.studyassistant.core.remote.models.schedule.MediatedBaseSchedulePojo
import ru.aleshin.studyassistant.sqldelight.schedules.BaseScheduleEntity

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun BaseScheduleDetailsPojo.mapToDomain() = BaseSchedule(
    uid = uid,
    dateVersion = DateVersion(dateVersionFrom.mapEpochTimeToInstant(), dateVersionTo.mapEpochTimeToInstant()),
    dayOfWeek = DayOfWeek.valueOf(weekDayOfWeek),
    week = NumberOfRepeatWeek.valueOf(week),
    classes = classes.map { it.mapToDomain() },
)

fun MediatedBaseSchedulePojo.mapToDomain() = MediatedBaseSchedule(
    uid = uid,
    dateVersion = DateVersion(dateVersionFrom.mapEpochTimeToInstant(), dateVersionTo.mapEpochTimeToInstant()),
    dayOfWeek = DayOfWeek.valueOf(weekDayOfWeek),
    week = NumberOfRepeatWeek.valueOf(week),
    classes = classes.map { it.mapToDomain() },
)

fun BaseScheduleDetailsEntity.mapToDomain() = BaseSchedule(
    uid = uid,
    dateVersion = DateVersion(dateVersionFrom.mapEpochTimeToInstant(), dateVersionTo.mapEpochTimeToInstant()),
    dayOfWeek = DayOfWeek.valueOf(weekDayOfWeek),
    week = NumberOfRepeatWeek.valueOf(week),
    classes = classes.map { it.mapToDomain() },
)

fun BaseSchedule.mapToRemoteData() = BaseSchedulePojo(
    uid = uid,
    dateVersionFrom = dateVersion.from.toEpochMilliseconds(),
    dateVersionTo = dateVersion.to.toEpochMilliseconds(),
    weekDayOfWeek = dayOfWeek.name,
    week = week.name,
    classes = mutableMapOf<UID, ClassPojo>().apply {
        classes.forEach { put(it.uid, it.mapToRemoteData()) }
    },
)

fun MediatedBaseSchedule.mapToRemoteData() = MediatedBaseSchedulePojo(
    uid = uid,
    dateVersionFrom = dateVersion.from.toEpochMilliseconds(),
    dateVersionTo = dateVersion.to.toEpochMilliseconds(),
    weekDayOfWeek = dayOfWeek.name,
    week = week.name,
    classes = classes.map { it.mapToRemoteData() },
)

fun BaseSchedule.mapToLocalData() = BaseScheduleEntity(
    uid = uid,
    date_version_from = dateVersion.from.toEpochMilliseconds(),
    date_version_to = dateVersion.to.toEpochMilliseconds(),
    week_day_of_week = dayOfWeek.name,
    week = week.name,
    classes = classes.map { Json.encodeToString(it.mapToLocalData()) },
)