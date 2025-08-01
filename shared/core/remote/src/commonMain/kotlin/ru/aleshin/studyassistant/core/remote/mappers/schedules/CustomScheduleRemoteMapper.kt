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

import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.remote.models.classes.ClassDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.classes.ClassPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.CustomScheduleDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.schedule.CustomSchedulePojo

/**
 * @author Stanislav Aleshin on 04.05.2024.
 */
fun CustomScheduleDetailsPojo.mapToRemoteData(userId: UID) = CustomSchedulePojo(
    id = uid,
    userId = userId,
    date = date,
    classes = classes.map { it.mapToBase().toJson<ClassPojo>() },
    updatedAt = updatedAt,
)

suspend fun CustomSchedulePojo.mapToDetails(
    classMapper: suspend (ClassPojo) -> ClassDetailsPojo,
) = CustomScheduleDetailsPojo(
    uid = id,
    date = date,
    classes = classes.map { classMapper(it.fromJson<ClassPojo>()) },
    updatedAt = updatedAt,
)