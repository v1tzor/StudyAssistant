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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.EditorFeatureComponent.EditorConfig
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleInput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleOutput
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleState

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal class WeekScheduleComposeStore(
    private val workProcessor: WeekScheduleWorkProcessor,
    stateCommunicator: StateCommunicator<WeekScheduleState>,
    effectCommunicator: EffectCommunicator<WeekScheduleEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<WeekScheduleState, WeekScheduleEvent, WeekScheduleAction, WeekScheduleEffect, WeekScheduleInput, WeekScheduleOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: WeekScheduleInput, isRestore: Boolean) {
        dispatchEvent(WeekScheduleEvent.Started(input, isRestore))
    }

    override suspend fun WorkScope<WeekScheduleState, WeekScheduleAction, WeekScheduleEffect, WeekScheduleOutput>.handleEvent(
        event: WeekScheduleEvent,
    ) {
        when (event) {
            is WeekScheduleEvent.Started -> with(event) {
                val week = if (isRestore) state.selectedWeek else inputData.week
                sendAction(WeekScheduleAction.UpdateSelectedWeek(week))
                launchBackgroundWork(BackgroundKey.FETCH_ORGANIZATIONS) {
                    val command = WeekScheduleWorkCommand.LoadOrganizationsData
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.FETCH_SCHEDULE) {
                    val command = WeekScheduleWorkCommand.LoadWeekSchedule(week)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is WeekScheduleEvent.Refresh -> with(state()) {
                launchBackgroundWork(BackgroundKey.FETCH_ORGANIZATIONS) {
                    val command = WeekScheduleWorkCommand.LoadOrganizationsData
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.FETCH_SCHEDULE) {
                    val command = WeekScheduleWorkCommand.LoadWeekSchedule(selectedWeek)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is WeekScheduleEvent.ChangeWeek -> {
                sendAction(WeekScheduleAction.UpdateSelectedWeek(event.numberOfWeek))
                launchBackgroundWork(BackgroundKey.FETCH_SCHEDULE) {
                    val command = WeekScheduleWorkCommand.LoadWeekSchedule(event.numberOfWeek)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is WeekScheduleEvent.UpdateOrganization -> {
                launchBackgroundWork(BackgroundKey.UPDATE_ORGANIZATION) {
                    val command = WeekScheduleWorkCommand.UpdateOrganization(event.organization)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is WeekScheduleEvent.DeleteClass -> {
                launchBackgroundWork(BackgroundKey.DELETE_CLASS) {
                    val command = WeekScheduleWorkCommand.DeleteClass(event.targetId, event.schedule)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is WeekScheduleEvent.CreateClassInEditor -> with(event) {
                val config = EditorConfig.Class(
                    classId = null,
                    scheduleId = event.schedule?.uid,
                    organizationId = null,
                    isCustomSchedule = false,
                    weekDay = weekDay,
                )
                consumeOutput(WeekScheduleOutput.NavigateToClassEditor(config))
            }
            is WeekScheduleEvent.EditClassInEditor -> with(event) {
                val config = EditorConfig.Class(
                    classId = editClass.uid,
                    scheduleId = editClass.scheduleId,
                    organizationId = editClass.organization.uid,
                    isCustomSchedule = false,
                    weekDay = weekDay,
                )
                consumeOutput(WeekScheduleOutput.NavigateToClassEditor(config))
            }
            is WeekScheduleEvent.NavigateToOrganizationEditor -> {
                val config = EditorConfig.Organization(null)
                consumeOutput(WeekScheduleOutput.NavigateToOrganizationEditor(config))
            }
            is WeekScheduleEvent.NavigateToBack -> {
                consumeOutput(WeekScheduleOutput.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: WeekScheduleAction,
        currentState: WeekScheduleState,
    ) = when (action) {
        is WeekScheduleAction.UpdateScheduleData -> currentState.copy(
            selectedWeek = action.week,
            weekSchedule = action.schedule,
            isLoading = false,
        )
        is WeekScheduleAction.UpdateOrganizationData -> currentState.copy(
            organizations = action.organizations,
            calendarSettings = action.settings,
        )
        is WeekScheduleAction.UpdateSelectedWeek -> currentState.copy(
            selectedWeek = action.week,
        )
        is WeekScheduleAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        FETCH_SCHEDULE, FETCH_ORGANIZATIONS, DELETE_CLASS, UPDATE_ORGANIZATION,
    }

    class Factory(
        private val workProcessor: WeekScheduleWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<WeekScheduleComposeStore, WeekScheduleState> {

        override fun create(savedState: WeekScheduleState): WeekScheduleComposeStore {
            return WeekScheduleComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}