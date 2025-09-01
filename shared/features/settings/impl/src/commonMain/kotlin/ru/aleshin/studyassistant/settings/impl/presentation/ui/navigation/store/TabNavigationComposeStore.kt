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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationOutput
import ru.aleshin.studyassistant.settings.impl.presentation.ui.navigation.contract.TabNavigationState

/**
 * @author Stanislav Aleshin on 08.07.2024
 */
internal class TabNavigationComposeStore(
    stateCommunicator: StateCommunicator<TabNavigationState>,
    effectCommunicator: EffectCommunicator<TabNavigationEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<TabNavigationState, TabNavigationEvent, TabNavigationAction, TabNavigationEffect, TabNavigationOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override suspend fun WorkScope<TabNavigationState, TabNavigationAction, TabNavigationEffect, TabNavigationOutput>.handleEvent(
        event: TabNavigationEvent,
    ) = when (event) {
        is TabNavigationEvent.NavigateToGeneral -> {
            consumeOutput(TabNavigationOutput.NavigateToGeneral)
        }
        is TabNavigationEvent.NavigateToNotification -> {
            consumeOutput(TabNavigationOutput.NavigateToNotification)
        }
        is TabNavigationEvent.NavigateToCalendar -> {
            consumeOutput(TabNavigationOutput.NavigateToCalendar)
        }
        is TabNavigationEvent.NavigateToSubscription -> {
            consumeOutput(TabNavigationOutput.NavigateToSubscription)
        }
        is TabNavigationEvent.NavigateToAboutApp -> {
            consumeOutput(TabNavigationOutput.NavigateToAboutApp)
        }
        is TabNavigationEvent.NavigateToBack -> {
            consumeOutput(TabNavigationOutput.NavigateToBack)
        }
    }

    override suspend fun reduce(
        action: TabNavigationAction,
        currentState: TabNavigationState,
    ) = TabNavigationState

    class Factory(
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<TabNavigationComposeStore, TabNavigationState> {

        override fun create(savedState: TabNavigationState): TabNavigationComposeStore {
            return TabNavigationComposeStore(
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}