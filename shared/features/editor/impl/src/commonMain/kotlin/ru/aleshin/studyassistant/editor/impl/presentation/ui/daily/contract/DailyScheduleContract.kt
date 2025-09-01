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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.daily.contract

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.FastEditDurations
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.CustomScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.settings.CalendarSettingsUi

/**
 * @author Stanislav Aleshin on 14.07.2024
 */
@Serializable
internal data class DailyScheduleState(
    val isLoading: Boolean = true,
    val targetDate: Instant? = null,
    val customSchedule: CustomScheduleUi? = null,
    val baseSchedule: BaseScheduleUi? = null,
    val calendarSettings: CalendarSettingsUi? = null,
) : StoreState

internal sealed class DailyScheduleEvent : StoreEvent {
    data class Started(val inputData: DailyScheduleInput) : DailyScheduleEvent()
    data object CreateCustomSchedule : DailyScheduleEvent()
    data object DeleteCustomSchedule : DailyScheduleEvent()
    data class DeleteClass(val targetId: UID) : DailyScheduleEvent()
    data class SwapClasses(val from: ClassUi, val to: ClassUi) : DailyScheduleEvent()
    data class FastEditStartOfDay(val time: Instant) : DailyScheduleEvent()
    data class FastEditClassesDuration(val durations: FastEditDurations) : DailyScheduleEvent()
    data class FastEditBreaksDuration(val durations: FastEditDurations) : DailyScheduleEvent()
    data class EditClassInEditor(val editClass: ClassUi) : DailyScheduleEvent()
    data object CreateClassInEditor : DailyScheduleEvent()
    data object NavigateToBack : DailyScheduleEvent()
}

internal sealed class DailyScheduleEffect : StoreEffect {
    data class ShowError(val failures: EditorFailures) : DailyScheduleEffect()
}

internal sealed class DailyScheduleAction : StoreAction {

    data class UpdateSchedules(
        val baseSchedule: BaseScheduleUi?,
        val customSchedule: CustomScheduleUi?,
    ) : DailyScheduleAction()

    data class UpdateTargetDate(val date: Instant) : DailyScheduleAction()
    data class UpdateCalendarSettings(val settings: CalendarSettingsUi) : DailyScheduleAction()
    data class UpdateLoading(val isLoading: Boolean) : DailyScheduleAction()
}

internal data class DailyScheduleInput(
    val date: Long,
    val baseScheduleId: UID?,
    val customScheduleId: UID?,
) : BaseInput

internal sealed class DailyScheduleOutput : BaseOutput {
    data object NavigateToBack : DailyScheduleOutput()
    data class NavigateToClassEditor(val config: EditorConfig.Class) : DailyScheduleOutput()
    data class NavigateToOrganizationEditor(val config: EditorConfig.Organization) : DailyScheduleOutput()
}