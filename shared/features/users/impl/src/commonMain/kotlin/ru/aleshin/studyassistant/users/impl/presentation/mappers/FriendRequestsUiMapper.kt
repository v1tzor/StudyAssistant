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

package ru.aleshin.studyassistant.users.impl.presentation.mappers

import ru.aleshin.studyassistant.core.domain.entities.requests.FriendRequestsDetails
import ru.aleshin.studyassistant.users.impl.presentation.models.FriendRequestsDetailsUi

/**
 * @author Stanislav Aleshin on 12.07.2024.
 */
internal fun FriendRequestsDetails.mapToUi() = FriendRequestsDetailsUi(
    received = received.mapKeys { it.key.mapToUi() },
    send = send.mapKeys { it.key.mapToUi() },
    lastActions = lastActions.mapKeys { it.key.mapToUi() },
    updatedAt = updatedAt,
)