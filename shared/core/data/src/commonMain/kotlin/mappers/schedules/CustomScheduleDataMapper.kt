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

import entities.schedules.CustomSchedule
import extensions.mapEpochTimeToInstant
import mappers.tasks.mapToData
import mappers.tasks.mapToDomain
import models.classes.ClassDetailsData
import models.schedules.CustomScheduleDetailsData
import models.schedules.CustomSchedulePojo
import ru.aleshin.studyassistant.sqldelight.schedules.CustomScheduleEntity

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun CustomScheduleDetailsData.mapToDomain() = CustomSchedule(
    uid = uid,
    date = date.mapEpochTimeToInstant(),
    classes = classes.map { it.mapToDomain() },
)

fun CustomSchedule.mapToData() = CustomScheduleDetailsData(
    uid = uid,
    date = date.toEpochMilliseconds(),
    classes = classes.map { it.mapToData() },
)

fun CustomScheduleDetailsData.mapToLocalData() = CustomScheduleEntity(
    uid = uid,
    date = date,
)

fun CustomScheduleEntity.mapToDetailsData(
    classes: List<ClassDetailsData>,
) = CustomScheduleDetailsData(
    uid = uid,
    date = date,
    classes = classes,
)

fun CustomScheduleDetailsData.mapToRemoteData() = CustomSchedulePojo(
    uid = uid,
    date = date,
)

fun CustomSchedulePojo.mapToDetailsData(
    classes: List<ClassDetailsData>,
) = CustomScheduleDetailsData(
    uid = uid,
    date = date,
    classes = classes,
)
