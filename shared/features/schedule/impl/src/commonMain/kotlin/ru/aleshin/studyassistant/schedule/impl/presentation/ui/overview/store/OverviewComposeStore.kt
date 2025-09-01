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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.isCurrentWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.extensions.weekTimeRange
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.schedule.impl.presentation.models.schedule.ScheduleDetailsUi
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewState

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
internal class OverviewComposeStore(
    private val workProcessor: OverviewWorkProcessor,
    private val dateManager: DateManager,
    stateCommunicator: StateCommunicator<OverviewState>,
    effectCommunicator: EffectCommunicator<OverviewEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<OverviewState, OverviewEvent, OverviewAction, OverviewEffect, OverviewOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(OverviewEvent.Started)
    }

    override suspend fun WorkScope<OverviewState, OverviewAction, OverviewEffect, OverviewOutput>.handleEvent(
        event: OverviewEvent,
    ) {
        when (event) {
            is OverviewEvent.Started -> with(state) {
                val currentDate = dateManager.fetchBeginningCurrentInstant()
                val date = selectedDate ?: currentDate
                launchBackgroundWork(BackgroundKey.LOAD_SCHEDULE) {
                    val command = OverviewWorkCommand.LoadSchedule(date)
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.LOAD_ANALYSIS) {
                    val targetWeek = date.dateTime().weekTimeRange()
                    val command = OverviewWorkCommand.LoadAnalysis(targetWeek)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.SelectedDate -> with(state) {
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
            }
            is OverviewEvent.SelectedCurrentDay -> with(state) {
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
            }
            is OverviewEvent.ClickCompleteHomework -> with(event) {
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = OverviewWorkCommand.UpdateIsHomeworkDone(homework, isDone = true)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.ClickAgainHomework -> with(event) {
                launchBackgroundWork(BackgroundKey.HOMEWORK_ACTION) {
                    val command = OverviewWorkCommand.UpdateIsHomeworkDone(homework, isDone = false)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is OverviewEvent.ClickEditHomework -> with(event) {
                val config = EditorConfig.Homework(
                    homeworkId = homework.uid,
                    date = homework.deadline.startThisDay().toEpochMilliseconds(),
                    subjectId = homework.subject?.uid,
                    organizationId = homework.organization.uid,
                )
                consumeOutput(OverviewOutput.NavigateToHomeworkEditor(config))
            }
            is OverviewEvent.ClickAddHomework -> with(event) {
                val config = EditorConfig.Homework(
                    homeworkId = null,
                    date = date.startThisDay().toEpochMilliseconds(),
                    subjectId = classModel.subject?.uid,
                    organizationId = classModel.organization.uid,
                )
                consumeOutput(OverviewOutput.NavigateToHomeworkEditor(config))
            }
            is OverviewEvent.ClickEdit -> with(state) {
                val selectedDate = checkNotNull(selectedDate)
                val baseSchedule = if (schedule != null && schedule is ScheduleDetailsUi.Base) schedule else null
                val customSchedule = if (schedule != null && schedule is ScheduleDetailsUi.Custom) schedule else null
                val config = EditorConfig.DailySchedule(
                    date = selectedDate.startThisDay().toEpochMilliseconds(),
                    baseScheduleId = baseSchedule?.data?.uid,
                    customScheduleId = customSchedule?.data?.uid,
                )
                consumeOutput(OverviewOutput.NavigateToDailyScheduleEditor(config))
            }
            is OverviewEvent.ClickDetails -> {
                consumeOutput(OverviewOutput.NavigateToDetails)
            }
        }
    }

    override suspend fun reduce(
        action: OverviewAction,
        currentState: OverviewState,
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

    class Factory(
        private val workProcessor: OverviewWorkProcessor,
        private val dateManager: DateManager,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<OverviewComposeStore, OverviewState> {

        override fun create(savedState: OverviewState): OverviewComposeStore {
            return OverviewComposeStore(
                workProcessor = workProcessor,
                dateManager = dateManager,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}