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

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import functional.UID
import ru.aleshin.studyassistant.preview.impl.domain.entities.PreviewFailures
import ru.aleshin.studyassistant.preview.impl.presentation.models.AppUserUi
import ru.aleshin.studyassistant.preview.impl.presentation.models.CalendarSettingsUi
import ru.aleshin.studyassistant.preview.impl.presentation.models.OrganizationUi
import ru.aleshin.studyassistant.preview.impl.presentation.ui.setup.views.SetupPage

/**
 * @author Stanislav Aleshin on 17.04.2024
 */
internal data class SetupViewState(
    // TODO: Not serializable
    val currentPage: SetupPage = SetupPage.PROFILE,
    val profile: AppUserUi? = null,
    val organization: OrganizationUi? = null,
    val calendarSettings: CalendarSettingsUi? = null,
) : BaseViewState

internal sealed class SetupEvent : BaseEvent {
    data class Init(val createdUserId: UID) : SetupEvent()
    data object NavigateToBackPage : SetupEvent()
    data class UpdateProfile(val userProfile: AppUserUi) : SetupEvent()
    data class UpdateOrganization(val organization: OrganizationUi) : SetupEvent()
    data class UpdateCalendarSettings(val calendarSettings: CalendarSettingsUi) : SetupEvent()
    data object SaveProfileInfo : SetupEvent()
    data object SaveOrganizationInfo : SetupEvent()
    data object SaveCalendarInfo : SetupEvent()
    data object NavigateToScheduleEditor : SetupEvent()
}

internal sealed class SetupEffect : BaseUiEffect {
    data class ShowError(val failures: PreviewFailures) : SetupEffect()
    data class ReplaceGlobalScreen(val screen: Screen) : SetupEffect()
}

internal sealed class SetupAction : BaseAction {
    data class UpdatePage(val page: SetupPage) : SetupAction()
    data class UpdateAll(val profile: AppUserUi, val organization: OrganizationUi,  val calendarSettings: CalendarSettingsUi) : SetupAction()
    data class UpdateProfileInfo(val profile: AppUserUi) : SetupAction()
    data class UpdateOrganizationInfo(val organization: OrganizationUi) : SetupAction()
    data class UpdateCalendarSettings( val calendarSettings: CalendarSettingsUi) : SetupAction()
}