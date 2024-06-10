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
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.EmptyDeps
import architecture.screenmodel.work.BackgroundWorkKey
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import entities.common.NumberOfRepeatWeek
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.editor.impl.di.holder.EditorFeatureDIHolder
import ru.aleshin.studyassistant.editor.impl.navigation.EditorScreenProvider
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.ScheduleEditorAction
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.ScheduleEditorEffect
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.ScheduleEditorEvent
import ru.aleshin.studyassistant.editor.impl.presentation.ui.schedule.contract.ScheduleEditorViewState

/**
 * @author Stanislav Aleshin on 27.05.2024.
 */
internal class ScheduleEditorScreenModel(
    private val workProcessor: ScheduleEditorWorkProcessor,
    private val screenProvider: EditorScreenProvider,
    stateCommunicator: ScheduleEditorStateCommunicator,
    effectCommunicator: ScheduleEditorEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<ScheduleEditorViewState, ScheduleEditorEvent, ScheduleEditorAction, ScheduleEditorEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(ScheduleEditorEvent.Init)
        }
    }

    override suspend fun WorkScope<ScheduleEditorViewState, ScheduleEditorAction, ScheduleEditorEffect>.handleEvent(
        event: ScheduleEditorEvent,
    ) {
        when (event) {
            is ScheduleEditorEvent.Init -> {
                launchBackgroundWork(BackgroundKey.FETCH_ORGANIZATIONS) {
                    val command = ScheduleEditorWorkCommand.LoadOrganizationsData
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.FETCH_SCHEDULE) {
                    val command = ScheduleEditorWorkCommand.LoadWeekSchedule(NumberOfRepeatWeek.ONE)
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is ScheduleEditorEvent.ChangeWeek -> launchBackgroundWork(BackgroundKey.FETCH_SCHEDULE) {
                val command = ScheduleEditorWorkCommand.LoadWeekSchedule(event.numberOfWeek)
                workProcessor.work(command).collectAndHandleWork()
            }
            is ScheduleEditorEvent.UpdateOrganization -> launchBackgroundWork(BackgroundKey.UPDATE_ORGANIZATION) {
                val command = ScheduleEditorWorkCommand.UpdateOrganization(event.organization)
                workProcessor.work(command).collectAndHandleWork()
            }
            is ScheduleEditorEvent.CreateClass -> launchBackgroundWork(BackgroundKey.DELETE_CLASS) {
                val featureScreen = EditorScreen.Class(null, event.schedule?.uid, false, event.weekDay)
                val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(ScheduleEditorEffect.NavigateToLocal(targetScreen))
            }
            is ScheduleEditorEvent.EditClass -> with(event) {
                val featureScreen = EditorScreen.Class(editClass.uid, editClass.scheduleId, false, weekDay)
                val targetScreen = screenProvider.provideFeatureScreen(featureScreen)
                sendEffect(ScheduleEditorEffect.NavigateToLocal(targetScreen))
            }
            is ScheduleEditorEvent.DeleteClass -> launchBackgroundWork(BackgroundKey.DELETE_CLASS) {
                val command = ScheduleEditorWorkCommand.DeleteClass(event.uid, event.schedule)
                workProcessor.work(command).collectAndHandleWork()
            }
            is ScheduleEditorEvent.SaveSchedule -> {
                sendEffect(ScheduleEditorEffect.NavigateToBack)
            }
        }
    }

    override suspend fun reduce(
        action: ScheduleEditorAction,
        currentState: ScheduleEditorViewState,
    ) = when (action) {
        is ScheduleEditorAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
        is ScheduleEditorAction.UpdateScheduleData -> currentState.copy(
            currentWeek = action.week,
            weekSchedule = action.schedule,
            isLoading = false,
        )
        is ScheduleEditorAction.UpdateOrganizationData -> currentState.copy(
            organizations = action.organizations,
            calendarSettings = action.settings,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        FETCH_SCHEDULE, FETCH_ORGANIZATIONS, DELETE_CLASS, UPDATE_ORGANIZATION,
    }
}

@Composable
internal fun Screen.rememberScheduleEditorScreenModel(): ScheduleEditorScreenModel {
    val di = EditorFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<ScheduleEditorScreenModel>() }
}