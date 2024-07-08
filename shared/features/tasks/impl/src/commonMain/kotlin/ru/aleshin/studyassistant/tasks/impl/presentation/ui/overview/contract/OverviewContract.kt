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
import cafe.adriel.voyager.core.screen.Screen
import dev.icerock.moko.parcelize.Parcelize
import dev.icerock.moko.parcelize.TypeParceler
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.platform.InstantParceler
import ru.aleshin.studyassistant.tasks.impl.domain.entities.TasksFailures
import ru.aleshin.studyassistant.tasks.impl.presentation.models.schedules.ScheduleUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkErrorsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.HomeworkScopeUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoDetailsUi
import ru.aleshin.studyassistant.tasks.impl.presentation.models.tasks.TodoErrorsUi

/**
 * @author Stanislav Aleshin on 27.06.2024
 */
@Immutable
@Parcelize
internal data class OverviewViewState(
    val isLoadingHomeworks: Boolean = true,
    val isLoadingErrors: Boolean = true,
    val isLoadingTasks: Boolean = true,
    @TypeParceler<Instant, InstantParceler>
    val currentDate: Instant = Clock.System.now().startThisDay(),
    val activeSchedule: ScheduleUi? = null,
    @TypeParceler<Instant, InstantParceler>
    val homeworks: Map<Instant, List<HomeworkDetailsUi>> = mapOf(),
    val homeworksScope: HomeworkScopeUi? = null,
    val homeworkErrors: HomeworkErrorsUi? = null,
    val todoErrors: TodoErrorsUi? = null,
    val todos: List<TodoDetailsUi> = emptyList(),
) : BaseViewState

internal sealed class OverviewEvent : BaseEvent {
    data object Init : OverviewEvent()
    data object Refresh : OverviewEvent()
    data class DoHomework(val homework: HomeworkDetailsUi) : OverviewEvent()
    data class RepeatHomework(val homework: HomeworkDetailsUi) : OverviewEvent()
    data class SkipHomework(val homework: HomeworkDetailsUi) : OverviewEvent()
    data class UpdateTodoDone(val todo: TodoDetailsUi, val isDone: Boolean) : OverviewEvent()
    data class NavigateToHomeworkEditor(val homework: HomeworkDetailsUi) : OverviewEvent()
    data class NavigateToTodoEditor(val todo: TodoDetailsUi?) : OverviewEvent()
    data object AddHomeworkInEditor : OverviewEvent()
    data class NavigateToHomeworks(val homework: HomeworkDetailsUi?) : OverviewEvent()
    data object NavigateToTodos : OverviewEvent()
}

internal sealed class OverviewEffect : BaseUiEffect {
    data class ShowError(val failures: TasksFailures) : OverviewEffect()
    data class NavigateToLocal(val pushScreen: Screen) : OverviewEffect()
    data class NavigateToGlobal(val pushScreen: Screen) : OverviewEffect()
}

internal sealed class OverviewAction : BaseAction {
    data class UpdateHomeworks(
        val homeworks: Map<Instant, List<HomeworkDetailsUi>>,
        val homeworkScope: HomeworkScopeUi,
    ) : OverviewAction()
    data class UpdateActiveSchedule(val activeSchedule: ScheduleUi?) : OverviewAction()
    data class UpdateTaskErrors(
        val homeworkErrors: HomeworkErrorsUi?,
        val todoErrors: TodoErrorsUi?,
    ) : OverviewAction()
    data class UpdateTodos(val todos: List<TodoDetailsUi>) : OverviewAction()
    data class UpdateCurrentDate(val date: Instant) : OverviewAction()
    data class UpdateHomeworksLoading(val isLoading: Boolean) : OverviewAction()
    data class UpdateErrorsLoading(val isLoading: Boolean) : OverviewAction()
    data class UpdateTasksLoading(val isLoading: Boolean) : OverviewAction()
}