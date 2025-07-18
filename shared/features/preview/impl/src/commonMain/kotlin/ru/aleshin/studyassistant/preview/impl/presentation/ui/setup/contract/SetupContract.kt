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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.contract

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import io.github.vinceglb.filekit.core.PlatformFile
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures
import ru.aleshin.studyassistant.preview.impl.presentation.models.organizations.OrganizationUi
import ru.aleshin.studyassistant.preview.impl.presentation.models.settings.CalendarSettingsUi
import ru.aleshin.studyassistant.preview.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.SetupPage

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
@Parcelize
@Immutable
internal data class SetupViewState(
    val profile: AppUserUi? = null,
    val isPaidUser: Boolean = false,
    val currentPage: SetupPage = SetupPage.PROFILE,
    val actionWithProfileAvatar: ActionWithAvatar = ActionWithAvatar.None(null),
    val organization: OrganizationUi? = null,
    val actionWithOrganizationAvatar: ActionWithAvatar = ActionWithAvatar.None(null),
    val calendarSettings: CalendarSettingsUi? = null,
) : BaseViewState

internal sealed class SetupEvent : BaseEvent {
    data object Init : SetupEvent()
    data object NavigateToBackPage : SetupEvent()
    data class UpdateProfile(val userProfile: AppUserUi) : SetupEvent()
    data class UpdateProfileAvatar(val image: PlatformFile) : SetupEvent()
    data object DeleteProfileAvatar : SetupEvent()
    data class UpdateOrganization(val organization: OrganizationUi) : SetupEvent()
    data class UpdateOrganizationAvatar(val image: PlatformFile) : SetupEvent()
    data object DeleteOrganizationAvatar : SetupEvent()
    data class UpdateCalendarSettings(val calendarSettings: CalendarSettingsUi) : SetupEvent()
    data object SaveProfileInfo : SetupEvent()
    data object SaveOrganizationInfo : SetupEvent()
    data object SaveCalendarInfo : SetupEvent()
    data object NavigateToWeekScheduleEditor : SetupEvent()
    data object NavigateToBilling : SetupEvent()
    data object NavigateToSchedule : SetupEvent()
    data object NavigateToBack : SetupEvent()
}

internal sealed class SetupEffect : BaseUiEffect {
    data class ShowError(val failures: PreviewFailures) : SetupEffect()
    data class NavigateToGlobalScreen(val pushScreen: Screen) : SetupEffect()
    data class ReplaceGlobalScreen(val screen: Screen) : SetupEffect()
    data object NavigateToBack : SetupEffect()
}

internal sealed class SetupAction : BaseAction {
    data class UpdatePage(val page: SetupPage) : SetupAction()
    data class UpdateAll(
        val profile: AppUserUi,
        val organization: OrganizationUi,
        val calendarSettings: CalendarSettingsUi
    ) : SetupAction()
    data class UpdateUserProfile(val profile: AppUserUi) : SetupAction()
    data class UpdateUserPaidStatus(val isPaid: Boolean) : SetupAction()
    data class UpdateActionWithProfileAvatar(val action: ActionWithAvatar) : SetupAction()
    data class UpdateOrganization(val organization: OrganizationUi) : SetupAction()
    data class UpdateActionWithOrganizationAvatar(val action: ActionWithAvatar) : SetupAction()
    data class UpdateCalendarSettings(val calendarSettings: CalendarSettingsUi) : SetupAction()
}