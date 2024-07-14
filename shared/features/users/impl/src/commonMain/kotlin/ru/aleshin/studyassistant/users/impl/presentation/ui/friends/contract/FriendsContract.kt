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

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.InstantParceler
import ru.aleshin.studyassistant.users.impl.domain.entities.UsersFailures
import ru.aleshin.studyassistant.users.impl.presentation.models.AppUserUi
import ru.aleshin.studyassistant.users.impl.presentation.models.FriendRequestsDetailsUi

/**
 * @author Stanislav Aleshin on 12.07.2024
 */
@Immutable
@Parcelize
internal data class FriendsViewState(
    val isLoading: Boolean = true,
    val isLoadingSearch: Boolean = false,
    @TypeParceler<Instant, InstantParceler>
    val currentTime: Instant = Clock.System.now(),
    val friends: Map<Char, List<AppUserUi>> = emptyMap(),
    val requests: FriendRequestsDetailsUi? = null,
    val searchedUsers: List<AppUserUi> = emptyList(),
) : BaseViewState

internal sealed class FriendsEvent : BaseEvent {
    data object Init : FriendsEvent()
    data class SearchUsers(val code: String) : FriendsEvent()
    data class AcceptFriendRequest(val userId: UID) : FriendsEvent()
    data class RejectFriendRequest(val userId: UID) : FriendsEvent()
    data class SendFriendRequest(val userId: UID) : FriendsEvent()
    data class CancelSendFriendRequest(val userId: UID) : FriendsEvent()
    data class DeleteFriend(val userId: UID) : FriendsEvent()
    data class NavigateToFriendProfile(val userId: UID) : FriendsEvent()
    data object NavigateToRequests : FriendsEvent()
    data object NavigateToBack : FriendsEvent()
}

internal sealed class FriendsEffect : BaseUiEffect {
    data class ShowError(val failures: UsersFailures) : FriendsEffect()
    data class NavigateToLocal(val pushScreen: Screen) : FriendsEffect()
    data object NavigateToBack : FriendsEffect()
}

internal sealed class FriendsAction : BaseAction {
    data class UpdateFriends(
        val friends: Map<Char, List<AppUserUi>>,
        val requests: FriendRequestsDetailsUi?
    ) : FriendsAction()

    data class UpdateSearchedUsers(val users: List<AppUserUi>) : FriendsAction()
    data class UpdateCurrentTime(val time: Instant) : FriendsAction()
    data class UpdateLoading(val isLoading: Boolean) : FriendsAction()
    data class UpdateSearchLoading(val isLoading: Boolean) : FriendsAction()
}