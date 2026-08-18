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
import ru.aleshin.studyassistant.core.common.architecture.store.work.BackgroundWorkKey
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
        dispatchEvent(MainEvent.Init(input, isRestore))
    }

    override suspend fun WorkScope<MainState, MainAction, MainEffect, MainOutput>.handleEvent(
        event: MainEvent,
    ) {
        when (event) {
            is MainEvent.Init -> with(event) {
                launchBackgroundWork(BackgroundKey.THEME) {
                    val command = MainWorkCommand.LoadThemeSettings
                    workProcessor.work(command).collectAndHandleWork()
                }
                launchBackgroundWork(BackgroundKey.REMINDERS) {
                    val command = MainWorkCommand.UpdateReminderServices
                    workProcessor.work(command).collectAndHandleWork()
                }
                if (input.deepLinkUrl != null) {
                    sendAction(MainAction.UpdatePendingDeepLink(input.deepLinkUrl))
                }
                if (!isRestore) {
                    launchBackgroundWork(BackgroundKey.NAVIGATION) {
                        val navigationCommand = MainWorkCommand.InitialNavigation
                        workProcessor.work(navigationCommand).collectAndHandleWork()
                        sendAction(MainAction.UpdateInitialNavigationDone(true))
                        val pendingDeepLink = state().pendingDeepLink
                        if (pendingDeepLink != null) {
                            val deepLinkCommand = MainWorkCommand.ProcessDeepLink(pendingDeepLink)
                            workProcessor.work(deepLinkCommand).collectAndHandleWork()
                        }
                    }
                } else {
                    sendAction(MainAction.UpdateInitialNavigationDone(true))
                }
            }
            is MainEvent.ProcessDeepLink -> {
                sendAction(MainAction.UpdatePendingDeepLink(event.deepLinkUrl))
                if (state().isInitialNavigationDone) {
                    launchBackgroundWork(BackgroundKey.DEEP_LINK) {
                        val command = MainWorkCommand.ProcessDeepLink(event.deepLinkUrl)
                        workProcessor.work(command).collectAndHandleWork()
                    }
                }
            }
            is MainEvent.OpenApp -> {
                launchBackgroundWork(BackgroundKey.NAVIGATION) {
                    val command = MainWorkCommand.OpenApp(state().pendingDeepLink)
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
        is MainAction.UpdatePendingDeepLink -> currentState.copy(
            pendingDeepLink = action.deepLinkUrl,
        )
        is MainAction.UpdateInitialNavigationDone -> currentState.copy(
            isInitialNavigationDone = action.isDone,
        )
    }

    enum class BackgroundKey : BackgroundWorkKey {
        THEME, REMINDERS, NAVIGATION, DEEP_LINK
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
