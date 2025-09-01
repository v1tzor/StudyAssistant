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

package ru.aleshin.studyassistant.users.impl.presentation.ui.requests.contract

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.users.api.UsersFeatureComponent.UsersConfig
import ru.aleshin.studyassistant.users.impl.domain.entities.UsersFailures
import ru.aleshin.studyassistant.users.impl.presentation.models.FriendRequestsDetailsUi

/**
 * @author Stanislav Aleshin on 13.07.2024
 */
@Serializable
internal data class RequestsState(
    val isLoading: Boolean = true,
    val currentTime: Instant = Clock.System.now(),
    val requests: FriendRequestsDetailsUi? = null,
) : StoreState

internal sealed class RequestsEvent : StoreEvent {
    data object Started : RequestsEvent()
    data class AcceptFriendRequest(val userId: UID) : RequestsEvent()
    data class RejectFriendRequest(val userId: UID) : RequestsEvent()
    data class CancelSendFriendRequest(val userId: UID) : RequestsEvent()
    data class ClickDeleteHistoryRequest(val userId: UID) : RequestsEvent()
    data class ClickUserProfile(val userId: UID) : RequestsEvent()
    data object ClickBack : RequestsEvent()
}

internal sealed class RequestsEffect : StoreEffect {
    data class ShowError(val failures: UsersFailures) : RequestsEffect()
}

internal sealed class RequestsAction : StoreAction {
    data class UpdateRequests(val requests: FriendRequestsDetailsUi) : RequestsAction()
    data class UpdateCurrentTime(val time: Instant) : RequestsAction()
    data class UpdateLoading(val isLoading: Boolean) : RequestsAction()
}

internal sealed class RequestsOutput : BaseOutput {
    data object NavigateToBack : RequestsOutput()
    data class NavigateToUserProfile(val config: UsersConfig.UserProfile) : RequestsOutput()
}