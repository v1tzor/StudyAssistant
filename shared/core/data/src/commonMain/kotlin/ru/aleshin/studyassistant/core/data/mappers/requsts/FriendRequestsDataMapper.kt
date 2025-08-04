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

package ru.aleshin.studyassistant.core.data.mappers.requsts

import ru.aleshin.studyassistant.core.common.extensions.decodeFromString
import ru.aleshin.studyassistant.core.common.extensions.encodeToString
import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.data.mappers.users.mapToLocalDataDetails
import ru.aleshin.studyassistant.core.data.mappers.users.mapToRemoteDataDetails
import ru.aleshin.studyassistant.core.data.utils.sync.SingleSyncMapper
import ru.aleshin.studyassistant.core.database.models.requests.FriendRequestsDetailsEntity
import ru.aleshin.studyassistant.core.domain.entities.requests.FriendRequests
import ru.aleshin.studyassistant.core.domain.entities.requests.FriendRequestsDetails
import ru.aleshin.studyassistant.core.remote.models.requests.FriendRequestsDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.requests.FriendRequestsPojo

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
class FriendRequestsSyncMapper : SingleSyncMapper<FriendRequestsDetailsEntity, FriendRequestsDetailsPojo>(
    localToRemote = { mapToDomainDetails().mapToRemoteDetails("") },
    remoteToLocal = { mapToDomainDetails().mapToLocal() },
)

fun FriendRequests.mapToRemote(userId: String) = FriendRequestsPojo(
    id = userId,
    received = received.mapValues { it.value.toEpochMilliseconds() }.encodeToString<UID, Long>(),
    send = send.mapValues { it.value.toEpochMilliseconds() }.encodeToString<UID, Long>(),
    lastActions = lastActions.encodeToString<UID, Boolean>(),
    updatedAt = updatedAt,
)

fun FriendRequestsDetails.mapToRemote(userId: String) = FriendRequestsPojo(
    id = userId,
    received = received.mapKeys { it.key.uid }.mapValues { it.value.toEpochMilliseconds() }.encodeToString<UID, Long>(),
    send = received.mapKeys { it.key.uid }.mapValues { it.value.toEpochMilliseconds() }.encodeToString<UID, Long>(),
    lastActions = lastActions.mapKeys { it.key.uid }.encodeToString<UID, Boolean>(),
    updatedAt = updatedAt,
)

fun FriendRequestsDetails.mapToRemoteDetails(userId: String) = FriendRequestsDetailsPojo(
    id = userId,
    received = received.mapKeys { it.key.mapToRemoteDataDetails() }.mapValues { it.value.toEpochMilliseconds() },
    send = received.mapKeys { it.key.mapToRemoteDataDetails() }.mapValues { it.value.toEpochMilliseconds() },
    lastActions = lastActions.mapKeys { it.key.mapToRemoteDataDetails() },
    updatedAt = updatedAt,
)

fun FriendRequestsPojo.mapToDomain() = FriendRequests(
    received = received.decodeFromString<UID, Long>().mapValues { it.value.mapEpochTimeToInstant() },
    send = send.decodeFromString<UID, Long>().mapValues { it.value.mapEpochTimeToInstant() },
    lastActions = lastActions.decodeFromString<UID, Boolean>(),
    updatedAt = updatedAt,
)

fun FriendRequestsDetailsPojo.mapToDomainDetails() = FriendRequestsDetails(
    received = received.mapKeys { it.key.mapToDomain() }.mapValues { it.value.mapEpochTimeToInstant() },
    send = send.mapKeys { it.key.mapToDomain() }.mapValues { it.value.mapEpochTimeToInstant() },
    lastActions = lastActions.mapKeys { it.key.mapToDomain() },
    updatedAt = updatedAt,
)

fun FriendRequestsDetailsPojo.mapToDomain() = FriendRequests(
    received = received.mapKeys { it.key.uid }.mapValues { it.value.mapEpochTimeToInstant() },
    send = send.mapKeys { it.key.uid }.mapValues { it.value.mapEpochTimeToInstant() },
    lastActions = lastActions.mapKeys { it.key.uid },
    updatedAt = updatedAt,
)

fun FriendRequestsDetails.mapToLocal() = FriendRequestsDetailsEntity(
    uid = "1",
    received = received.mapKeys { it.key.mapToLocalDataDetails() }.mapValues { it.value.toEpochMilliseconds() },
    send = send.mapKeys { it.key.mapToLocalDataDetails() }.mapValues { it.value.toEpochMilliseconds() },
    lastActions = lastActions.mapKeys { it.key.mapToLocalDataDetails() }.mapValues { it.value },
    updatedAt = updatedAt,
)

fun FriendRequestsDetailsEntity.mapToDomain() = FriendRequests(
    received = received.mapKeys { it.key.uid }.mapValues { it.value.mapEpochTimeToInstant() },
    send = send.mapKeys { it.key.uid }.mapValues { it.value.mapEpochTimeToInstant() },
    lastActions = lastActions.mapKeys { it.key.uid },
    updatedAt = updatedAt,
)

fun FriendRequestsDetailsEntity.mapToDomainDetails() = FriendRequestsDetails(
    received = received.mapKeys { it.key.mapToDomain() }.mapValues { it.value.mapEpochTimeToInstant() },
    send = send.mapKeys { it.key.mapToDomain() }.mapValues { it.value.mapEpochTimeToInstant() },
    lastActions = lastActions.mapKeys { it.key.mapToDomain() },
    updatedAt = updatedAt,
)