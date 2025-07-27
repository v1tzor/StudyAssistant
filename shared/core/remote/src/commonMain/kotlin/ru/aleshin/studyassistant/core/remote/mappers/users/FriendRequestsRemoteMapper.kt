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

package ru.aleshin.studyassistant.core.remote.mappers.users

import ru.aleshin.studyassistant.core.common.extensions.encodeToString
import ru.aleshin.studyassistant.core.remote.models.requests.FriendRequestsDetailsPojo
import ru.aleshin.studyassistant.core.remote.models.requests.FriendRequestsPojo

/**
 * @author Stanislav Aleshin on 26.07.2025.
 */
fun FriendRequestsDetailsPojo.mapToBase() = FriendRequestsPojo(
    id = id,
    received = received.mapKeys { it.key.uid }.encodeToString<Long>(),
    send = send.mapKeys { it.key.uid }.encodeToString<Long>(),
    lastActions = lastActions.mapKeys { it.key.uid }.encodeToString<Boolean>(),
)