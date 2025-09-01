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

package ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.extensions.dateTime
import ru.aleshin.studyassistant.core.common.extensions.shiftWeek
import ru.aleshin.studyassistant.core.common.extensions.startThisDay
import ru.aleshin.studyassistant.core.common.extensions.weekTimeRange
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.core.common.managers.DateManager
import ru.aleshin.studyassistant.core.domain.entities.common.NumberOfRepeatWeek
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsOutput
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsState
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.store.OverviewComposeStore

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
internal class DetailsComposeStore(
    private val workProcessor: DetailsWorkProcessor,
    private val dateManager: DateManager,
    stateCommunicator: StateCommunicator<DetailsState>,
    effectCommunicator: EffectCommunicator<DetailsEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<DetailsState, DetailsEvent, DetailsAction, DetailsEffect, DetailsOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: EmptyInput, isRestore: Boolean) {
        dispatchEvent(DetailsEvent.Started)
    }

    override suspend fun WorkScope<DetailsState, DetailsAction, DetailsEffect, DetailsOutput>.handleEvent(
        event: DetailsEvent,
    ) {
        when (event) {
            is DetailsEvent.Started -> with(state) {
                launchBackgroundWork(BackgroundKey.LOAD_SCHEDULE) {
                    if (selectedWeek != null) {
                        val command = DetailsWorkCommand.LoadWeekSchedule(selectedWeek)
                        workProcessor.work(command).collectAndHandleWork()
                    } else {
                        val currentDate = dateManager.fetchBeginningCurrentInstant()
                        val week = currentDate.dateTime().weekTimeRange()
                        sendAction(DetailsAction.UpdateSelectedWeek(week))
                        val command = DetailsWorkCommand.LoadWeekSchedule(week)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is DetailsEvent.SelectedCurrentWeek -> with(state) {
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
                launchBackgroundWork(OverviewComposeStore.BackgroundKey.HOMEWORK_ACTION) {
                    val command = DetailsWorkCommand.UpdateIsHomeworkDone(event.homework, isDone = true)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DetailsEvent.ClickAgainHomework -> {
                launchBackgroundWork(OverviewComposeStore.BackgroundKey.HOMEWORK_ACTION) {
                    val command = DetailsWorkCommand.UpdateIsHomeworkDone(event.homework, isDone = false)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is DetailsEvent.ClickEditHomework -> with(event) {
                val config = EditorConfig.Homework(
                    homeworkId = homework.uid,
                    date = homework.deadline.startThisDay().toEpochMilliseconds(),
                    subjectId = homework.subject?.uid,
                    organizationId = homework.organization.uid,
                )
                consumeOutput(DetailsOutput.NavigateToHomeworkEditor(config))
            }
            is DetailsEvent.ClickAddHomework -> with(event) {
                val config = EditorConfig.Homework(
                    homeworkId = null,
                    date = event.date.startThisDay().toEpochMilliseconds(),
                    subjectId = classModel.subject?.uid,
                    organizationId = classModel.organization.uid,
                )
                consumeOutput(DetailsOutput.NavigateToHomeworkEditor(config))
            }
            is DetailsEvent.ClickOverview -> {
                consumeOutput(DetailsOutput.NavigateToOverview)
            }
            is DetailsEvent.ClickEdit -> with(state()) {
                val week = weekSchedule?.numberOfWeek ?: NumberOfRepeatWeek.ONE
                val config = EditorConfig.WeekSchedule(week)
                consumeOutput(DetailsOutput.NavigateToWeekScheduleEditor(config))
            }
        }
    }

    override suspend fun reduce(
        action: DetailsAction,
        currentState: DetailsState,
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

    class Factory(
        private val workProcessor: DetailsWorkProcessor,
        private val dateManager: DateManager,
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<DetailsComposeStore, DetailsState> {

        override fun create(savedState: DetailsState): DetailsComposeStore {
            return DetailsComposeStore(
                workProcessor = workProcessor,
                dateManager = dateManager,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}