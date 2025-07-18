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

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.database.models.schedule.CustomScheduleDetailsEntity
import ru.aleshin.studyassistant.core.domain.entities.schedules.custom.CustomSchedule
import ru.aleshin.studyassistant.core.remote.models.schedule.CustomScheduleDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.CustomSchedulePojo
import ru.aleshin.studyassistant.sqldelight.schedules.CustomScheduleEntity

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun CustomScheduleDetailsPojo.mapToDomain() = CustomSchedule(
    uid = uid,
    date = date.mapEpochTimeToInstant(),
    classes = classes.map { it.mapToDomain() },
)

fun CustomScheduleDetailsEntity.mapToDomain() = CustomSchedule(
    uid = uid,
    date = date.mapEpochTimeToInstant(),
    classes = classes.map { it.mapToDomain() },
)

fun CustomSchedule.mapToRemoteData(userId: UID) = CustomSchedulePojo(
    uid = uid,
    userId = userId,
    date = date.toEpochMilliseconds(),
    classes = classes.map { it.mapToRemoteData().toJson() },
)

fun CustomSchedule.mapToLocalData() = CustomScheduleEntity(
    uid = uid,
    date = date.toEpochMilliseconds(),
    classes = classes.map { it.mapToLocalData().toJson() },
)