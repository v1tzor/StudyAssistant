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

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.users.UserFriendStatus
import ru.aleshin.studyassistant.users.impl.domain.entities.UsersFailures
import ru.aleshin.studyassistant.users.impl.presentation.models.AppUserUi

/**
 * @author Stanislav Aleshin on 15.07.2024
 */
@Serializable
internal data class UserProfileState(
    val isLoading: Boolean = true,
    val user: AppUserUi? = null,
    val friendStatus: UserFriendStatus? = null,
) : StoreState

internal sealed class UserProfileEvent : StoreEvent {
    data class Started(val userId: UID) : UserProfileEvent()
    data object SendFriendRequest : UserProfileEvent()
    data object AcceptFriendRequest : UserProfileEvent()
    data object CancelSendFriendRequest : UserProfileEvent()
    data object DeleteFromFriends : UserProfileEvent()
    data object ClickBack : UserProfileEvent()
}

internal sealed class UserProfileEffect : StoreEffect {
    data class ShowError(val failures: UsersFailures) : UserProfileEffect()
}

internal sealed class UserProfileAction : StoreAction {
    data class UpdateUser(val user: AppUserUi, val status: UserFriendStatus) : UserProfileAction()
    data class UpdateLoading(val isLoading: Boolean) : UserProfileAction()
}

internal data class UserProfileInput(
    val userId: UID
) : BaseInput

internal sealed class UserProfileOutput : BaseOutput {
    data object NavigateToBack : UserProfileOutput()
}