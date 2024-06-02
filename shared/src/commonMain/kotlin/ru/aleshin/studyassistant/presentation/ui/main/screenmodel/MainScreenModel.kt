/*
 * Copyright 2023 Stanislav Aleshin
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

package ru.aleshin.studyassistant.presentation.ui.main.screenmodel

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import architecture.screenmodel.BaseScreenModel
import architecture.screenmodel.EmptyDeps
import architecture.screenmodel.work.WorkScope
import managers.CoroutineManager
import ru.aleshin.studyassistant.di.MainDependenciesGraph
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainAction
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainEffect
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainEvent
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainViewState

/**
 * @author Stanislav Aleshin on 27.01.2024
 */
class MainScreenModel(
    private val workProcessor: MainWorkProcessor,
    stateCommunicator: MainStateCommunicator,
    effectCommunicator: MainEffectCommunicator,
    coroutineManager: CoroutineManager,
) : BaseScreenModel<MainViewState, MainEvent, MainAction, MainEffect, EmptyDeps>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun init(deps: EmptyDeps) {
        if (!isInitialize) {
            super.init(deps)
            dispatchEvent(MainEvent.Init)
        }
    }

    override suspend fun WorkScope<MainViewState, MainAction, MainEffect>.handleEvent(
        event: MainEvent,
    ) {
        when (event) {
            MainEvent.Init -> {
                launchBackgroundWork(MainWorkCommand.LoadThemeSettings) {
                    val command = MainWorkCommand.LoadThemeSettings
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(MainWorkCommand.InitialNavigation) {
                    val command = MainWorkCommand.InitialNavigation
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
        }
    }

    override suspend fun reduce(
        action: MainAction,
        currentState: MainViewState,
    ) = when (action) {
        is MainAction.UpdateSettings -> currentState.copy(
            generalSettings = action.settings,
        )
    }
}

@Composable
fun rememberMainScreenModel(): MainScreenModel {
    return remember { MainDependenciesGraph.instance<MainScreenModel>() }
}