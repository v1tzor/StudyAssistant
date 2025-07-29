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

package ru.aleshin.studyassistant.core.database.mappers.shared

import ru.aleshin.studyassistant.core.common.extensions.decodeFromString
import ru.aleshin.studyassistant.core.common.extensions.encodeToString
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.database.models.shared.schedules.ReceivedMediatedSchedulesShortDetailsEntity
import ru.aleshin.studyassistant.core.database.models.shared.schedules.SentMediatedSchedulesShortDetailsEntity
import ru.aleshin.studyassistant.core.database.models.shared.schedules.SharedSchedulesShortDetailsEntity
import ru.aleshin.studyassistant.sqldelight.shared.CurrentSharedSchedulesEntity

/**
 * @author Stanislav Aleshin on 25.07.2025.
 */
fun CurrentSharedSchedulesEntity.mapToBase() = SharedSchedulesShortDetailsEntity(
    uid = document_id,
    received = received.decodeFromString<UID, ReceivedMediatedSchedulesShortDetailsEntity>(),
    sent = sent.decodeFromString<UID, SentMediatedSchedulesShortDetailsEntity>(),
    updatedAt = updated_at
)

fun SharedSchedulesShortDetailsEntity.mapToEntity() = CurrentSharedSchedulesEntity(
    id = 1,
    document_id = uid,
    received = received.encodeToString(),
    sent = sent.encodeToString(),
    updated_at = updatedAt
)