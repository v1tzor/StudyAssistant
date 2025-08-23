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

package ru.aleshin.studyassistant.auth.impl.presentation.ui.login.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginOutput
import ru.aleshin.studyassistant.auth.impl.presentation.ui.login.contract.LoginState
import ru.aleshin.studyassistant.core.common.architecture.component.ChildComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore

/**
 * @author Stanislav Aleshin on 16.08.2025.
 */
internal abstract class LoginComponent(
    componentContext: ComponentContext
) : ChildComponent(componentContext) {

    abstract val store: LoginComposeStore

    class Default(
        storeFactory: LoginComposeStore.Factory,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<LoginOutput>,
    ) : LoginComponent(componentContext) {

        private companion object Companion {
            const val COMPONENT_KEY = "AUTH_LOGIN"
        }

        override val store by saveableStore(
            storeFactory = storeFactory,
            defaultState = LoginState(),
            stateSerializer = LoginState.serializer(),
            outputConsumer = outputConsumer,
            componentKey = COMPONENT_KEY,
        )
    }
}