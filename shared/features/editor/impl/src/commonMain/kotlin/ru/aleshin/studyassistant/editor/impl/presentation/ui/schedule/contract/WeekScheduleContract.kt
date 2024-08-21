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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.editor.api.presentation.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseWeekScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.settings.CalendarSettingsUi

/**
 * @author Stanislav Aleshin on 05.05.2024
 */
@Immutable
@Parcelize
internal data class WeekScheduleViewState(
    val isLoading: Boolean = true,
    val weekSchedule: BaseWeekScheduleUi? = null,
    val selectedWeek: NumberOfRepeatWeek = NumberOfRepeatWeek.ONE,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val calendarSettings: CalendarSettingsUi? = null,
) : BaseViewState

internal sealed class WeekScheduleEvent : BaseEvent {
    data class Init(val week: NumberOfRepeatWeek) : WeekScheduleEvent()
    data object Refresh : WeekScheduleEvent()
    data class ChangeWeek(val numberOfWeek: NumberOfRepeatWeek) : WeekScheduleEvent()
    data class UpdateOrganization(val organization: OrganizationShortUi) : WeekScheduleEvent()
    data class DeleteClass(val targetId: UID, val schedule: BaseScheduleUi) : WeekScheduleEvent()
    data class EditClassInEditor(val editClass: ClassUi, val weekDay: DayOfNumberedWeekUi) : WeekScheduleEvent()
    data class CreateClassInEditor(val weekDay: DayOfNumberedWeekUi, val schedule: BaseScheduleUi?) : WeekScheduleEvent()
    data object NavigateToOrganizationEditor : WeekScheduleEvent()
    data object NavigateToBack : WeekScheduleEvent()
}

internal sealed class WeekScheduleEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : WeekScheduleEffect()
    data class NavigateToLocal(val pushScreen: Screen) : WeekScheduleEffect()
    data object NavigateToBack : WeekScheduleEffect()
}

internal sealed class WeekScheduleAction : BaseAction {
    data class UpdateScheduleData(
        val week: NumberOfRepeatWeek,
        val schedule: BaseWeekScheduleUi
    ) : WeekScheduleAction()

    data class UpdateOrganizationData(
        val organizations: List<OrganizationShortUi>,
        val settings: CalendarSettingsUi?,
    ) : WeekScheduleAction()

    data class UpdateSelectedWeek(val week: NumberOfRepeatWeek) : WeekScheduleAction()

    data class UpdateLoading(val isLoading: Boolean) : WeekScheduleAction()
}