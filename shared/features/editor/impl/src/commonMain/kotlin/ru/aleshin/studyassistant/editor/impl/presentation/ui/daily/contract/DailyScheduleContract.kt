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

import androidx.compose.runtime.Immutable
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.functional.UID
import ru.aleshin.studyassistant.core.common.platform.NullInstantParceler
import ru.aleshin.studyassistant.editor.impl.domain.entities.EditorFailures
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.ClassUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.classes.FastEditDurations
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.BaseScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.schedules.CustomScheduleUi
import ru.aleshin.studyassistant.editor.impl.presentation.models.settings.CalendarSettingsUi

/**
 * @author Stanislav Aleshin on 14.07.2024
 */
@Immutable
@Parcelize
internal data class DailyScheduleViewState(
    val isLoading: Boolean = true,
    @TypeParceler<Instant?, NullInstantParceler>
    val targetDate: Instant? = null,
    val customSchedule: CustomScheduleUi? = null,
    val baseSchedule: BaseScheduleUi? = null,
    val calendarSettings: CalendarSettingsUi? = null,
) : BaseViewState

internal sealed class DailyScheduleEvent : BaseEvent {
    data class Init(val date: Long, val baseScheduleId: UID?, val customScheduleId: UID?) : DailyScheduleEvent()
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

internal sealed class DailyScheduleEffect : BaseUiEffect {
    data class ShowError(val failures: EditorFailures) : DailyScheduleEffect()
    data class NavigateToLocal(val pushScreen: Screen) : DailyScheduleEffect()
    data object NavigateToBack : DailyScheduleEffect()
}

internal sealed class DailyScheduleAction : BaseAction {

    data class UpdateSchedules(
        val baseSchedule: BaseScheduleUi?,
        val customSchedule: CustomScheduleUi?,
    ) : DailyScheduleAction()

    data class UpdateTargetDate(val date: Instant) : DailyScheduleAction()
    data class UpdateCalendarSettings(val settings: CalendarSettingsUi) : DailyScheduleAction()
    data class UpdateLoading(val isLoading: Boolean) : DailyScheduleAction()
}