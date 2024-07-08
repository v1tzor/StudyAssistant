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

package ru.aleshin.studyassistant.core.common.architecture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.FlowCollector
import org.kodein.di.DIContext.Lazy
import ru.aleshin.studyassistant.core.common.architecture.communications.state.EffectCommunicator
import ru.aleshin.studyassistant.core.common.architecture.communications.state.StateCommunicator
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.Actor
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.ContractProvider
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.Reducer
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.ScreenDependencies
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseAction
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseEvent
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseUiEffect
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.contract.BaseViewState
import ru.aleshin.studyassistant.core.common.architecture.screenmodel.store.launchedStore
import ru.aleshin.studyassistant.core.common.managers.CoroutineManager
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Stanislav Aleshin on 12.06.2023.
 */
abstract class BaseViewModel<S : BaseViewState, E : BaseEvent, A : BaseAction, F : BaseUiEffect, D : ScreenDependencies>(
    protected val stateCommunicator: StateCommunicator<S>,
    protected val effectCommunicator: EffectCommunicator<F>,
    coroutineManager: CoroutineManager,
) : ViewModel(), Reducer<S, A>, Actor<S, E, A, F>, ContractProvider<S, E, F, D> {

    private val scope get() = viewModelScope

    protected val isInitialize = AtomicBoolean(false)

    private val store = launchedStore(
        scope = scope,
        effectCommunicator = effectCommunicator,
        stateCommunicator = stateCommunicator,
        actor = this,
        reducer = this,
        coroutineManager = coroutineManager,
    )

    override fun init(deps: D) {
        isInitialize.set(true)
    }

    override fun dispatchEvent(event: E) = store.sendEvent(event)

    override suspend fun collectState(collector: FlowCollector<S>) {
        store.collectState(collector)
    }

    override suspend fun collectUiEffect(collector: FlowCollector<F>) {
        store.collectUiEffect(collector)
    }

    abstract class Factory(
        private val viewModel: Lazy<out ViewModel>,
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return viewModel.getValue as T
        }
    }
}
