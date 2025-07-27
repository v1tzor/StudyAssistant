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

package ru.aleshin.studyassistant.core.database.mappers.sync

import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChange
import ru.aleshin.studyassistant.core.domain.entities.sync.OfflineChangeType
import ru.aleshin.studyassistant.sqldelight.sync.OfflineChangeEntity

/**
 * @author Stanislav Aleshin on 19.07.2025.
 */
fun OfflineChangeEntity.convertToBase() = OfflineChange(
    id = id,
    documentId = document_id,
    updatedAt = updated_at,
    type = OfflineChangeType.valueOf(type),
    sourceKey = source_key,
)

fun OfflineChange.convertToLocal() = OfflineChangeEntity(
    id = id,
    type = type.name,
    document_id = documentId,
    updated_at = updatedAt,
    source_key = sourceKey,
)