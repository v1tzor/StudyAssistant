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

package ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.screenmodel

import androidx.compose.runtime.Composable
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import org.kodein.di.instance
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.BaseScreenModel
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.BackgroundWorkKey
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.navigation.EditorScreenProvider
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleDeps
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.WeekScheduleViewState

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal class WeekScheduleScreenModel(
    private val workProcessor: WeekScheduleWorkProcessor,
    private val screenProvider: EditorScreenProvider,
    stateCommunicator: WeekScheduleStateCommunicator,
    effectCommunicator: WeekScheduleEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<WeekScheduleViewState, WeekScheduleEvent, WeekScheduleAction, WeekScheduleEffect, WeekScheduleDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: WeekScheduleDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(WeekScheduleEvent.Init(deps.week))
        }
    }

    override suspend fun WorkScope<WeekScheduleViewState, WeekScheduleAction, WeekScheduleEffect>.handleEvent(
        event: WeekScheduleEvent,
    ) {
        when (event) {
            is WeekScheduleEvent.Init -> {
                sendAction(WeekScheduleAction.UpdateSelectedWeek(event.week))
                launchBackgroundWork(BackgroundKey.FETCH_ORGANIZATIONS) {
                    val command = WeekScheduleWorkCommand.LoadOrganizationsData
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.FETCH_SCHEDULE) {
                    val command = WeekScheduleWorkCommand.LoadWeekSchedule(event.week)
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
                val featureScreen = EditorScreen.Class(
                    classId = null,
                    scheduleId = event.schedule?.uid,
                    organizationId = null,
                    isCustomSchedule = false,
                    weekDay = weekDay,
                )
                val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(WeekScheduleEffect.NavigateToLocal(targetScreen))
            }
            is WeekScheduleEvent.EditClassInEditor -> with(event) {
                val featureScreen = EditorScreen.Class(
                    classId = editClass.uid,
                    scheduleId = editClass.scheduleId,
                    organizationId = editClass.organization.uid,
                    isCustomSchedule = false,
                    weekDay = weekDay,
                )
                val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(WeekScheduleEffect.NavigateToLocal(targetScreen))
            }
            is WeekScheduleEvent.NavigateToOrganizationEditor -> {
                val featureScreen = EditorScreen.Organization(null)
                val screen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(WeekScheduleEffect.NavigateToLocal(screen))
            }
            is WeekScheduleEvent.NavigateToBack -> {
                sendEffect(WeekScheduleEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: WeekScheduleAction,
        currentState: WeekScheduleViewState,
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
}

@Composable
internal fun Screen.rememberWeekScheduleScreenModel(): WeekScheduleScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<WeekScheduleScreenModel>() }
}