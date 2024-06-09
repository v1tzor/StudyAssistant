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
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.EmptyDeps
import architecture.screenmodel.work.BackgroundWorkKey
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.editor.api.navigation.EditorScreen
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen
import ru.aleshin.studyassistant.schedule.impl.di.holder.ScheduleFeatureDIHolder
import ru.aleshin.studyassistant.schedule.impl.navigation.ScheduleScreenProvider
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.details.contract.DetailsViewState

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
            is DetailsEvent.Init -> launchBackgroundWork(BackgroundKey.LOAD_SCHEDULE) {
                val command = DetailsWorkCommand.SetupWeekSchedule
                workProcessor.work(command).collectAndHandleWork()
            }
            is DetailsEvent.SelectedWeek -> launchBackgroundWork(BackgroundKey.LOAD_SCHEDULE) {
                sendAction(DetailsAction.UpdateSelectedWeek(event.week))
                val command = DetailsWorkCommand.LoadWeekSchedule(event.week)
                workProcessor.work(command).collectAndHandleWork()
            }
            is DetailsEvent.SelectedViewType -> {
                sendAction(DetailsAction.UpdateViewType(event.scheduleView))
            }
            is DetailsEvent.NavigateToOverview -> {
                val screen = screenProvider.provideFeatureScreen(ScheduleScreen.Overview)
                sendEffect(DetailsEffect.NavigateToLocal(screen))
            }
            is DetailsEvent.NavigateToEditor -> {
                val screen = screenProvider.provideEditorScreen(EditorScreen.Schedule)
                sendEffect(DetailsEffect.NavigateToGlobal(screen))
            }
        }
    }

    override suspend fun reduce(
        action: DetailsAction,
        currentState: DetailsViewState,
    ) = when (action) {
        is DetailsAction.SetupWeekSchedule -> currentState.copy(
            schedule = action.schedule,
            currentWeek = action.currentWeek,
            isLoading = false,
        )
        is DetailsAction.UpdateWeekSchedule -> currentState.copy(
            schedule = action.schedule,
            isLoading = false,
        )
        is DetailsAction.UpdateSelectedWeek -> currentState.copy(
            selectedWeek = action.week,
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
