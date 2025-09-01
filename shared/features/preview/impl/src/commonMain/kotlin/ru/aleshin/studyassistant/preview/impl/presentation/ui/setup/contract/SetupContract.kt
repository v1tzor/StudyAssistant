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

import io.github.vinceglb.filekit.core.PlatformFile
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.ui.models.ActionWithAvatar
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures
import ru.aleshin.studyassistant.preview.impl.presentation.models.organizations.OrganizationUi
import ru.aleshin.studyassistant.preview.impl.presentation.models.settings.CalendarSettingsUi
import ru.aleshin.studyassistant.preview.impl.presentation.models.users.AppUserUi
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.SetupPage

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
@Serializable
internal data class SetupState(
    val profile: AppUserUi? = null,
    val isPaidUser: Boolean = false,
    val currentPage: SetupPage = SetupPage.PROFILE,
    val actionWithProfileAvatar: ActionWithAvatar = ActionWithAvatar.None(null),
    val organization: OrganizationUi? = null,
    val actionWithOrganizationAvatar: ActionWithAvatar = ActionWithAvatar.None(null),
    val calendarSettings: CalendarSettingsUi? = null,
) : StoreState

internal sealed class SetupEvent : StoreEvent {
    data class Started(val isRestore: Boolean) : SetupEvent()
    data object ClickBackPage : SetupEvent()
    data class UpdateProfile(val userProfile: AppUserUi) : SetupEvent()
    data class UpdateProfileAvatar(val image: PlatformFile) : SetupEvent()
    data object DeleteProfileAvatar : SetupEvent()
    data class UpdateOrganization(val organization: OrganizationUi) : SetupEvent()
    data class UpdateOrganizationAvatar(val image: PlatformFile) : SetupEvent()
    data object DeleteOrganizationAvatar : SetupEvent()
    data class UpdateCalendarSettings(val calendarSettings: CalendarSettingsUi) : SetupEvent()
    data object ClickSaveProfileInfo : SetupEvent()
    data object ClickSaveOrganizationInfo : SetupEvent()
    data object ClickSaveCalendarInfo : SetupEvent()
    data object ClickEditWeekSchedule : SetupEvent()
    data object ClickPaidFunction : SetupEvent()
    data object ClickGoToApp : SetupEvent()
    data object ClickBack : SetupEvent()
}

internal sealed class SetupEffect : StoreEffect {
    data class ShowError(val failures: PreviewFailures) : SetupEffect()
}

internal sealed class SetupAction : StoreAction {
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

internal sealed class SetupOutput : BaseOutput {
    data object NavigateToBack : SetupOutput()
    data object NavigateToApp : SetupOutput()
    data object NavigateToBilling : SetupOutput()
    data object NavigateToWeekScheduleEditor : SetupOutput()
}