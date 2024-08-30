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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.calendar.contract

import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.settings.impl.domain.entities.SettingsFailures
import ru.aleshin.studyassistant.settings.impl.presentation.models.organizations.OrganizationShortUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.CalendarSettingsUi
import ru.aleshin.studyassistant.settings.impl.presentation.models.settings.HolidaysUi

/**
 * @author Stanislav Aleshin on 10.07.2024
 */
@Parcelize
internal data class CalendarViewState(
    val settings: CalendarSettingsUi? = null,
    val allOrganizations: List<OrganizationShortUi> = emptyList(),
) : BaseViewState

internal sealed class CalendarEvent : BaseEvent {
    data object Init : CalendarEvent()
    data class ChangeNumberOfRepeatWeek(val numberOfWeek: NumberOfRepeatWeek) : CalendarEvent()
    data class UpdateHolidays(val holidays: List<HolidaysUi>) : CalendarEvent()
}

internal sealed class CalendarEffect : BaseUiEffect {
    data class ShowError(val failures: SettingsFailures) : CalendarEffect()
}

internal sealed class CalendarAction : BaseAction {
    data class UpdateSettings(val settings: CalendarSettingsUi?) : CalendarAction()
    data class UpdateOrganizations(val organizations: List<OrganizationShortUi>) : CalendarAction()
}