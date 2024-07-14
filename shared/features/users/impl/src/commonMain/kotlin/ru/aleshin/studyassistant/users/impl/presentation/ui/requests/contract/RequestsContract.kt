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
import ru.aleshin.studyassistant.users.impl.presentation.models.FriendRequestsDetailsUi

/**
 * @author Stanislav Aleshin on 13.07.2024
 */
@Immutable
@Parcelize
internal data class RequestsViewState(
    val isLoading: Boolean = true,
    @TypeParceler<Instant, InstantParceler>
    val currentTime: Instant = Clock.System.now(),
    val requests: FriendRequestsDetailsUi? = null,
) : BaseViewState

internal sealed class RequestsEvent : BaseEvent {
    data object Init : RequestsEvent()
    data class AcceptFriendRequest(val userId: UID) : RequestsEvent()
    data class RejectFriendRequest(val userId: UID) : RequestsEvent()
    data class CancelSendFriendRequest(val userId: UID) : RequestsEvent()
    data class DeleteHistoryRequest(val userId: UID) : RequestsEvent()
    data class NavigateToFriendProfile(val userId: UID) : RequestsEvent()
    data object NavigateToBack : RequestsEvent()
}

internal sealed class RequestsEffect : BaseUiEffect {
    data class ShowError(val failures: UsersFailures) : RequestsEffect()
    data class NavigateToLocal(val pushScreen: Screen) : RequestsEffect()
    data object NavigateToBack : RequestsEffect()
}

internal sealed class RequestsAction : BaseAction {
    data class UpdateRequests(val requests: FriendRequestsDetailsUi) : RequestsAction()
    data class UpdateCurrentTime(val time: Instant) : RequestsAction()
    data class UpdateLoading(val isLoading: Boolean) : RequestsAction()
}