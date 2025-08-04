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

package ru.aleshin.studyassistant.core.data.mappers.ai

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.data.utils.sync.MultipleSyncMapper
import ru.aleshin.studyassistant.core.database.models.ai.BaseDailyAiResponsesEntity
import ru.aleshin.studyassistant.core.domain.entities.ai.DailyAiResponses
import ru.aleshin.studyassistant.core.remote.models.ai.DailyAiResponsesPojo

/**
 * @author Stanislav Aleshin on 01.08.2025.
 */
fun DailyAiResponses.mapToRemote(userId: String) = DailyAiResponsesPojo(
    id = id,
    userId = userId,
    totalResponses = totalResponses,
    date = date.toEpochMilliseconds(),
    updatedAt = updatedAt,
)

fun DailyAiResponsesPojo.mapToDomain() = DailyAiResponses(
    id = id,
    totalResponses = totalResponses,
    date = date.mapEpochTimeToInstant(),
    updatedAt = updatedAt,
)

fun DailyAiResponses.mapToLocal() = BaseDailyAiResponsesEntity(
    uid = id,
    totalResponses = totalResponses.toLong(),
    date = date.toEpochMilliseconds(),
    updatedAt = updatedAt,
)

fun BaseDailyAiResponsesEntity.mapToDomain() = DailyAiResponses(
    id = uid,
    totalResponses = totalResponses.toInt(),
    date = date.mapEpochTimeToInstant(),
    updatedAt = updatedAt,
)

fun BaseDailyAiResponsesEntity.convertToRemote(userId: String) = DailyAiResponsesPojo(
    id = uid,
    userId = userId,
    totalResponses = totalResponses.toInt(),
    date = date,
    updatedAt = updatedAt,
)

fun DailyAiResponsesPojo.convertToLocal() = BaseDailyAiResponsesEntity(
    uid = id,
    totalResponses = totalResponses.toLong(),
    date = date,
    updatedAt = updatedAt,
)

class DailyAiResponsesSyncMapper : MultipleSyncMapper<BaseDailyAiResponsesEntity, DailyAiResponsesPojo>(
    localToRemote = { currentUser -> convertToRemote(currentUser) },
    remoteToLocal = { convertToLocal() },
)