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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.screenmodel

import androidx.compose.runtime.Composable
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.work.BackgroundWorkKey
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import extensions.endOfWeek
import extensions.shiftWeek
import extensions.startOfWeek
import extensions.startThisDay
import functional.TimeRange
import managers.CoroutineManager
import managers.DateManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.tasks.api.navigation.TasksScreen
import ru.aleshin.studyassistant.tasks.impl.di.holder.TasksFeatureDIHolder
import ru.aleshin.studyassistant.tasks.impl.navigation.TasksScreenProvider
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksAction
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksDeps
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEffect
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksEvent
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksViewState

/**
 * @author Stanislav Aleshin on 27.06.2024
 */
internal class HomeworksScreenModel(
    private val workProcessor: HomeworksWorkProcessor,
    private val screenProvider: TasksScreenProvider,
    private val dateManager: DateManager,
    stateCommunicator: HomeworksStateCommunicator,
    effectCommunicator: HomeworksEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<HomeworksViewState, HomeworksEvent, HomeworksAction, HomeworksEffect, HomeworksDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: HomeworksDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(HomeworksEvent.Init)
        }
    }

    override suspend fun WorkScope<HomeworksViewState, HomeworksAction, HomeworksEffect>.handleEvent(
        event: HomeworksEvent,
    ) {
        when (event) {
            is HomeworksEvent.Init, HomeworksEvent.Refresh -> {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val targetTimeRange = TimeRange(
                    from = currentDate.startOfWeek().shiftWeek(-1),
                    to = currentDate.endOfWeek().shiftWeek(+1),
                )
                sendAction(HomeworksAction.UpdateDates(currentDate, targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = HomeworksWorkCommand.LoadHomeworks(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ACTIVE_SCHEDULE) {
                    val command = HomeworksWorkCommand.LoadActiveSchedule(currentDate)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.NextTimeRange -> with(state()) {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val currentTimeRange = checkNotNull(selectedTimeRange)
                val targetTimeRange = TimeRange(
                    from = currentTimeRange.from.shiftWeek(+1),
                    to = currentTimeRange.to.shiftWeek(+1),
                )
                sendAction(HomeworksAction.UpdateDates(currentDate, targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = HomeworksWorkCommand.LoadHomeworks(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.PreviousTimeRange -> with(state()){
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val currentTimeRange = checkNotNull(selectedTimeRange)
                val targetTimeRange = TimeRange(
                    from = currentTimeRange.from.shiftWeek(-1),
                    to = currentTimeRange.to.shiftWeek(-1),
                )
                sendAction(HomeworksAction.UpdateDates(currentDate, targetTimeRange))
                launchBackgroundWork(BackgroundKey.LOAD_HOMEWORKS) {
                    val command = HomeworksWorkCommand.LoadHomeworks(targetTimeRange)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.DoHomework -> with(event) {
                val currentTime = dateManager.fetchCurrentInstant()
                val updatedHomework = homework.copy(isDone = true, completeDate = currentTime)
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = HomeworksWorkCommand.UpdateHomework(updatedHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.SkipHomework -> with(event) {
                val currentTime = dateManager.fetchCurrentInstant()
                val updatedHomework = homework.copy(isDone = false, completeDate = currentTime)
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = HomeworksWorkCommand.UpdateHomework(updatedHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.RepeatHomework -> with(event) {
                val updatedHomework = homework.copy(isDone = false, completeDate = null)
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = HomeworksWorkCommand.UpdateHomework(updatedHomework)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is HomeworksEvent.NavigateToHomeworkEditor -> with(event) {
                val featureScreen = EditorScreen.Homework(
                    homeworkId = homework.uid,
                    date = homework.deadline.startThisDay().toEpochMilliseconds(),
                    subjectId = homework.subject?.uid,
                    organizationId = homework.organization.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(HomeworksEffect.NavigateToGlobal(screen))
            }
            is HomeworksEvent.AddHomeworkInEditor -> with(state()) {
                val currentTime = dateManager.fetchCurrentInstant()
                val activeClass = activeSchedule?.classes?.find {
                    it.timeRange.containsTime(currentTime)
                }
                val featureScreen = EditorScreen.Homework(
                    homeworkId = null,
                    date = null,
                    subjectId = activeClass?.subject?.uid,
                    organizationId = activeClass?.organization?.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(HomeworksEffect.NavigateToGlobal(screen))
            }
            is HomeworksEvent.NavigateToOverview -> {
                val screen = screenProvider.provideFeatureScreen(TasksScreen.Overview)
                sendEffect(HomeworksEffect.NavigateToLocal(screen))
            }
        }
    }

    override suspend fun reduce(
        action: HomeworksAction,
        currentState: HomeworksViewState,
    ) = when (action) {
        is HomeworksAction.UpdateHomeworks -> currentState.copy(
            homeworks = action.homeworks,
            isLoading = false,
        )
        is HomeworksAction.UpdateActiveSchedule -> currentState.copy(
            activeSchedule = action.activeSchedule,
        )
        is HomeworksAction.UpdateDates -> currentState.copy(
            currentDate = action.currentDate,
            selectedTimeRange = action.selectedTimeRange,
        )
        is HomeworksAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_HOMEWORKS, LOAD_ACTIVE_SCHEDULE, HOMEWORK_ACTION,
    }
}

@Composable
internal fun Screen.rememberHomeworksScreenModel(): HomeworksScreenModel {
    val di = TasksFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<HomeworksScreenModel>() }
}