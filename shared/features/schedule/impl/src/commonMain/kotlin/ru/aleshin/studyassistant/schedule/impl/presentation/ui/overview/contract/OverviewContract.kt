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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import extensions.startThisDay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import platform.InstantParceler
import platform.NullInstantParceler
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.presentation.models.analysis.DailyAnalysisUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.ScheduleDetailsUi

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
@Parcelize
internal data class OverviewViewState(
    val isScheduleLoading: Boolean = true,
    val isAnalyticsLoading: Boolean = true,
    @TypeParceler<Instant, InstantParceler>
    val currentDate: Instant = Clock.System.now().startThisDay(),
    @TypeParceler<Instant?, NullInstantParceler>
    val selectedDate: Instant? = null,
    val schedule: ScheduleDetailsUi? = null,
    val weekAnalysis: List<DailyAnalysisUi>? = null,
    val activeClass: ActiveClassUi? = null,
) : BaseViewState

internal sealed class OverviewEvent : BaseEvent {
    data class Init(val firstDay: Instant?) : OverviewEvent()
    data class SelectedDate(val date: Instant) : OverviewEvent()
    data object SelectedCurrentDay : OverviewEvent()
    data object NavigateToDetails : OverviewEvent()
}

internal sealed class OverviewEffect : BaseUiEffect {
    data class ShowError(val failures: ScheduleFailures) : OverviewEffect()
    data class NavigateToLocal(val pushScreen: Screen) : OverviewEffect()
    data class NavigateToGlobal(val pushScreen: Screen) : OverviewEffect()
}

internal sealed class OverviewAction : BaseAction {
    data class UpdateSchedule(val schedule: ScheduleDetailsUi) : OverviewAction()
    data class UpdateAnalysis(val weekAnalysis: List<DailyAnalysisUi>) : OverviewAction()
    data class UpdateSelectedDate(val date: Instant) : OverviewAction()
    data class UpdateActiveClass(val activeClass: ActiveClassUi?) : OverviewAction()
    data class UpdateScheduleLoading(val isLoading: Boolean) : OverviewAction()
    data class UpdateAnalyticsLoading(val isLoading: Boolean) : OverviewAction()
}