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
package ru.aleshin.studyassistant.core.common.architecture.screenmodel.store

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import ru.aleshin.studyassistant.core.common.architecture.communications.state.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.Actor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.Reducer
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.StateProvider
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.UiEffectProvider
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.withReentrantLock
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
interface BaseStore<S : BaseViewState, E : BaseEvent, A : BaseAction, F : BaseUiEffect> :
    StateProvider<S>,
    UiEffectProvider<F> {

    fun sendEvent(event: E)
    suspend fun postEffect(effect: F)
    suspend fun handleAction(action: A)
    suspend fun updateState(transform: suspend (S) -> S)
    suspend fun fetchState(): S

    abstract class Abstract<S : BaseViewState, E : BaseEvent, A : BaseAction, F : BaseUiEffect>(
        private val stateCommunicator: StateCommunicator<S>,
        private val actor: Actor<S, E, A, F>,
        private val reducer: Reducer<S, A>,
        private val coroutineManager: CoroutineManager,
    ) : BaseStore<S, E, A, F> {

        private val mutex = Mutex()

        private val eventChannel = Channel<E>(Channel.UNLIMITED, BufferOverflow.SUSPEND)

        fun start(scope: CoroutineScope) = coroutineManager.runOnBackground(scope) {
            val workScope = WorkScope.Base(
                store = this@Abstract,
                coroutineScope = this
            )
            while (isActive) {
                actor.apply { workScope.handleEvent(eventChannel.receive()) }
            }
        }

        override fun sendEvent(event: E) {
            eventChannel.trySend(event)
        }

        override suspend fun fetchState(): S {
            return stateCommunicator.read()
        }

        override suspend fun updateState(transform: suspend (S) -> S) = mutex.withReentrantLock {
            val state = transform(stateCommunicator.read())
            stateCommunicator.update(state)
        }

        override suspend fun handleAction(action: A) = updateState { currentState ->
            reducer.reduce(action, currentState)
        }

        override suspend fun collectState(collector: FlowCollector<S>) {
            stateCommunicator.collect(collector)
        }
    }
}
