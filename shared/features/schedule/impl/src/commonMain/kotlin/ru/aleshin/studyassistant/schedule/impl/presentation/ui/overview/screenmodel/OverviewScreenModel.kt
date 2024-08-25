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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.isCurrentWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.extensions.weekTimeRange
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen
import ru.aleshin.studyassistant.schedule.impl.di.holder.ScheduleFeatureDIHolder
import ru.aleshin.studyassistant.schedule.impl.navigation.ScheduleScreenProvider
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.ScheduleDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewViewState

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
internal class OverviewScreenModel(
    private val workProcessor: OverviewWorkProcessor,
    private val screenProvider: ScheduleScreenProvider,
    stateCommunicator: OverviewStateCommunicator,
    effectCommunicator: OverviewEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<OverviewViewState, OverviewEvent, OverviewAction, OverviewEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(OverviewEvent.Init)
        }
    }

    override suspend fun WorkScope<OverviewViewState, OverviewAction, OverviewEffect>.handleEvent(
        event: OverviewEvent,
    ) {
        when (event) {
            is OverviewEvent.Init -> with(state()) {
                sendAction(OverviewAction.UpdateSelectedDate(currentDate))
                launchBackgroundWork(BackgroundKey.LOAD_SCHEDULE) {
                    val command = OverviewWorkCommand.LoadSchedule(currentDate)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ANALYSIS) {
                    val targetWeek = currentDate.dateTime().weekTimeRange()
                    val command = OverviewWorkCommand.LoadAnalysis(targetWeek)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.SelectedDate -> with(state()) {
                if (selectedDate?.isCurrentWeek(event.date) != true) {
                    launchBackgroundWork(BackgroundKey.LOAD_ANALYSIS) {
                        val targetWeek = event.date.dateTime().weekTimeRange()
                        val command = OverviewWorkCommand.LoadAnalysis(targetWeek)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
                launchBackgroundWork(BackgroundKey.LOAD_SCHEDULE) {
                    val command = OverviewWorkCommand.LoadSchedule(event.date)
                    workProcessor.work(command).collectAndHandleWork()
                }
                sendAction(OverviewAction.UpdateSelectedDate(event.date))
            }
            is OverviewEvent.SelectedCurrentDay -> with(state()) {
                if (selectedDate?.isCurrentWeek(currentDate) != true) {
                    launchBackgroundWork(BackgroundKey.LOAD_ANALYSIS) {
                        val targetWeek = currentDate.dateTime().weekTimeRange()
                        val command = OverviewWorkCommand.LoadAnalysis(targetWeek)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
                launchBackgroundWork(BackgroundKey.LOAD_SCHEDULE) {
                    val command = OverviewWorkCommand.LoadSchedule(currentDate)
                    workProcessor.work(command).collectAndHandleWork()
                }
                sendAction(OverviewAction.UpdateSelectedDate(currentDate))
            }
            is OverviewEvent.CompleteHomework -> {
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = OverviewWorkCommand.UpdateIsHomeworkDone(event.homework, isDone = true)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.CancelCompleteHomework -> {
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = OverviewWorkCommand.UpdateIsHomeworkDone(event.homework, isDone = false)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.EditHomeworkInEditor -> with(event) {
                val featureScreen = EditorScreen.Homework(
                    homeworkId = homework.uid,
                    date = homework.deadline.startThisDay().toEpochMilliseconds(),
                    subjectId = homework.subject?.uid,
                    organizationId = homework.organization.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(OverviewEffect.NavigateToGlobal(screen))
            }
            is OverviewEvent.AddHomeworkInEditor -> with(event) {
                val featureScreen = EditorScreen.Homework(
                    homeworkId = null,
                    date = date.startThisDay().toEpochMilliseconds(),
                    subjectId = classModel.subject?.uid,
                    organizationId = classModel.organization.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(OverviewEffect.NavigateToGlobal(screen))
            }
            is OverviewEvent.NavigateToDailyScheduleEditor -> with(state()) {
                val selectedDate = checkNotNull(selectedDate)
                val baseSchedule = if (schedule != null && schedule is ScheduleDetailsUi.Base) schedule else null
                val customSchedule = if (schedule != null && schedule is ScheduleDetailsUi.Custom) schedule else null
                val featureScreen = EditorScreen.DailySchedule(
                    date = selectedDate.startThisDay().toEpochMilliseconds(),
                    baseScheduleId = baseSchedule?.data?.uid,
                    customScheduleId = customSchedule?.data?.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(OverviewEffect.NavigateToGlobal(screen))
            }
            is OverviewEvent.NavigateToDetails -> {
                val screen = screenProvider.provideFeatureScreen(ScheduleScreen.Details)
                sendEffect(OverviewEffect.NavigateToLocal(screen))
            }
        }
    }

    override suspend fun reduce(
        action: OverviewAction,
        currentState: OverviewViewState,
    ) = when (action) {
        is OverviewAction.UpdateSchedule -> currentState.copy(
            schedule = action.schedule,
            isScheduleLoading = false,
        )
        is OverviewAction.UpdateAnalysis -> currentState.copy(
            weekAnalysis = action.weekAnalysis,
            isAnalyticsLoading = false,
        )
        is OverviewAction.UpdateSelectedDate -> currentState.copy(
            selectedDate = action.date,
        )
        is OverviewAction.UpdateActiveClass -> currentState.copy(
            activeClass = action.activeClass,
        )
        is OverviewAction.UpdateScheduleLoading -> currentState.copy(
            isScheduleLoading = action.isLoading,
        )
        is OverviewAction.UpdateAnalyticsLoading -> currentState.copy(
            isAnalyticsLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_ANALYSIS, LOAD_SCHEDULE, HOMEWORK_ACTION,
    }
}

@Composable
internal fun Screen.rememberOverviewScreenModel(): OverviewScreenModel {
    val di = ScheduleFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<OverviewScreenModel>() }
}