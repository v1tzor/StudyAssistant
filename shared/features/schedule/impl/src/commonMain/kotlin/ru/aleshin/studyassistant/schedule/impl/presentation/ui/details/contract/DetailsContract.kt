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

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.core.domain.entities.settings.WeekScheduleViewType
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.WeekScheduleDetailsUi

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
@Serializable
internal data class DetailsState(
    val isLoading: Boolean = true,
    val currentDate: Instant = Clock.System.now(),
    val weekSchedule: WeekScheduleDetailsUi? = null,
    val selectedWeek: TimeRange? = null,
    val activeClass: ActiveClassUi? = null,
    val scheduleView: WeekScheduleViewType = WeekScheduleViewType.COMMON,
) : StoreState

internal sealed class DetailsEvent : StoreEvent {
    data object Started : DetailsEvent()
    data object SelectedNextWeek : DetailsEvent()
    data object SelectedCurrentWeek : DetailsEvent()
    data object SelectedPreviousWeek : DetailsEvent()
    data class SelectedViewType(val scheduleView: WeekScheduleViewType) : DetailsEvent()
    data class CompleteHomework(val homework: HomeworkDetailsUi) : DetailsEvent()
    data class ClickAgainHomework(val homework: HomeworkDetailsUi) : DetailsEvent()
    data class ClickEditHomework(val homework: HomeworkDetailsUi) : DetailsEvent()
    data class ClickAddHomework(val classModel: ClassDetailsUi, val date: Instant) : DetailsEvent()
    data object ClickOverview : DetailsEvent()
    data object ClickEdit : DetailsEvent()
}

internal sealed class DetailsEffect : StoreEffect {
    data class ShowError(val failures: ScheduleFailures) : DetailsEffect()
}

internal sealed class DetailsAction : StoreAction {
    data class UpdateWeekSchedule(val schedule: WeekScheduleDetailsUi) : DetailsAction()
    data class UpdateSelectedWeek(val week: TimeRange?) : DetailsAction()
    data class UpdateActiveClass(val activeClass: ActiveClassUi?) : DetailsAction()
    data class UpdateViewType(val scheduleView: WeekScheduleViewType) : DetailsAction()
    data class UpdateLoading(val isLoading: Boolean) : DetailsAction()
}

internal sealed class DetailsOutput : BaseOutput {
    data object NavigateToOverview : DetailsOutput()
    data class NavigateToWeekScheduleEditor(val config: EditorConfig.WeekSchedule) : DetailsOutput()
    data class NavigateToHomeworkEditor(val config: EditorConfig.Homework) : DetailsOutput()
}