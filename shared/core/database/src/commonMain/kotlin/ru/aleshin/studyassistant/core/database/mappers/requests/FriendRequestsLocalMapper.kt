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

package ru.aleshin.studyassistant.core.database.mappers.requests

import ru.aleshin.studyassistant.core.common.extensions.decodeFromString
import ru.aleshin.studyassistant.core.common.extensions.encodeToString
import ru.aleshin.studyassistant.core.common.extensions.fromJson
import ru.aleshin.studyassistant.core.common.extensions.toJson
import ru.aleshin.studyassistant.core.database.models.requests.FriendRequestsDetailsEntity
import ru.aleshin.studyassistant.core.database.models.users.AppUserDetailsEntity
import ru.aleshin.studyassistant.sqldelight.user.CurrentFriendRequestsEntity

/**
 * @author Stanislav Aleshin on 26.07.2025.
 */
fun FriendRequestsDetailsEntity.mapToEntity() = CurrentFriendRequestsEntity(
    id = 1,
    document_id = uid,
    received = received.mapKeys { it.key.toJson<AppUserDetailsEntity>() }.encodeToString(),
    sent = send.mapKeys { it.key.toJson<AppUserDetailsEntity>() }.encodeToString(),
    last_actions = lastActions.mapKeys { it.key.toJson<AppUserDetailsEntity>() }.encodeToString(),
    updated_at = updatedAt,
)

fun CurrentFriendRequestsEntity.mapToDetails() = FriendRequestsDetailsEntity(
    uid = document_id,
    received = received.decodeFromString<Long>().mapKeys { it.key.fromJson<AppUserDetailsEntity>() },
    send = sent.decodeFromString<Long>().mapKeys { it.key.fromJson<AppUserDetailsEntity>() },
    lastActions = last_actions.decodeFromString<Boolean>().mapKeys { it.key.fromJson<AppUserDetailsEntity>() },
    updatedAt = updated_at,
)