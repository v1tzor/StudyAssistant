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

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import entities.common.NumberOfRepeatWeek
import functional.UID
import ru.aleshin.studyassistant.editor.api.ui.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseWeekScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.settings.CalendarSettingsUi

/**
 * @author Stanislav Aleshin on 05.05.2024
 */
@Parcelize
internal data class ScheduleEditorViewState(
    val isLoading: Boolean = true,
    val weekSchedule: BaseWeekScheduleUi? = null,
    val selectedWeek: NumberOfRepeatWeek = NumberOfRepeatWeek.ONE,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val calendarSettings: CalendarSettingsUi? = null,
) : BaseViewState

internal sealed class ScheduleEditorEvent : BaseEvent {
    data class Init(val week: NumberOfRepeatWeek) : ScheduleEditorEvent()
    data object Refresh : ScheduleEditorEvent()
    data class ChangeWeek(val numberOfWeek: NumberOfRepeatWeek) : ScheduleEditorEvent()
    data class UpdateOrganization(val organization: OrganizationShortUi) : ScheduleEditorEvent()
    data class CreateClass(val weekDay: DayOfNumberedWeekUi, val schedule: BaseScheduleUi?) : ScheduleEditorEvent()
    data class EditClass(val editClass: ClassUi, val weekDay: DayOfNumberedWeekUi) : ScheduleEditorEvent()
    data class DeleteClass(val uid: UID, val schedule: BaseScheduleUi) : ScheduleEditorEvent()
    data object SaveSchedule : ScheduleEditorEvent()
}

internal sealed class ScheduleEditorEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : ScheduleEditorEffect()
    data class NavigateToLocal(val pushScreen: Screen) : ScheduleEditorEffect()
    data object NavigateToBack : ScheduleEditorEffect()
}

internal sealed class ScheduleEditorAction : BaseAction {
    data class UpdateLoading(val isLoading: Boolean) : ScheduleEditorAction()
    data class UpdateSelectedWeek(val week: NumberOfRepeatWeek) : ScheduleEditorAction()
    data class UpdateScheduleData(
        val week: NumberOfRepeatWeek,
        val schedule: BaseWeekScheduleUi
    ) : ScheduleEditorAction()
    data class UpdateOrganizationData(
        val organizations: List<OrganizationShortUi>,
        val settings: CalendarSettingsUi?,
    ) : ScheduleEditorAction()
}