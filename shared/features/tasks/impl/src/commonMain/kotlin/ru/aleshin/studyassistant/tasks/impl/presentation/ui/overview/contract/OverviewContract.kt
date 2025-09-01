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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.overview.contract

import androidx.compose.runtime.Immutable
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.domain.entities.goals.GoalTime
import ru.aleshin.studyassistant.core.domain.entities.organizations.Millis
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.DailyGoalsProgressUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalCreateModelUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.goals.GoalShortUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SentMediatedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.share.SharedHomeworksDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.DailyHomeworksUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.DetailsGroupedTodosUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkScopeUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworksCompleteProgressUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.users.AppUserUi

/**
 * @author Stanislav Aleshin on 27.06.2024
 */
@Serializable
@Immutable
internal data class OverviewState(
    val isLoadingHomeworks: Boolean = true,
    val isLoadingHomeworksProgress: Boolean = true,
    val isLoadingTasks: Boolean = true,
    val isLoadingShare: Boolean = true,
    val isLoadingGoals: Boolean = true,
    val isPaidUser: Boolean = false,
    val currentDate: Instant = Clock.System.now().startThisDay(),
    val activeSchedule: ScheduleUi? = null,
    val selectedGoalsDate: Instant = currentDate,
    val dailyGoals: List<GoalDetailsUi> = emptyList(),
    val goalsProgress: Map<Instant, DailyGoalsProgressUi> = emptyMap(),
    val homeworks: Map<Instant, DailyHomeworksUi> = emptyMap(),
    val friends: List<AppUserUi> = emptyList(),
    val sharedHomeworks: SharedHomeworksDetailsUi? = null,
    val groupedTodos: DetailsGroupedTodosUi? = null,
    val homeworksScope: HomeworkScopeUi? = null,
    val homeworksProgress: HomeworksCompleteProgressUi? = null,
) : StoreState

internal sealed class OverviewEvent : StoreEvent {
    data object Init : OverviewEvent()
    data class DoHomework(val homework: HomeworkUi) : OverviewEvent()
    data class RepeatHomework(val homework: HomeworkUi) : OverviewEvent()
    data class SkipHomework(val homework: HomeworkUi) : OverviewEvent()
    data class UpdateTodoDone(val todo: TodoDetailsUi, val isDone: Boolean) : OverviewEvent()
    data class ShareHomeworks(val sentMediatedHomeworks: SentMediatedHomeworksDetailsUi) : OverviewEvent()
    data class SetNewGoalNumbers(val goals: List<GoalDetailsUi>) : OverviewEvent()
    data class SelectedGoalsDate(val date: Instant) : OverviewEvent()
    data class CompleteGoal(val goal: GoalDetailsUi) : OverviewEvent()
    data class DeleteGoal(val goal: GoalShortUi) : OverviewEvent()
    data class ClickStartGoalTime(val goal: GoalDetailsUi) : OverviewEvent()
    data class ClickPauseGoalTime(val goal: GoalDetailsUi) : OverviewEvent()
    data class ClickResetGoalTime(val goal: GoalDetailsUi) : OverviewEvent()
    data class ChangeGoalTimeType(val goal: GoalDetailsUi, val type: GoalTime.Type) : OverviewEvent()
    data class ChangeGoalDesiredTime(val goal: GoalDetailsUi, val time: Millis?) : OverviewEvent()
    data class ScheduleGoal(val createModel: GoalCreateModelUi) : OverviewEvent()
    data class ClickEditHomework(val homework: HomeworkUi) : OverviewEvent()
    data class ClickEditTodo(val todo: TodoUi?) : OverviewEvent()
    data object AddHomeworkInEditor : OverviewEvent()
    data class ClickHomework(val homework: HomeworkUi?) : OverviewEvent()
    data object ClickShowAllSharedHomeworks : OverviewEvent()
    data object ClickShowAllTodo : OverviewEvent()
    data object ClickPaidFunction : OverviewEvent()
}

internal sealed class OverviewEffect : StoreEffect {
    data class ShowError(val failures: TasksFailures) : OverviewEffect()
}

internal sealed class OverviewAction : StoreAction {
    data class UpdateHomeworks(
        val homeworks: Map<Instant, DailyHomeworksUi>,
        val homeworkScope: HomeworkScopeUi,
    ) : OverviewAction()
    data class UpdateTodos(
        val groupedTodos: DetailsGroupedTodosUi,
    ) : OverviewAction()
    data class UpdateGoals(
        val selectedGoalsDate: Instant,
        val dailyGoals: List<GoalDetailsUi>,
        val goalsProgress: Map<Instant, DailyGoalsProgressUi>,
    ) : OverviewAction()
    data class UpdateSharedHomeworks(
        val homeworks: SharedHomeworksDetailsUi,
        val friends: List<AppUserUi>,
    ) : OverviewAction()

    data class UpdateHomeworksProgress(val homeworkProgress: HomeworksCompleteProgressUi?) : OverviewAction()
    data class UpdateActiveSchedule(val activeSchedule: ScheduleUi?) : OverviewAction()
    data class UpdateCurrentDate(val date: Instant) : OverviewAction()
    data class UpdateUserPaidStatus(val isPaidUser: Boolean) : OverviewAction()
    data class UpdateHomeworksLoading(val isLoading: Boolean) : OverviewAction()
    data class UpdateHomeworksProgressLoading(val isLoading: Boolean) : OverviewAction()
    data class UpdateTasksLoading(val isLoading: Boolean) : OverviewAction()
    data class UpdateShareLoading(val isLoading: Boolean) : OverviewAction()
    data class UpdateGoalsLoading(val isLoading: Boolean) : OverviewAction()
}

internal sealed class OverviewOutput : BaseOutput {
    data class NavigateToHomeworkEditor(val config: EditorConfig.Homework) : OverviewOutput()
    data class NavigateToTodoEditor(val config: EditorConfig.Todo) : OverviewOutput()
    data class NavigateToHomeworks(val targetDate: Long?) : OverviewOutput()
    data object NavigateToBilling : OverviewOutput()
    data object NavigateToTodo : OverviewOutput()
    data object NavigateToShareHomeworks : OverviewOutput()
}