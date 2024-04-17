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
package architecture.screenmodel

import kotlinx.coroutines.flow.FlowCollector
import architecture.screenmodel.contract.BaseEvent
import architecture.screenmodel.contract.BaseUiEffect
import architecture.screenmodel.contract.BaseViewState

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
interface StateProvider<S : BaseViewState> {
    suspend fun collectState(collector: FlowCollector<S>)
}

interface UiEffectProvider<F : BaseUiEffect> {
    suspend fun collectUiEffect(collector: FlowCollector<F>)
}

interface EventReceiver<E : BaseEvent> {
    fun dispatchEvent(event: E)
}

interface ContractProvider<S : BaseViewState, E : BaseEvent, F : BaseUiEffect, D : ScreenDependencies> :
    StateProvider<S>,
    EventReceiver<E>,
    UiEffectProvider<F>,
    Init<D>
