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

package ru.aleshin.studyassistant.settings.impl.presentation.ui.info.store

import ru.aleshin.studyassistant.core.common.architecture.component.EmptyOutput
import ru.aleshin.studyassistant.core.common.architecture.store.BaseSimpleComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.contract.AboutAppAction
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.contract.AboutAppEffect
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.contract.AboutAppEvent
import ru.aleshin.studyassistant.settings.impl.presentation.ui.info.contract.AboutAppState

/**
 * @author Stanislav Aleshin on 04.08.2025
 */
internal class AboutAppComposeStore(
    stateCommunicator: StateCommunicator<AboutAppState>,
    effectCommunicator: EffectCommunicator<AboutAppEffect>,
    coroutineManager: CoroutineManager,
) : BaseSimpleComposeStore<AboutAppState, AboutAppEvent, AboutAppAction, AboutAppEffect>(
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {

    override suspend fun WorkScope<AboutAppState, AboutAppAction, AboutAppEffect, EmptyOutput>.handleEvent(
        event: AboutAppEvent,
    ) = Unit

    override suspend fun reduce(action: AboutAppAction, currentState: AboutAppState) = currentState

    class Factory(
        private val coroutineManager: CoroutineManager,
    ) : BaseSimpleComposeStore.Factory<AboutAppComposeStore, AboutAppState> {

        override fun create(savedState: AboutAppState): AboutAppComposeStore {
            return AboutAppComposeStore(
                stateCommunicator = StateCommunicator.Default(savedState),
                effectCommunicator = EffectCommunicator.Default(),
                coroutineManager = coroutineManager,
            )
        }
    }
}