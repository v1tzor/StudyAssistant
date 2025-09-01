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

package ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.store

import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroAction
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroEffect
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroEvent
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroOutput
import ru.aleshin.studyassistant.preview.impl.presentation.ui.intro.contract.IntroState

/**
 * @author Stanislav Aleshin on 14.04.2024
 */
internal class IntroComposeStore(
    stateCommunicator: StateCommunicator<IntroState>,
    effectCommunicator: EffectCommunicator<IntroEffect>,
    coroutineManager: CoroutineManager,
) : BaseOnlyOutComposeStore<IntroState, IntroEvent, IntroAction, IntroEffect, IntroOutput>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override suspend fun WorkScope<IntroState, IntroAction, IntroEffect, IntroOutput>.handleEvent(
        event: IntroEvent,
    ) = when (event) {
        is IntroEvent.ClickLogin -> {
            consumeOutput(IntroOutput.NavigateToLogin)
        }
        is IntroEvent.ClickRegister -> {
            consumeOutput(IntroOutput.NavigateToRegister)
        }
        is IntroEvent.SelectedNextPage -> {
            sendEffect(IntroEffect.ScrollToPage(event.currentPage + 1))
        }
        is IntroEvent.SelectedPreviousPage -> {
            sendEffect(IntroEffect.ScrollToPage(event.currentPage - 1))
        }
    }

    override suspend fun reduce(action: IntroAction, currentState: IntroState) = IntroState

    class Factory(
        private val coroutineManager: CoroutineManager,
    ) : BaseOnlyOutComposeStore.Factory<IntroComposeStore, IntroState> {

        override fun create(savedState: IntroState): IntroComposeStore {
            return IntroComposeStore(
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}