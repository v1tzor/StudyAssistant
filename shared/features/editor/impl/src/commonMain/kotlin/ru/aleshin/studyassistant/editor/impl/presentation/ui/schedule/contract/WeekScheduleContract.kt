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

import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.editor.api.DayOfNumberedWeekUi
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.orgnizations.OrganizationShortUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseWeekScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.settings.CalendarSettingsUi

/**
 * @author Stanislav Aleshin on 05.05.2024
 */
@Serializable
internal data class WeekScheduleState(
    val isLoading: Boolean = true,
    val weekSchedule: BaseWeekScheduleUi? = null,
    val selectedWeek: NumberOfRepeatWeek = NumberOfRepeatWeek.ONE,
    val organizations: List<OrganizationShortUi> = emptyList(),
    val calendarSettings: CalendarSettingsUi? = null,
) : StoreState

internal sealed class WeekScheduleEvent : StoreEvent {
    data class Started(val inputData: WeekScheduleInput, val isRestore: Boolean) : WeekScheduleEvent()
    data object Refresh : WeekScheduleEvent()
    data class ChangeWeek(val numberOfWeek: NumberOfRepeatWeek) : WeekScheduleEvent()
    data class UpdateOrganization(val organization: OrganizationShortUi) : WeekScheduleEvent()
    data class DeleteClass(val targetId: UID, val schedule: BaseScheduleUi) : WeekScheduleEvent()
    data class EditClassInEditor(val editClass: ClassUi, val weekDay: DayOfNumberedWeekUi) : WeekScheduleEvent()
    data class CreateClassInEditor(val weekDay: DayOfNumberedWeekUi, val schedule: BaseScheduleUi?) : WeekScheduleEvent()
    data object NavigateToOrganizationEditor : WeekScheduleEvent()
    data object NavigateToBack : WeekScheduleEvent()
}

internal sealed class WeekScheduleEffect : StoreEffect {
    data class ShowError(val failures: EditorFailures) : WeekScheduleEffect()
}

internal sealed class WeekScheduleAction : StoreAction {
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

internal data class WeekScheduleInput(val week: NumberOfRepeatWeek) : BaseInput

internal sealed class WeekScheduleOutput : BaseOutput {
    data object NavigateToBack : WeekScheduleOutput()
    data class NavigateToClassEditor(val config: EditorConfig.Class) : WeekScheduleOutput()
    data class NavigateToOrganizationEditor(val config: EditorConfig.Organization) : WeekScheduleOutput()
}