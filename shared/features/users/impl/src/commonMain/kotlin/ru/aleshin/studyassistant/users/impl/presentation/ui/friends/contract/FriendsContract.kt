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

package ru.aleshin.studyassistant.users.impl.presentation.ui.friends.contract

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
import ru.aleshin.studyassistant.users.impl.presentation.models.AppUserUi
import ru.aleshin.studyassistant.users.impl.presentation.models.FriendRequestsDetailsUi

/**
 * @author Stanislav Aleshin on 12.07.2024
 */
@Serializable
internal data class FriendsState(
    val isLoading: Boolean = true,
    val isLoadingSearch: Boolean = false,
    val currentTime: Instant = Clock.System.now(),
    val friends: Map<Char, List<AppUserUi>> = emptyMap(),
    val requests: FriendRequestsDetailsUi? = null,
    val searchedUsers: List<AppUserUi> = emptyList(),
) : StoreState

internal sealed class FriendsEvent : StoreEvent {
    data object Started : FriendsEvent()
    data class SearchUsers(val code: String) : FriendsEvent()
    data class AcceptFriendRequest(val userId: UID) : FriendsEvent()
    data class RejectFriendRequest(val userId: UID) : FriendsEvent()
    data class SendFriendRequest(val userId: UID) : FriendsEvent()
    data class CancelSendFriendRequest(val userId: UID) : FriendsEvent()
    data class DeleteFriend(val userId: UID) : FriendsEvent()
    data class ClickUserProfile(val userId: UID) : FriendsEvent()
    data object ClickShowRequests : FriendsEvent()
    data object ClickBack : FriendsEvent()
}

internal sealed class FriendsEffect : StoreEffect {
    data class ShowError(val failures: UsersFailures) : FriendsEffect()
}

internal sealed class FriendsAction : StoreAction {
    data class UpdateFriends(
        val friends: Map<Char, List<AppUserUi>>,
        val requests: FriendRequestsDetailsUi?
    ) : FriendsAction()

    data class UpdateSearchedUsers(val users: List<AppUserUi>) : FriendsAction()
    data class UpdateCurrentTime(val time: Instant) : FriendsAction()
    data class UpdateLoading(val isLoading: Boolean) : FriendsAction()
    data class UpdateSearchLoading(val isLoading: Boolean) : FriendsAction()
}

internal sealed class FriendsOutput : BaseOutput {
    data object NavigateToBack : FriendsOutput()
    data object NavigateToRequests : FriendsOutput()
    data class NavigateToUserProfile(val config: UsersConfig.UserProfile) : FriendsOutput()
}