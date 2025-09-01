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

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
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
@Serializable
internal data class ProfileState(
    val isLoading: Boolean = true,
    val isLoadingShare: Boolean = true,
    val isLoadingSend: Boolean = false,
    val currentTime: Instant = Clock.System.now(),
    val appUserProfile: AppUserUi? = null,
    val friendRequest: FriendRequestsUi? = null,
    val sharedSchedules: SharedSchedulesShortUi? = null,
    val allOrganizations: List<OrganizationShortUi> = emptyList(),
    val allFriends: List<AppUserUi> = emptyList(),
) : StoreState

internal sealed class ProfileEvent : StoreEvent {
    data object Started : ProfileEvent()
    data object ClickFriends : ProfileEvent()
    data class SendSharedSchedule(val sendData: ShareSchedulesSendDataUi) : ProfileEvent()
    data class CancelSentSchedule(val schedule: SentMediatedSchedulesUi) : ProfileEvent()
    data class ClickSharedSchedule(val shareId: UID) : ProfileEvent()
    data object ClickAboutApp : ProfileEvent()
    data object ClickGeneralSettings : ProfileEvent()
    data object ClickNotifySettings : ProfileEvent()
    data object ClickCalendarSettings : ProfileEvent()
    data object ClickPaymentsSettings : ProfileEvent()
    data object ClickEditProfile : ProfileEvent()
    data object ClickSignOut : ProfileEvent()
}

internal sealed class ProfileEffect : StoreEffect {
    data class ShowError(val failures: ProfileFailures) : ProfileEffect()
}

internal sealed class ProfileAction : StoreAction {

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