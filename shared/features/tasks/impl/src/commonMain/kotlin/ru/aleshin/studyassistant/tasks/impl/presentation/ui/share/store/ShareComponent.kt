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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.ChildComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.share.contract.ShareState

/**
 * @author Stanislav Aleshin on 25.08.2025
 */
internal abstract class ShareComponent(
    componentContext: ComponentContext
) : ChildComponent(componentContext) {

    abstract val store: ShareComposeStore

    class Default(
        storeFactory: ShareComposeStore.Factory,
        componentContext: ComponentContext,
        outputConsumer: OutputConsumer<ShareOutput>,
    ) : ShareComponent(componentContext) {

        private companion object Companion {
            const val COMPONENT_KEY = "TASKS_SHARE"
        }

        override val store by saveableStore(
            storeFactory = storeFactory,
            defaultState = ShareState(),
            stateSerializer = ShareState.serializer(),
            outputConsumer = outputConsumer,
            storeKey = COMPONENT_KEY,
        )
    }
}