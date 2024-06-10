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
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.EmptyDeps
import architecture.screenmodel.work.WorkScope
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import managers.CoroutineManager
import org.kodein.di.instance
import ru.aleshin.studyassistant.schedule.api.navigation.ScheduleScreen
import ru.aleshin.studyassistant.schedule.impl.di.holder.ScheduleFeatureDIHolder
import ru.aleshin.studyassistant.schedule.impl.navigation.ScheduleScreenProvider
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewAction
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEffect
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewEvent
import ru.aleshin.studyassistant.schedule.impl.presentation.ui.overview.contract.OverviewViewState

/**
 * @author Stanislav Aleshin on 09.06.2024
 */
internal class OverviewScreenModel(
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
    ) = when (event) {
        OverviewEvent.Init -> {}
        OverviewEvent.SelectedCurrentDay -> {}
        OverviewEvent.NavigateToDetails -> {
            val screen = screenProvider.provideFeatureScreen(ScheduleScreen.Details)
            sendEffect(OverviewEffect.NavigateToLocal(screen))
        }
    }

    override suspend fun reduce(
        action: OverviewAction,
        currentState: OverviewViewState,
    ) = when (action) {
        is OverviewAction.UpdateLoading -> currentState.copy(
            isLoading = action.isLoading,
        )
    }
}

@Composable
internal fun Screen.rememberOverviewScreenModel(): OverviewScreenModel {
    val di = ScheduleFeatureDIHolder.fetchDI()
    return rememberScreenModel { di.instance<OverviewScreenModel>() }
}