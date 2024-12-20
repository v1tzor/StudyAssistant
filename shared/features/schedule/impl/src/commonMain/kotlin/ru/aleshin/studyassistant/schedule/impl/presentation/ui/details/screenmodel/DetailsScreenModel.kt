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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.EmptyDeps
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.extensions.weekTimeRange
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen
import ru.aleshin.studyassistant.schedule.impl.di.holder.ScheduleFeatureDIHolder
import ru.aleshin.studyassistant.schedule.impl.navigation.ScheduleScreenProvider
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsViewState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.screenmodel.OverviewScreenModel

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
internal class DetailsScreenModel(
    private val workProcessor: DetailsWorkProcessor,
    private val screenProvider: ScheduleScreenProvider,
    stateCommunicator: DetailsStateCommunicator,
    effectCommunicator: DetailsEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<DetailsViewState, DetailsEvent, DetailsAction, DetailsEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(DetailsEvent.Init)
        }
    }

    override suspend fun WorkScope<DetailsViewState, DetailsAction, DetailsEffect>.handleEvent(
        event: DetailsEvent,
    ) {
        when (event) {
            is DetailsEvent.Init, DetailsEvent.SelectedCurrentWeek -> with(state()) {
                launchBackgroundWork(BackgroundKey.LOAD_SCHEDULE) {
                    val week = currentDate.dateTime().weekTimeRange()
                    sendAction(DetailsAction.UpdateSelectedWeek(week))
                    val command = DetailsWorkCommand.LoadWeekSchedule(week)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DetailsEvent.SelectedNextWeek -> with(state()) {
                launchBackgroundWork(BackgroundKey.LOAD_SCHEDULE) {
                    if (selectedWeek != null) {
                        val week = selectedWeek.shiftWeek(1)
                        sendAction(DetailsAction.UpdateSelectedWeek(week))
                        val command = DetailsWorkCommand.LoadWeekSchedule(week)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is DetailsEvent.SelectedPreviousWeek -> with(state()) {
                launchBackgroundWork(BackgroundKey.LOAD_SCHEDULE) {
                    if (selectedWeek != null) {
                        val week = selectedWeek.shiftWeek(-1)
                        sendAction(DetailsAction.UpdateSelectedWeek(week))
                        val command = DetailsWorkCommand.LoadWeekSchedule(week)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is DetailsEvent.SelectedViewType -> {
                sendAction(DetailsAction.UpdateViewType(event.scheduleView))
            }
            is DetailsEvent.CompleteHomework -> {
                launchBackgroundWork(OverviewScreenModel.BackgroundKey.HOMEWORK_ACTION) {
                    val command = DetailsWorkCommand.UpdateIsHomeworkDone(event.homework, isDone = true)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DetailsEvent.CancelCompleteHomework -> {
                launchBackgroundWork(OverviewScreenModel.BackgroundKey.HOMEWORK_ACTION) {
                    val command = DetailsWorkCommand.UpdateIsHomeworkDone(event.homework, isDone = false)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DetailsEvent.EditHomeworkInEditor -> with(event) {
                val featureScreen = EditorScreen.Homework(
                    homeworkId = homework.uid,
                    date = homework.deadline.startThisDay().toEpochMilliseconds(),
                    subjectId = homework.subject?.uid,
                    organizationId = homework.organization.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(DetailsEffect.NavigateToGlobal(screen))
            }
            is DetailsEvent.AddHomeworkInEditor -> with(event) {
                val featureScreen = EditorScreen.Homework(
                    homeworkId = null,
                    date = event.date.startThisDay().toEpochMilliseconds(),
                    subjectId = classModel.subject?.uid,
                    organizationId = classModel.organization.uid,
                )
                val screen = screenProvider.provideEditorScreen(featureScreen)
                sendEffect(DetailsEffect.NavigateToGlobal(screen))
            }
            is DetailsEvent.NavigateToOverview -> {
                val screen = screenProvider.provideFeatureScreen(ScheduleScreen.Overview)
                sendEffect(DetailsEffect.NavigateToLocal(screen))
            }
            is DetailsEvent.NavigateToEditor -> with(state()) {
                val week = weekSchedule?.numberOfWeek ?: NumberOfRepeatWeek.ONE
                val screen = screenProvider.provideEditorScreen(EditorScreen.WeekSchedule(week))
                sendEffect(DetailsEffect.NavigateToGlobal(screen))
            }
        }
    }

    override suspend fun reduce(
        action: DetailsAction,
        currentState: DetailsViewState,
    ) = when (action) {
        is DetailsAction.UpdateWeekSchedule -> currentState.copy(
            weekSchedule = action.schedule,
            isLoading = false,
        )
        is DetailsAction.UpdateSelectedWeek -> currentState.copy(
            selectedWeek = action.week,
        )
        is DetailsAction.UpdateActiveClass -> currentState.copy(
            activeClass = action.activeClass,
        )
        is DetailsAction.UpdateViewType -> currentState.copy(
            scheduleView = action.scheduleView,
        )
        is DetailsAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        LOAD_SCHEDULE
    }
}

@Composable
internal fun Screen.rememberDetailsScreenModel(): DetailsScreenModel {
    val di = ScheduleFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<DetailsScreenModel>() }
}