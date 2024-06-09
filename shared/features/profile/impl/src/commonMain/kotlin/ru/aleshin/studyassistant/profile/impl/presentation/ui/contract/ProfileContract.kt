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

package ru.aleshin.studyassistant.profile.impl.presentation.ui.contract

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.profile.impl.domain.entities.ProfileFailures
import ru.aleshin.studyassistant.profile.impl.presentation.models.AppUserUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.FriendRequestsUi

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
@Parcelize
internal data class ProfileViewState(
    val myProfile: AppUserUi? = null,
    val myFriendRequest: FriendRequestsUi? = null,
) : BaseViewState

internal sealed class ProfileEvent : BaseEvent {
    data object Init : ProfileEvent()
    data object NavigateToFriends : ProfileEvent()
    data object NavigateToPrivacySettings : ProfileEvent()
    data object NavigateToGeneralSettings : ProfileEvent()
    data object NavigateToNotifySettings : ProfileEvent()
    data object NavigateToCalendarSettings : ProfileEvent()
    data object NavigateToPaymentsSettings : ProfileEvent()
    data object EditProfile : ProfileEvent()
    data object SignOut : ProfileEvent()
}

internal sealed class ProfileEffect : BaseUiEffect {
    data class ShowError(val failures: ProfileFailures) : ProfileEffect()
    data class PushGlobalScreen(val screen: Screen) : ProfileEffect()
    data class ReplaceGlobalScreen(val screen: Screen) : ProfileEffect()
}

internal sealed class ProfileAction : BaseAction {
    data class UpdateProfileInfo(val profile: AppUserUi?, val requests: FriendRequestsUi?) : ProfileAction()
}