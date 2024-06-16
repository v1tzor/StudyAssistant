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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import functional.TimeRange
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import platform.InstantParceler
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.ScheduleViewType
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.WeekScheduleDetailsUi

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
@Parcelize
internal data class DetailsViewState(
    val isLoading: Boolean = true,
    @TypeParceler<Instant, InstantParceler>
    val currentDate: Instant = Clock.System.now(),
    val weekSchedule: WeekScheduleDetailsUi? = null,
    val selectedWeek: TimeRange? = null,
    val activeClass: ActiveClassUi? = null,
    val scheduleView: ScheduleViewType = ScheduleViewType.COMMON,
) : BaseViewState

internal sealed class DetailsEvent : BaseEvent {
    data object Init : DetailsEvent()
    data object SelectedNextWeek : DetailsEvent()
    data object SelectedCurrentWeek : DetailsEvent()
    data object SelectedPreviousWeek : DetailsEvent()
    data class OpenOverviewSchedule(val date: Instant) : DetailsEvent()
    data class SelectedViewType(val scheduleView: ScheduleViewType) : DetailsEvent()
    data object NavigateToOverview : DetailsEvent()
    data object NavigateToEditor : DetailsEvent()
}

internal sealed class DetailsEffect : BaseUiEffect {
    data class ShowError(val failures: ScheduleFailures) : DetailsEffect()
    data class NavigateToLocal(val pushScreen: Screen) : DetailsEffect()
    data class NavigateToGlobal(val pushScreen: Screen) : DetailsEffect()
}

internal sealed class DetailsAction : BaseAction {
    data class UpdateWeekSchedule(val schedule: WeekScheduleDetailsUi) : DetailsAction()
    data class UpdateSelectedWeek(val week: TimeRange?) : DetailsAction()
    data class UpdateActiveClass(val activeClass: ActiveClassUi?) : DetailsAction()
    data class UpdateViewType(val scheduleView: ScheduleViewType) : DetailsAction()
    data class UpdateLoading(val isLoading: Boolean) : DetailsAction()
}