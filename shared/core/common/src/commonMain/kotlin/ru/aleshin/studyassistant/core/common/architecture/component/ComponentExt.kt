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

package ru.aleshin.studyassistant.core.common.architecture.component

import com.arkivanov.essenty.statekeeper.ExperimentalStateKeeperApi
import com.arkivanov.essenty.statekeeper.saveable
import kotlinx.serialization.KSerializer
import ru.aleshin.studyassistant.core.common.architecture.store.BaseComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyInComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.BaseOnlyOutComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.BaseSimpleComposeStore
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreAction
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEffect
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreEvent
import ru.aleshin.studyassistant.core.common.architecture.store.contract.StoreState
import kotlin.properties.PropertyDelegateProvider
import kotlin.properties.ReadOnlyProperty

/**
 * @author Stanislav Aleshin on 20.08.2025.
 */
@OptIn(ExperimentalStateKeeperApi::class)
fun <Store : BaseComposeStore<S, E, A, F, I, O>, S : StoreState, E : StoreEvent, A : StoreAction, F : StoreEffect, I : BaseInput, O : BaseOutput> BaseComponent.saveableStore(
    storeFactory: BaseComposeStore.Factory<Store, S, I, O>,
    defaultState: S,
    stateSerializer: KSerializer<S>,
    input: I,
    outputConsumer: OutputConsumer<O>,
    componentKey: String,
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Store>> {
    return saveable(
        serializer = stateSerializer,
        state = { it.state },
        key = componentKey,
    ) { savedState ->
        storeFactory.create(savedState ?: defaultState, input, outputConsumer).apply {
            initialize(input)
        }
    }
}

@OptIn(ExperimentalStateKeeperApi::class)
fun <Store : BaseSimpleComposeStore<S, E, A, F>, S : StoreState, E : StoreEvent, A : StoreAction, F : StoreEffect> BaseComponent.saveableStore(
    storeFactory: BaseSimpleComposeStore.Factory<Store, S>,
    defaultState: S,
    stateSerializer: KSerializer<S>,
    componentKey: String,
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Store>> {
    return saveable(
        serializer = stateSerializer,
        state = { it.state },
        key = componentKey,
    ) { savedState ->
        storeFactory.create(
            savedState = savedState ?: defaultState,
            input = EmptyInput,
            output = EmptyOutputConsumer,
        ).apply {
            initialize(EmptyInput)
        }
    }
}

@OptIn(ExperimentalStateKeeperApi::class)
fun <Store : BaseOnlyInComposeStore<S, E, A, F, I>, S : StoreState, E : StoreEvent, A : StoreAction, F : StoreEffect, I : BaseInput> BaseComponent.saveableStore(
    storeFactory: BaseOnlyInComposeStore.Factory<Store, S, I>,
    defaultState: S,
    stateSerializer: KSerializer<S>,
    input: I,
    componentKey: String,
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Store>> {
    return saveable(
        serializer = stateSerializer,
        state = { it.state },
        key = componentKey,
    ) { savedState ->
        storeFactory.create(
            savedState = savedState ?: defaultState,
            input = input,
            output = EmptyOutputConsumer,
        ).apply {
            initialize(input)
        }
    }
}

@OptIn(ExperimentalStateKeeperApi::class)
fun <Store : BaseOnlyOutComposeStore<S, E, A, F, O>, S : StoreState, E : StoreEvent, A : StoreAction, F : StoreEffect, O : BaseOutput> BaseComponent.saveableStore(
    storeFactory: BaseOnlyOutComposeStore.Factory<Store, S, O>,
    defaultState: S,
    stateSerializer: KSerializer<S>,
    outputConsumer: OutputConsumer<O>,
    componentKey: String,
): PropertyDelegateProvider<Any?, ReadOnlyProperty<Any?, Store>> {
    return saveable(
        serializer = stateSerializer,
        state = { it.state },
        key = componentKey,
    ) { savedState ->
        storeFactory.create(
            savedState = savedState ?: defaultState,
            input = EmptyInput,
            output = outputConsumer,
        ).apply {
            initialize(EmptyInput)
        }
    }
}