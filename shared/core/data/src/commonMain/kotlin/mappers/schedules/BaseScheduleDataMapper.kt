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

package mappers.schedules

import entities.schedules.BaseSchedule
import entities.schedules.DateVersion
import entities.settings.NumberOfWeek
import extensions.mapEpochTimeToInstant
import kotlinx.datetime.DayOfWeek
import mappers.tasks.mapToData
import mappers.tasks.mapToDomain
import models.classes.ClassDetailsData
import models.schedules.BaseScheduleDetailsData
import models.schedules.BaseSchedulePojo
import ru.aleshin.studyassistant.sqldelight.schedules.BaseScheduleEntity

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun BaseScheduleDetailsData.mapToDomain() = BaseSchedule(
    uid = uid,
    dateVersion = DateVersion(dateVersionFrom.mapEpochTimeToInstant(), dateVersionTo.mapEpochTimeToInstant()),
    weekDayOfWeek = DayOfWeek.valueOf(weekDayOfWeek),
    week = NumberOfWeek.valueOf(week),
    classes = classes.map { it.mapToDomain() },
)

fun BaseSchedule.mapToData() = BaseScheduleDetailsData(
    uid = uid,
    dateVersionFrom = dateVersion.from.toEpochMilliseconds(),
    dateVersionTo = dateVersion.to.toEpochMilliseconds(),
    weekDayOfWeek = weekDayOfWeek.name,
    week = week.name,
    classes = classes.map { it.mapToData() },
)

fun BaseScheduleDetailsData.mapToLocalData() = BaseScheduleEntity(
    uid = uid,
    date_version_from = dateVersionFrom,
    date_version_to = dateVersionTo,
    week_day_of_week = weekDayOfWeek,
    week = week,
)

fun BaseScheduleEntity.mapToDetailsData(
    classes: List<ClassDetailsData>,
) = BaseScheduleDetailsData(
    uid = uid,
    dateVersionFrom = date_version_from,
    dateVersionTo = date_version_to,
    weekDayOfWeek = week_day_of_week,
    week = week,
    classes = classes,
)

fun BaseScheduleDetailsData.mapToRemoteData() = BaseSchedulePojo(
    uid = uid,
    dateVersionFrom = dateVersionFrom,
    dateVersionTo = dateVersionTo,
    weekDayOfWeek = weekDayOfWeek,
    week = week,
)

fun BaseSchedulePojo.mapToDetailsData(
    classes: List<ClassDetailsData>,
) = BaseScheduleDetailsData(
    uid = uid,
    dateVersionFrom = dateVersionFrom,
    dateVersionTo = dateVersionTo,
    weekDayOfWeek = weekDayOfWeek,
    week = week,
    classes = classes,
)
