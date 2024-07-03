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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract

import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import extensions.startThisDay
import functional.TimeRange
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import platform.InstantParceler
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi

/**
 * @author Stanislav Aleshin on 27.06.2024
 */
@Parcelize
internal data class HomeworksViewState(
    val isLoading: Boolean = true,
    @TypeParceler<Instant, InstantParceler>
    val currentDate: Instant = Clock.System.now().startThisDay(),
    val selectedTimeRange: TimeRange? = null,
    val activeSchedule: ScheduleUi? = null,
    @TypeParceler<Instant, InstantParceler>
    val homeworks: Map<Instant, List<HomeworkDetailsUi>> = mapOf(),
) : BaseViewState

internal sealed class HomeworksEvent : BaseEvent {
    data object Init : HomeworksEvent()
    data object Refresh : HomeworksEvent()
    data object NextTimeRange : HomeworksEvent()
    data object PreviousTimeRange : HomeworksEvent()
    data class DoHomework(val homework: HomeworkDetailsUi) : HomeworksEvent()
    data class RepeatHomework(val homework: HomeworkDetailsUi) : HomeworksEvent()
    data class SkipHomework(val homework: HomeworkDetailsUi) : HomeworksEvent()
    data class NavigateToHomeworkEditor(val homework: HomeworkDetailsUi) : HomeworksEvent()
    data object AddHomeworkInEditor : HomeworksEvent()
    data object NavigateToOverview : HomeworksEvent()
}

internal sealed class HomeworksEffect : BaseUiEffect {
    data class ShowError(val failures: TasksFailures) : HomeworksEffect()
    data class NavigateToLocal(val pushScreen: Screen) : HomeworksEffect()
    data class NavigateToGlobal(val pushScreen: Screen) : HomeworksEffect()
    data object NavigateToBack : HomeworksEffect()
}

internal sealed class HomeworksAction : BaseAction {
    data class UpdateHomeworks(val homeworks: Map<Instant, List<HomeworkDetailsUi>>) : HomeworksAction()
    data class UpdateActiveSchedule(val activeSchedule: ScheduleUi?) : HomeworksAction()
    data class UpdateDates(val currentDate: Instant, val selectedTimeRange: TimeRange?) : HomeworksAction()
    data class UpdateLoading(val isLoading: Boolean) : HomeworksAction()
}