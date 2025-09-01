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

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.schedule.impl.domain.entities.ScheduleFailures
import ru.aleshin.studyassistant.schedule.impl.presentation.models.analysis.DailyAnalysisUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ActiveClassUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.classes.ClassDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.homework.HomeworkDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.ScheduleDetailsUi

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
@Serializable
internal data class OverviewState(
    val isScheduleLoading: Boolean = true,
    val isAnalyticsLoading: Boolean = true,
    val currentDate: Instant = Clock.System.now().startThisDay(),
    val selectedDate: Instant? = null,
    val schedule: ScheduleDetailsUi? = null,
    val weekAnalysis: List<DailyAnalysisUi>? = null,
    val activeClass: ActiveClassUi? = null,
) : StoreState

internal sealed class OverviewEvent : StoreEvent {
    data object Started : OverviewEvent()
    data class SelectedDate(val date: Instant) : OverviewEvent()
    data object SelectedCurrentDay : OverviewEvent()
    data class ClickCompleteHomework(val homework: HomeworkDetailsUi) : OverviewEvent()
    data class ClickAgainHomework(val homework: HomeworkDetailsUi) : OverviewEvent()
    data class ClickEditHomework(val homework: HomeworkDetailsUi) : OverviewEvent()
    data class ClickAddHomework(val classModel: ClassDetailsUi, val date: Instant) : OverviewEvent()
    data object ClickEdit : OverviewEvent()
    data object ClickDetails : OverviewEvent()
}

internal sealed class OverviewEffect : StoreEffect {
    data class ShowError(val failures: ScheduleFailures) : OverviewEffect()
}

internal sealed class OverviewAction : StoreAction {
    data class UpdateSchedule(val schedule: ScheduleDetailsUi) : OverviewAction()
    data class UpdateAnalysis(val weekAnalysis: List<DailyAnalysisUi>) : OverviewAction()
    data class UpdateSelectedDate(val date: Instant) : OverviewAction()
    data class UpdateActiveClass(val activeClass: ActiveClassUi?) : OverviewAction()
    data class UpdateScheduleLoading(val isLoading: Boolean) : OverviewAction()
    data class UpdateAnalyticsLoading(val isLoading: Boolean) : OverviewAction()
}

internal sealed class OverviewOutput : BaseOutput {
    data class NavigateToHomeworkEditor(val config: EditorConfig.Homework) : OverviewOutput()
    data class NavigateToDailyScheduleEditor(val config: EditorConfig.DailySchedule) : OverviewOutput()
    data object NavigateToDetails : OverviewOutput()
}