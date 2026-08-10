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

package ru.aleshin.studyassistant.core.data.mappers.share

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.tasks.mapToRemoteData
import ru.aleshin.studyassistant.core.domain.entities.share.HomeworkShare
import ru.aleshin.studyassistant.core.remote.models.shared.HomeworkSharePayloadPojo

/**
 * @author Stanislav Aleshin on 08.08.2026.
 */
fun HomeworkShare.mapToRemoteData() = HomeworkSharePayloadPojo(
    senderName = senderName,
    date = date.toEpochMilliseconds(),
    homeworks = homeworks.map { homework -> homework.mapToRemoteData() },
)

fun HomeworkSharePayloadPojo.mapToDomain() = HomeworkShare(
    senderName = senderName,
    date = date.mapEpochTimeToInstant(),
    homeworks = homeworks.map { homework -> homework.mapToDomain() },
)
