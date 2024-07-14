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

import ru.aleshin.studyassistant.core.common.extensions.mapEpochTimeToInstant
import ru.aleshin.studyassistant.core.data.mappers.users.mapToDomain
import ru.aleshin.studyassistant.core.domain.entities.requests.FriendRequests
import ru.aleshin.studyassistant.core.domain.entities.requests.FriendRequestsDetails
import ru.aleshin.studyassistant.core.remote.models.requests.FriendRequestsDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.requests.FriendRequestsPojo

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
internal fun FriendRequests.mapToRemote() = FriendRequestsPojo(
    received = received.mapValues { it.value.toEpochMilliseconds() },
    send = send.mapValues { it.value.toEpochMilliseconds() },
    lastActions = lastActions,
)

internal fun FriendRequestsPojo.mapToData() = FriendRequests(
    received = received.mapValues { it.value.mapEpochTimeToInstant() },
    send = send.mapValues { it.value.mapEpochTimeToInstant() },
    lastActions = lastActions,
)

internal fun FriendRequestsDetailsPojo.mapToData() = FriendRequestsDetails(
    received = received.mapKeys { it.key.mapToDomain() }.mapValues { it.value.mapEpochTimeToInstant() },
    send = send.mapKeys { it.key.mapToDomain() }.mapValues { it.value.mapEpochTimeToInstant() },
    lastActions = lastActions.mapKeys { it.key.mapToDomain() },
)