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

package ru.aleshin.studyassistant.users.impl.presentation.ui.user.contract

import androidx.compose.runtime.Immutable
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.users.UserFriendStatus
import ru.aleshin.studyassistant.users.impl.domain.entities.UsersFailures
import ru.aleshin.studyassistant.users.impl.presentation.models.AppUserUi

/**
 * @author Stanislav Aleshin on 15.07.2024
 */
@Immutable
@Parcelize
internal data class UserProfileViewState(
    val isLoading: Boolean = true,
    val user: AppUserUi? = null,
    val friendStatus: UserFriendStatus? = null,
) : BaseViewState

internal sealed class UserProfileEvent : BaseEvent {
    data class Init(val userId: UID) : UserProfileEvent()
    data object SendFriendRequest : UserProfileEvent()
    data object AcceptFriendRequest : UserProfileEvent()
    data object CancelSendFriendRequest : UserProfileEvent()
    data object DeleteFromFriends : UserProfileEvent()
    data object NavigateToBack : UserProfileEvent()
}

internal sealed class UserProfileEffect : BaseUiEffect {
    data class ShowError(val failures: UsersFailures) : UserProfileEffect()
    data object NavigateToBack : UserProfileEffect()
}

internal sealed class UserProfileAction : BaseAction {
    data class UpdateUser(val user: AppUserUi, val status: UserFriendStatus) : UserProfileAction()
    data class UpdateLoading(val isLoading: Boolean) : UserProfileAction()
}