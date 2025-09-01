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

package ru.aleshin.studyassistant.presentation.ui.main.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainAction
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainEffect
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainEvent
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainInput
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainOutput
import ru.aleshin.studyassistant.presentation.ui.main.contract.MainState

/**
 * @author Stanislav Aleshin on 27.01.2024
 */
class MainComposeStore(
    private val workProcessor: MainWorkProcessor,
    stateCommunicator: StateCommunicator<MainState>,
    effectCommunicator: EffectCommunicator<MainEffect>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<MainState, MainEvent, MainAction, MainEffect, MainInput, MainOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override fun initialize(input: MainInput, isRestore: Boolean) {
        dispatchEvent(MainEvent.StartBackgroundWork)
    }

    override suspend fun WorkScope<MainState, MainAction, MainEffect, MainOutput>.handleEvent(
        event: MainEvent,
    ) {
        when (event) {
            is MainEvent.StartBackgroundWork -> {
                launchBackgroundWork(MainWorkCommand.LoadThemeSettings) {
                    val command = MainWorkCommand.LoadThemeSettings
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(MainWorkCommand.UpdatePushToken) {
                    val command = MainWorkCommand.UpdatePushToken
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(MainWorkCommand.UpdateReminderServices) {
                    val command = MainWorkCommand.UpdateReminderServices
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(MainWorkCommand.UpdateSubscriptionInfo) {
                    val command = MainWorkCommand.UpdateSubscriptionInfo
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(MainWorkCommand.PerformSourceSync) {
                    val command = MainWorkCommand.PerformSourceSync
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
            is MainEvent.ExecuteNavigation -> {
                launchBackgroundWork(MainWorkCommand.InitialNavigation) {
                    val command = MainWorkCommand.InitialNavigation
                    workProcessor.work(command).collectAndHandleWork()
                }
            }
        }
    }

    override suspend fun reduce(
        action: MainAction,
        currentState: MainState,
    ) = when (action) {
        is MainAction.UpdateSettings -> currentState.copy(
            generalSettings = action.settings,
        )
    }

    class Factory(
        private val workProcessor: MainWorkProcessor,
        private val coroutineManager: CoroutineManager,
    ) : BaseComposeStore.Factory<MainComposeStore, MainState> {

        override fun create(savedState: MainState): MainComposeStore {
            return MainComposeStore(
                workProcessor = workProcessor,
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}