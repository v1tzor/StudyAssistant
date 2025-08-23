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

package ru.aleshin.studyassistant.core.common.architecture.store

import com.arkivanov.essenty.instancekeeper.InstanceKeeper
import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import ru.aleshin.studyassistant.core.common.architecture.component.BaseInput
import ru.aleshin.studyassistant.core.common.architecture.component.BaseOutput
import ru.aleshin.studyassistant.core.common.architecture.component.EmptyInput
import ru.aleshin.studyassistant.core.common.architecture.component.EmptyOutput
import ru.aleshin.studyassistant.core.common.architecture.component.EmptyOutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.communicators.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import ru.aleshin.studyassistant.core.common.architecture.store.functional.Actor
import ru.aleshin.studyassistant.core.common.architecture.store.functional.Reducer
import ru.aleshin.studyassistant.core.common.architecture.store.work.WorkScope
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager

/**
 * @author Stanislav Aleshin on 16.08.2025.
 */
abstract class BaseComposeStore<S : StoreState, E : StoreEvent, A : StoreAction, F : StoreEffect, I : BaseInput, O : BaseOutput>(
    protected val inputData: I,
    protected val outputConsumer: OutputConsumer<O>,
    protected val stateCommunicator: StateCommunicator<S>,
    protected val effectCommunicator: EffectCommunicator<F>,
    protected val coroutineManager: CoroutineManager,
) : InstanceKeeper.Instance, ComposeStore<S, E, F, I>, Actor<S, E, A, F, O>, Reducer<S, A> {

    override val state: S get() = stateCommunicator.getValue()

    protected val isInit: AtomicBoolean = atomic(false)

    protected val mainJob: Job = SupervisorJob()
    protected val storeScope = CoroutineScope(mainJob + coroutineManager.mainDispatcher)

    private val mutex = Mutex()

    private val eventChannel = Channel<E>(Channel.UNLIMITED, BufferOverflow.SUSPEND)

    init {
        startStore()
    }

    override fun dispatchEvent(event: E) {
        eventChannel.trySend(event)
    }

    override suspend fun collectState(collector: FlowCollector<S>) {
        stateCommunicator.collect(collector)
    }

    override suspend fun collectEffects(collector: FlowCollector<F>) {
        effectCommunicator.collect(collector)
    }

    override fun onDestroy() {
        super.onDestroy()
        storeScope.cancel()
    }

    fun consumeOutput(output: O) {
        outputConsumer.consume(output)
    }

    internal suspend fun updateState(transform: suspend (S) -> S) = mutex.withReentrantLock {
        val state = transform(stateCommunicator.getValue())
        stateCommunicator.update(state)
    }

    internal suspend fun handleAction(action: A) = updateState { currentState ->
        reduce(action, currentState)
    }

    internal fun postEffect(effect: F) {
        effectCommunicator.update(effect)
    }

    internal fun startStore() = coroutineManager.runOnBackground(storeScope) {
        val workScope = WorkScope.Default(store = this@BaseComposeStore, coroutineScope = this)

        while (isActive) {
            workScope.handleEvent(eventChannel.receive())
        }
    }

    interface Factory<Store, S : StoreState, I : BaseInput, O : BaseOutput> {
        fun create(savedState: S, input: I, output: OutputConsumer<O>): Store
    }
}

abstract class BaseSimpleComposeStore<S : StoreState, E : StoreEvent, A : StoreAction, F : StoreEffect>(
    stateCommunicator: StateCommunicator<S>,
    effectCommunicator: EffectCommunicator<F>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<S, E, A, F, EmptyInput, EmptyOutput>(
    inputData = EmptyInput,
    outputConsumer = EmptyOutputConsumer,
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {
    interface Factory<Store, S : StoreState> : BaseComposeStore.Factory<Store, S, EmptyInput, EmptyOutput>
}

abstract class BaseOnlyInComposeStore<S : StoreState, E : StoreEvent, A : StoreAction, F : StoreEffect, I : BaseInput>(
    inputData: I,
    stateCommunicator: StateCommunicator<S>,
    effectCommunicator: EffectCommunicator<F>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<S, E, A, F, I, EmptyOutput>(
    inputData = inputData,
    outputConsumer = EmptyOutputConsumer,
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {
    interface Factory<Store, S : StoreState, I : BaseInput> : BaseComposeStore.Factory<Store, S, I, EmptyOutput>
}

abstract class BaseOnlyOutComposeStore<S : StoreState, E : StoreEvent, A : StoreAction, F : StoreEffect, O : BaseOutput>(
    outputConsumer: OutputConsumer<O>,
    stateCommunicator: StateCommunicator<S>,
    effectCommunicator: EffectCommunicator<F>,
    coroutineManager: CoroutineManager,
) : BaseComposeStore<S, E, A, F, EmptyInput, O>(
    inputData = EmptyInput,
    outputConsumer = outputConsumer,
    stateCommunicator = stateCommunicator,
    effectCommunicator = effectCommunicator,
    coroutineManager = coroutineManager,
) {
    interface Factory<Store, S : StoreState, O : BaseOutput> : BaseComposeStore.Factory<Store, S, EmptyInput, O>
}