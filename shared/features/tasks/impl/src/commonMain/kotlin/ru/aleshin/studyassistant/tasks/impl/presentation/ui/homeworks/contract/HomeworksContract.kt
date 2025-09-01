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

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.functional.TimeRange
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalCreateModelUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.DailyHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.AppUserUi

/**
 * @author Stanislav Aleshin on 27.06.2024
 */
@Serializable
internal data class HomeworksState(
    val isLoading: Boolean = true,
    val isPaidUser: Boolean = false,
    val currentDate: Instant = Clock.System.now().startThisDay(),
    val selectedTimeRange: TimeRange? = null,
    val activeSchedule: ScheduleUi? = null,
    val homeworks: Map<Instant, DailyHomeworksUi> = mapOf(),
    val friends: List<AppUserUi> = emptyList(),
) : StoreState

internal sealed class HomeworksEvent : StoreEvent {
    data class Started(val inputData: HomeworksInput, val isRestore: Boolean) : HomeworksEvent()
    data object ClickCurrentTimeRange : HomeworksEvent()
    data object ClickNextTimeRange : HomeworksEvent()
    data object ClickPreviousTimeRange : HomeworksEvent()
    data class DoHomework(val homework: HomeworkDetailsUi) : HomeworksEvent()
    data class RepeatHomework(val homework: HomeworkDetailsUi) : HomeworksEvent()
    data class SkipHomework(val homework: HomeworkDetailsUi) : HomeworksEvent()
    data class ShareHomeworks(val sentMediatedHomeworks: SentMediatedHomeworksDetailsUi) : HomeworksEvent()
    data class ScheduleGoal(val goalCreateModel: GoalCreateModelUi) : HomeworksEvent()
    data class DeleteGoal(val goal: GoalShortUi) : HomeworksEvent()
    data class ClickEditHomework(val homework: HomeworkDetailsUi) : HomeworksEvent()
    data class ClickAddHomework(val date: Instant) : HomeworksEvent()
    data object AddHomeworkInEditor : HomeworksEvent()
    data object ClickPaidFunction : HomeworksEvent()
    data object ClickBack : HomeworksEvent()
}

internal sealed class HomeworksEffect : StoreEffect {
    data class ShowError(val failures: TasksFailures) : HomeworksEffect()
    data class ScrollToDate(val targetDate: Instant) : HomeworksEffect()
}

internal sealed class HomeworksAction : StoreAction {
    data class UpdateHomeworks(val homeworks: Map<Instant, DailyHomeworksUi>) : HomeworksAction()
    data class UpdateActiveSchedule(val activeSchedule: ScheduleUi?) : HomeworksAction()
    data class UpdateDates(val currentDate: Instant, val selectedTimeRange: TimeRange?) : HomeworksAction()
    data class UpdateUserPaidStatus(val isPaidUser: Boolean) : HomeworksAction()
    data class UpdateFriends(val friends: List<AppUserUi>) : HomeworksAction()
    data class UpdateLoading(val isLoading: Boolean) : HomeworksAction()
}

internal data class HomeworksInput(
    val targetDate: Long?
) : BaseInput

internal sealed class HomeworksOutput : BaseOutput {
    data object NavigateToBack : HomeworksOutput()
    data object NavigateToBilling : HomeworksOutput()
    data class NavigateToHomeworkEditor(val config: EditorConfig.Homework) : HomeworksOutput()
}