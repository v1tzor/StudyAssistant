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
import ru.aleshin.studyassistant.profile.impl.domain.entities.ProfileFailures
import ru.aleshin.studyassistant.profile.impl.presentation.models.organization.OrganizationShortUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.SentMediatedSchedulesUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.ShareSchedulesSendDataUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.shared.SharedSchedulesShortUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.profile.impl.presentation.models.users.FriendRequestsUi

/**
 * @author Stanislav Aleshin on 21.04.2024
 */
@Parcelize
internal data class ProfileViewState(
    val isLoading: Boolean = true,
    val isLoadingShare: Boolean = true,
    val isLoadingSend: Boolean = false,
    @TypeParceler<Instant, InstantParceler>
    val currentTime: Instant = Clock.System.now(),
    val appUserProfile: AppUserUi? = null,
    val friendRequest: FriendRequestsUi? = null,
    val sharedSchedules: SharedSchedulesShortUi? = null,
    val allOrganizations: List<OrganizationShortUi> = emptyList(),
    val allFriends: List<AppUserUi> = emptyList(),
) : BaseViewState

internal sealed class ProfileEvent : BaseEvent {
    data object Init : ProfileEvent()
    data object NavigateToFriends : ProfileEvent()
    data class SendSharedSchedule(val sendData: ShareSchedulesSendDataUi) : ProfileEvent()
    data class CancelSentSchedule(val schedule: SentMediatedSchedulesUi) : ProfileEvent()
    data class NavigateToShareScheduleViewer(val shareId: UID) : ProfileEvent()
    data object NavigateToPrivacySettings : ProfileEvent()
    data object NavigateToGeneralSettings : ProfileEvent()
    data object NavigateToNotifySettings : ProfileEvent()
    data object NavigateToCalendarSettings : ProfileEvent()
    data object NavigateToPaymentsSettings : ProfileEvent()
    data object NavigateToProfileEditor : ProfileEvent()
    data object SignOut : ProfileEvent()
}

internal sealed class ProfileEffect : BaseUiEffect {
    data class ShowError(val failures: ProfileFailures) : ProfileEffect()
    data class PushGlobalScreen(val screen: Screen) : ProfileEffect()
    data class ReplaceGlobalScreen(val screen: Screen) : ProfileEffect()
}

internal sealed class ProfileAction : BaseAction {

    data class UpdateProfileInfo(
        val profile: AppUserUi?,
        val requests: FriendRequestsUi?,
    ) : ProfileAction()

    data class UpdateSharedSchedules(
        val schedules: SharedSchedulesShortUi?,
        val organizations: List<OrganizationShortUi>
    ) : ProfileAction()

    data class UpdateFriends(val friends: List<AppUserUi>) : ProfileAction()
    data class UpdateCurrentTime(val time: Instant) : ProfileAction()
    data class UpdateLoading(val isLoading: Boolean) : ProfileAction()
    data class UpdateLoadingShared(val isLoading: Boolean) : ProfileAction()
    data class UpdateLoadingSend(val isLoading: Boolean) : ProfileAction()
}