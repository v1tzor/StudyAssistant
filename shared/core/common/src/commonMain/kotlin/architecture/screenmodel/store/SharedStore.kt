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
package architecture.screenmodel.store

import kotlinx.coroutines.flow.FlowCollector
import managers.CoroutineManager
import architecture.communications.state.EffectCommunicator
import architecture.communications.state.StateCommunicator
import architecture.screenmodel.Actor
import architecture.screenmodel.Reducer
import architecture.screenmodel.contract.BaseAction
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
class SharedStore<S : BaseViewState, E : BaseEvent, A : BaseAction, F : BaseUiEffect>(
    private val effectCommunicator: EffectCommunicator<F>,
    stateCommunicator: StateCommunicator<S>,
    actor: Actor<S, E, A, F>,
    reducer: Reducer<S, A>,
    coroutineManager: CoroutineManager,
) : BaseStore.Abstract<S, E, A, F>(stateCommunicator, actor, reducer, coroutineManager) {

    override suspend fun postEffect(effect: F) {
        effectCommunicator.update(effect)
    }

    override suspend fun collectUiEffect(collector: FlowCollector<F>) {
        effectCommunicator.collect(collector)
    }
}
