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
import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.database.models.shared.homeworks.ReceivedMediatedHomeworksDetailsEntity
import ru.aleshin.studyassistant.core.database.models.shared.homeworks.SentMediatedHomeworksDetailsEntity
import ru.aleshin.studyassistant.core.database.models.shared.homeworks.SharedHomeworksDetailsEntity
import ru.aleshin.studyassistant.sqldelight.shared.CurrentSharedHomeworksEntity

/**
 * @author Stanislav Aleshin on 25.07.2025.
 */
fun CurrentSharedHomeworksEntity.mapToBase() = SharedHomeworksDetailsEntity(
    uid = document_id,
    received = received.decodeFromString<String>().mapValues { it.value.fromJson<ReceivedMediatedHomeworksDetailsEntity>() },
    sent = sent.decodeFromString<String>().mapValues { it.value.fromJson<SentMediatedHomeworksDetailsEntity>() },
    updatedAt = updated_at
)

fun SharedHomeworksDetailsEntity.mapToEntity() = CurrentSharedHomeworksEntity(
    id = 1,
    document_id = uid,
    received = received.mapValues { it.value.toJson<ReceivedMediatedHomeworksDetailsEntity>() }.encodeToString(),
    sent = sent.mapValues { it.value.toJson<SentMediatedHomeworksDetailsEntity>() }.encodeToString(),
    updated_at = updatedAt
)