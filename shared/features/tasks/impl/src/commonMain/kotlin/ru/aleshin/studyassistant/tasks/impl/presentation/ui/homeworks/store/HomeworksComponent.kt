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

package ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.store

import com.arkivanov.decompose.ComponentContext
import ru.aleshin.studyassistant.core.common.architecture.component.ChildComponent
import ru.aleshin.studyassistant.core.common.architecture.component.OutputConsumer
import ru.aleshin.studyassistant.core.common.architecture.component.saveableStore
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksInput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksOutput
import ru.aleshin.studyassistant.tasks.impl.presentation.ui.homeworks.contract.HomeworksState

/**
 * @author Stanislav Aleshin on 25.08.2025
 */
internal abstract class HomeworksComponent(
    componentContext: ComponentContext
) : ChildComponent(componentContext) {

    abstract val store: HomeworksComposeStore

    class Default(
        storeFactory: HomeworksComposeStore.Factory,
        componentContext: ComponentContext,
        inputData: HomeworksInput,
        outputConsumer: OutputConsumer<HomeworksOutput>,
    ) : HomeworksComponent(componentContext) {

        private companion object Companion {
            const val COMPONENT_KEY = "TASKS_HOMEWORKS"
        }

        override val store by saveableStore(
            storeFactory = storeFactory,
            defaultState = HomeworksState(),
            stateSerializer = HomeworksState.serializer(),
            input = inputData,
            outputConsumer = outputConsumer,
            storeKey = COMPONENT_KEY,
        )
    }
}